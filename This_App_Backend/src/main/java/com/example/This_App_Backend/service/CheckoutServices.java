package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.CheckoutDTO.*;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.*;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckoutServices {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProductsRepository productsRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemsRepository orderItemsRepo;

    @Autowired
    private UserAddressesRepository userAddressesRepo;


    @Transactional(readOnly = true)
    public CheckoutSummaryDTO getCheckoutSummary(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can checkout items");
        }

        List<Cart> cartItems = cartRepo.findByUser(user);

        if(cartItems.isEmpty()){
            throw new RuntimeException("Cart is empty");
        }

        //Group items by store
        Map<Stores, List<Cart>> itemsByStore = cartItems.stream()
                .collect(Collectors.groupingBy(Cart :: getStore));

        CheckoutSummaryDTO summary = new CheckoutSummaryDTO();
        summary.setUserId(userId);

        List<StoreOrderSummary> storeOrders = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for(Map.Entry<Stores, List<Cart>> entry : itemsByStore.entrySet()){
            Stores stores = entry.getKey();
            List<Cart> storeCartItem = entry.getValue();

            StoreOrderSummary storeOrder = new StoreOrderSummary();
            storeOrder.setStoreId(stores.getStoreId());
            storeOrder.setStoreName(stores.getStoreName());
            storeOrder.setStoreAddress(stores.getStoreAddress());

            List<CheckoutItemDTO> items = new ArrayList<>();
            BigDecimal storeTotal = BigDecimal.ZERO;

            for(Cart cartItem : storeCartItem){
                Products product = cartItem.getProduct();

                //Check stock availablity
                if(product.getStockQuantity() < cartItem.getQuantity()){
                    throw new RuntimeException(
                            String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                                    product.getProductName(),
                                    product.getStockQuantity(),
                                    cartItem.getQuantity())
                    );
                }

                CheckoutItemDTO itemDTO = new CheckoutItemDTO();
                itemDTO.setCartItemId(cartItem.getCartItemId());
                itemDTO.setProductId(product.getProductId());
                itemDTO.setProductName(product.getProductName());
                itemDTO.setProductImage(product.getImageUrl());
                itemDTO.setProductPrice(product.getProductPrice());
                itemDTO.setQuantity(cartItem.getQuantity());

                BigDecimal itemTotal = product.getProductPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                itemDTO.setSubTotal(itemTotal);

                items.add(itemDTO);
                storeTotal = storeTotal.add(itemTotal);
            }

            storeOrder.setItems(items);
            storeOrder.setStoreTotal(storeTotal);
            storeOrders.add(storeOrder);
            grandTotal = grandTotal.add(storeTotal);
        }

        summary.setStoreOrders(storeOrders);
        summary.setGrandTotal(grandTotal);
        summary.setTotalStores(storeOrders.size());
        summary.setTotalItems(cartItems.stream()
                .mapToLong(Cart :: getQuantity)
                .sum());

        return summary;
    }

    @Transactional
    public CheckoutResponseDTO proccessCheckout(Long userId, CheckoutRequestDTO request){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can checkout");
        }

        List<Cart> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()){
            throw new RuntimeException("Cart is empty. Nothing to checkout");
        }

        //Validate and get delivery address
        User_Addresses deliveryAddress = validateAndGetDeliveryAddress(
                userId, user, request.getDeliveryAddressId()
        );

        //Group cart by store
        Map<Stores, List<Cart>> itemsByStore = cartItems.stream()
                .collect(Collectors.groupingBy(Cart :: getStore));

        List<CustomerOrders> createdOrders = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        //Create separate order for each other
        for(Map.Entry<Stores, List<Cart>> entry : itemsByStore.entrySet()){
            Stores store = entry.getKey();
            List<Cart> storeCartItems = entry.getValue();

            BigDecimal storeTotal = BigDecimal.ZERO;

            //Create order for specific store
            CustomerOrders order = new CustomerOrders();
            order.setUser(user);
            order.setStore(store);
            order.setOrderStatus("PENDING");
            order.setDeliveryAddress(deliveryAddress);
            order.setIsAssignedDriver(false);
            order.setOrderDate(LocalDateTime.now());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            //Create order items for this store
            List<Order_Items> orderItems = new ArrayList<>();
            for(Cart cartItem : storeCartItems){
                Products products = cartItem.getProduct();
                BigDecimal priceAtPurchase = products.getProductPrice();

                //Final stock check
                if(products.getStockQuantity() < cartItem.getQuantity()){
                    throw new RuntimeException(
                            String.format("Insufficient stock for %s from store %s",
                            products.getProductName(), store.getStoreName())
                    );
                }

                Order_Items orderItem = new Order_Items();
                orderItem.setOrder(order);
                orderItem.setProduct(products);
                orderItem.setQuantity(cartItem.getQuantity().intValue());
                orderItem.setPriceAtPurchase(priceAtPurchase);
                orderItem.setCreatedAt(LocalDateTime.now());

                orderItems.add(orderItem);

                //Update product stock
                products.setStockQuantity(
                        products.getStockQuantity() - cartItem.getQuantity().intValue()
                );

                productsRepo.save(products);

                BigDecimal itemTotal = priceAtPurchase
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                storeTotal = storeTotal.add(itemTotal);
            }

            order.setTotalAmount(storeTotal);
            order.setOrderItems(orderItems);

            CustomerOrders savedOrder = orderRepo.save(order);
            createdOrders.add(savedOrder);
            grandTotal = grandTotal.add(storeTotal);
        }

        //clear the cart after a succesfull creation
        cartRepo.deleteAll(cartItems);

        //Build response
        CheckoutResponseDTO response = new CheckoutResponseDTO();
        response.setSuccess(true);
        response.setMessage("Checkout successful! " + createdOrders.size() +
                " order(s) created from " + itemsByStore.size() + " store(s)");
        response.setTotalOrders(createdOrders.size());
        response.setTotalOrders(createdOrders.size());
        response.setGrandTotal(grandTotal);

        List<OrderSummaryDTO> orderSummaries = createdOrders.stream()
                .map(this:: convertOrderSummary)
                .collect(Collectors.toList());
        response.setOrders(orderSummaries);

        return response;
    }

    private User_Addresses validateAndGetDeliveryAddress(
            Long userId, User user, Long deliveryAddressId
    ) {
        User_Addresses deliveryAddress;

        if (deliveryAddressId != null) {
            deliveryAddress = userAddressesRepo.findById(deliveryAddressId)
                    .orElseThrow(() -> new RuntimeException("Delivery address not found"));

            if (!deliveryAddress.getUser().getUserId().equals(userId)) {
                throw new RuntimeException("Delivery address does not belong to this user");
            }
        } else {
            deliveryAddress = userAddressesRepo.findFirstByUserAndIsDefault(user, true)
                    .orElseGet(() -> userAddressesRepo.findFirstByUser(user)
                            .orElseThrow(() -> new RuntimeException(
                                    "No delivery address found. Please add a delivery address before checkout."
                            )));
        }

        // Validate address completeness
        if (deliveryAddress.getAddressLine1() == null ||
                deliveryAddress.getAddressLine1().trim().isEmpty()) {
            throw new RuntimeException(
                    "Delivery address is incomplete. Please update your address."
            );
        }

        return deliveryAddress;
    }


    private OrderSummaryDTO convertOrderSummary(CustomerOrders order){
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setOrderId(order.getOrderId());
        dto.setStoreId(order.getStore().getStoreId());
        dto.setStoreName(order.getStore().getStoreName());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderDate(order.getOrderDate());
        dto.setItemCount(order.getOrderItems().size());

        if(order.getDeliveryAddress() != null) {
            dto.setDeliveryAddress(formatAddress(order.getDeliveryAddress()));
        }

        return dto;
    }

    private String formatAddress(User_Addresses address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null && !address.getAddressLine2().trim().isEmpty()) {
            sb.append(", ").append(address.getAddressLine2());
        }
        if (address.getAddressLine3() != null && !address.getAddressLine3().trim().isEmpty()) {
            sb.append(", ").append(address.getAddressLine3());
        }
        sb.append(", ").append(address.getCity());
        sb.append(", ").append(address.getState());
        sb.append(" ").append(address.getPostalCode());
        return sb.toString();
    }

}
