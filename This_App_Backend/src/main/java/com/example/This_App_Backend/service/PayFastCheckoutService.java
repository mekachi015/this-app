package com.example.This_App_Backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        //Enforce single store: all cart items must be from same store
        long distinctStores = cartItems.stream()
        .map(c -> c.getStore().getStoreId())
        .distinct().count();

        if(distinctStores > 1){
            throw new RuntimeException("Cart contains items from multiple" +
            "stores Please purchase from one store at a time");
        }

        Stores store = cartItems.get(0).getStore();

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

        BigDecimal grandTotal = subtotal.add(shippingAmount);

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
        pending.setShippingAmount(shippingAmount);
        pending.setStoreId(store.getStoreId());
        pending.setCartSnapshot(cartSnapshotJson);
        pending.setStatus(PendingCheckoutStatus.PAYMENT_PENDING);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        pendingCheckoutRepo.save(pending);

        //Build payfast payment URL
        String itemName = "Order from " + store.getStoreName();
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
        response.setShippingAmount(shippingAmount);
        response.setStoreName(store.getStoreName());
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

        //Get store and address
        Stores store = cartItems.get(0).getStore();
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

       //Calculate order total
       BigDecimal subTotal = cartItems.stream()
         .map(c -> c.getProduct().getProductPrice()
         .multiply(BigDecimal.valueOf(c.getQuantity())))
         .reduce(BigDecimal.ZERO, BigDecimal :: add);

        BigDecimal grandTotal = subTotal.add(pending.getShippingAmount());

        //Create customerOrders
        CustomerOrders order = new CustomerOrders();
        order.setUser(user);
        order.setStore(store);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAmount(pending.getShippingAmount());
        order.setTotalAmount(grandTotal);
        order.setDeliveryAddress(address);
        order.setIsAssignedDriver(false);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        

        // create order items
        List<Order_Items> orderItems = new ArrayList<>();
        for(Cart item : cartItems) {
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

        //Record payment with escrow status HELD
        Payments payment = new Payments();
        payment.setOrder(savedOrder);
        payment.setStore(store);
        payment.setStoreOwners(store.getStoreOwner());
        payment.setCustomer(user);
        payment.setAmount(grandTotal);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setTransactionReference(pendingId);
        payment.setPayfastPaymentId(itnParams.get("m_payment_id"));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setEscrowStatus(EscrowStatus.HELD);
        paymentsRepo.save(payment);

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
