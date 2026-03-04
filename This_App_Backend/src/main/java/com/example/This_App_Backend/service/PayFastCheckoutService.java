package com.example.This_App_Backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.This_App_Backend.Enuma.EscrowStatus;
import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.Enuma.PaymentStatus;
import com.example.This_App_Backend.Enuma.PendingCheckoutStatus;
import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutInitiateResponse;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Order_Items;
import com.example.This_App_Backend.entity.Payments;
import com.example.This_App_Backend.entity.PendingCheckout;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.CartRepo;
import com.example.This_App_Backend.repository.OrderItemsRepository;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.PaymentsRepository;
import com.example.This_App_Backend.repository.PendingCheckoutRepository;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.UserAddressesRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PayFastCheckoutService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserAddressesRepository userAddressRepo;

    @Autowired
    private PendingCheckoutRepository pendingCheckoutRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemsRepository orderItemsRepo;

    @Autowired
    private PaymentsRepository paymentsRepo;

    @Autowired
    private ProductsRepository productsRepo;

    @Autowired
    private PayfastService payfastService;

    @Value("${platform.commission.percent}")
    private int commissionPercent;

//    private static final BigDecimal SHIPPING_AMOUNT = new BigDecimal("20.00"); // Flat shipping rate

    @Value("${platform.shipping.fee}")
    private BigDecimal shippingAmount;

    @Value("${platform.shipping.fee.multistore}")
    private BigDecimal multiStoreShippingAmount;

    //Validate , build pending record, return payfast url
    @Transactional
    public CheckoutInitiateResponse initiateCheckout(Long userId, Long deliveryAddressId) throws Exception{
        
        //Validate the user
        User user = userRepo.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
        if(user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can initiate checkout");
        }

        // get cart items and calculate total
        List<Cart> cartItems = cartRepo.findByUser(user);
        if(cartItems.isEmpty()){
            throw new RuntimeException("Cart is empty");
        }

        //Count distinct stores for shipping fee calculation
        long distinctStores = cartItems.stream()
        .map(c -> c.getStore().getStoreId())
        .distinct().count();

        int storeCount = (int) distinctStores;

        // Determine applicable shipping fee
        BigDecimal applicableShipping = distinctStores > 1 ? multiStoreShippingAmount : shippingAmount;

        //Stock check - do not deduct here
        for(Cart item: cartItems){
            if(item.getProduct().getStockQuantity() < item.getQuantity()){
                throw new RuntimeException("Insufficient stock for:"
                + item.getProduct().getProductName());
            }
        }

        //Validate delivery address 
        User_Addresses address = resolveDeliveryAddress(userId, user, deliveryAddressId);

        //Calculate totals
        BigDecimal subtotal = cartItems.stream()
        .map(c -> c.getProduct().getProductPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal:: add);

        BigDecimal grandTotal = subtotal.add(applicableShipping);

        //Serialize cart snapshot to json
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> snapshot = new ArrayList<>();
        for (Cart item : cartItems){
            Map<String, Object> entry = new HashMap<>();
            entry.put("cartItemId", item.getCartItemId());
            entry.put("productId", item.getProduct().getProductId());
            entry.put("productName", item.getProduct().getProductName());
            entry.put("quantity", item.getQuantity());
            entry.put("priceAtTime", item.getProduct().getProductPrice().toString());
            snapshot.add(entry);
        }

        String cartSnapshotJson = mapper.writeValueAsString(snapshot);

        //Create pending checkout record
        String pendingId = UUID.randomUUID().toString();
        PendingCheckout pending = new PendingCheckout();
        pending.setPendingCheckoutId(pendingId);
        pending.setUser(user);
        pending.setDeliveryAddressId(address.getAddressId());
        pending.setTotalAmount(grandTotal);
        pending.setShippingAmount(applicableShipping);
        pending.setStoreId(storeCount == 1 ? cartItems.get(0).getStore().getStoreId() : null);
        pending.setStoreCount(storeCount);
        pending.setCartSnapshot(cartSnapshotJson);
        pending.setStatus(PendingCheckoutStatus.PAYMENT_PENDING);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        pendingCheckoutRepo.save(pending);

        //Build payfast payment URL
        String itemName;
        if (storeCount == 1) {
            itemName = "Order from " + cartItems.get(0).getStore().getStoreName();
        } else {
            itemName = "Order from " + storeCount + " stores";
        }
        String paymentUrl = payfastService.buildPaymentUrl(
                pendingId,
                grandTotal,
                itemName,
                user.getFirstName() != null ? user.getFirstName() : "Customer",
                user.getLastName() != null ? user.getLastName() : "",
                user.getEmail()
        );

        //Return response from frontend
        CheckoutInitiateResponse response = new CheckoutInitiateResponse();
        response.setPendingCheckoutId(pendingId);
        response.setPaymentUrl(paymentUrl);
        response.setTotalAmount(grandTotal);
        response.setShippingAmount(applicableShipping);
        response.setStoreName(itemName.replace("Order from ", ""));
        return response;
    }

    //Payfast ITN webhook - create order only after confirmed payment
    @Transactional
    public void processPayfastItn (Map<String, String> itnParams){

        String pendingId = itnParams.get("m_payment_id");
        if(pendingId == null){
            return;
        }

        //find the pending checkout - reject if already processed (idempotency)
        PendingCheckout pending = pendingCheckoutRepo
        .findByPendingCheckoutIdAndStatus(pendingId, PendingCheckoutStatus.PAYMENT_PENDING)
        .orElseThrow(() -> new RuntimeException("PendingCheckout not founf or already processed"));


        //Verify ITN Signatuur and amount
        if(!payfastService.verifyITN(itnParams,pending.getTotalAmount())){
            pending.setStatus(PendingCheckoutStatus.FAILED);
            pendingCheckoutRepo.save(pending);
            throw new RuntimeException("ITN verfication failed");
        }

        //Check expiry
        if(LocalDateTime.now().isAfter(pending.getExpiredAt())){
            pending.setStatus(PendingCheckoutStatus.EXPIRED);
            pendingCheckoutRepo.save(pending);
            throw new RuntimeException("Checkout session has expired");
        }

        User user = pending.getUser();

        //Re-fetch cart items (use snapshot for safety if cart was already cleared)
        List<Cart> cartItems = cartRepo.findByUser(user);
        if(cartItems.isEmpty()){
            throw new RuntimeException("Cart is empty at time of payment processing");
        }

        //Get delivery address
        User_Addresses address = userAddressRepo.findById(pending.getDeliveryAddressId())
            .orElseThrow(() -> new RuntimeException("Delivery address was not found"));

       //Final stock check and deduction
       for(Cart item : cartItems){
            Products product = item.getProduct();
            if(product.getStockQuantity() < item.getQuantity()){
                throw new RuntimeException("Stock changed for: " + product.getProductName());
            }
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity().intValue());
            productsRepo.save(product);
       }

       // Group cart items by store for multi-store support
       Map<Stores, List<Cart>> cartByStore = cartItems.stream()
           .collect(Collectors.groupingBy(Cart::getStore));

       // Generate a single checkout session ID for all orders from this checkout
       String checkoutSessionId = UUID.randomUUID().toString();

       // Track whether this is the first store (to apply shipping fee to first order only)
       boolean isFirstStore = true;

       // Create one CustomerOrders + one Payments per store
       for (Map.Entry<Stores, List<Cart>> entry : cartByStore.entrySet()) {
           Stores store = entry.getKey();
           List<Cart> storeCartItems = entry.getValue();

           // Calculate subtotal for this store
           BigDecimal storeSubTotal = storeCartItems.stream()
               .map(c -> c.getProduct().getProductPrice()
                   .multiply(BigDecimal.valueOf(c.getQuantity())))
               .reduce(BigDecimal.ZERO, BigDecimal::add);

           // Apply shipping to first store only, others get 0 shipping
           BigDecimal storeShipping = isFirstStore ? pending.getShippingAmount() : BigDecimal.ZERO;
           BigDecimal storeTotal = storeSubTotal.add(storeShipping);

           // Create CustomerOrders for this store
           CustomerOrders order = new CustomerOrders();
           order.setUser(user);
           order.setStore(store);
           order.setOrderStatus(OrderStatus.PENDING);
           order.setOrderDate(LocalDateTime.now());
           order.setShippingAmount(storeShipping);
           order.setTotalAmount(storeTotal);
           order.setDeliveryAddress(address);
           order.setIsAssignedDriver(false);
           order.setCheckoutSessionId(checkoutSessionId);
           order.setCreatedAt(LocalDateTime.now());
           order.setUpdatedAt(LocalDateTime.now());

           // Create order items for this store
           List<Order_Items> orderItems = new ArrayList<>();
           for(Cart item : storeCartItems) {
               Order_Items oi = new Order_Items();
               oi.setOrder(order);
               oi.setProduct(item.getProduct());
               oi.setQuantity(item.getQuantity().intValue());
               oi.setPriceAtPurchase(item.getProduct().getProductPrice());
               oi.setCreatedAt(LocalDateTime.now());
               orderItems.add(oi);
           }

           order.setOrderItems(orderItems);
           CustomerOrders savedOrder = orderRepo.save(order);

           // Record payment with escrow status HELD for this store
           Payments payment = new Payments();
           payment.setOrder(savedOrder);
           payment.setStore(store);
           payment.setStoreOwners(store.getStoreOwner());
           payment.setCustomer(user);
           payment.setAmount(storeTotal);
           payment.setPaymentDate(LocalDateTime.now());
           payment.setCreatedAt(LocalDateTime.now());
           // For multi-store: append order ID to transaction reference to ensure uniqueness
           payment.setTransactionReference(pendingId + "-" + savedOrder.getOrderId());
           payment.setPayfastPaymentId(itnParams.get("m_payment_id"));
           payment.setPaymentStatus(PaymentStatus.COMPLETED);
           payment.setEscrowStatus(EscrowStatus.HELD);
           paymentsRepo.save(payment);

           isFirstStore = false;
       }

        //clear the cart
        cartRepo.deleteAll(cartItems);

        //Mark PendingCheckout as processed
        pending.setStatus(PendingCheckoutStatus.COMPLETED);
        pending.setPayfastPaymentId(itnParams.get("pf_payment_id"));
        pending.setProcessedAt(LocalDateTime.now());
        pendingCheckoutRepo.save(pending);
    }

    //private helper
    private User_Addresses resolveDeliveryAddress(Long userId, User user, Long deliveryAddressId) {
        User_Addresses address;
        if (deliveryAddressId != null) {
            address = userAddressRepo.findById(deliveryAddressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            if (!address.getUser().getUserId().equals(userId))
                throw new RuntimeException("Address does not belong to this user");
        } else {
            address = userAddressRepo.findFirstByUserAndIsDefault(user, true)
                    .orElseGet(() -> userAddressRepo.findFirstByUser(user)
                            .orElseThrow(() -> new RuntimeException(
                                    "No delivery address found. Please add one before checkout.")));
        }
        if (address.getCity() == null || address.getPostalCode() == null)
            throw new RuntimeException("Delivery address is incomplete.");
        return address;
    }



}
