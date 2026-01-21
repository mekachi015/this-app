package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.CartDTO.CartDto;
import com.example.This_App_Backend.dto.CartDTO.CartRequest;
import com.example.This_App_Backend.dto.CartDTO.CartResponse;
import com.example.This_App_Backend.dto.CartDTO.QuantityUpdateRequest;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class CartContoller {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Long quantity,
            Authentication authentication) {

        try{
            // Verify user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            // Verify the authenticated user matches the userId
            String authenticatedUsername = authentication.getName();
            if (!isUserAuthorized(authenticatedUsername, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            CartDto cartDto = cartService.addToCart(userId, productId, quantity);
            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("message", "Product added to cart successfully");
            response.put("data", cartDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Endpoint for checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestParam Long userId,
            @RequestParam(required = false) Long deliveryAddressId,
            Authentication authentication
    ) {
        try {
            // 1. Authorization and Authentication Check
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            // 2. Call checkout service (now returns List<CustomerOrders>)
            List<CustomerOrders> orders = cartService.checkout(userId, deliveryAddressId);

            // 3. Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Checkout successful. " + orders.size() + " order(s) placed");
            response.put("orderCount", orders.size());

            // 4. Calculate totals across all orders
            BigDecimal grandTotal = orders.stream()
                    .map(CustomerOrders::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalShipping = orders.stream()
                    .map(CustomerOrders::getShippingAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal subtotal = grandTotal.subtract(totalShipping);

            response.put("grandTotal", grandTotal);
            response.put("subtotal", subtotal);
            response.put("totalShipping", totalShipping);

            // 5. Build order details list
            List<Map<String, Object>> orderDetailsList = new ArrayList<>();

            for (CustomerOrders order : orders) {
                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("orderId", order.getOrderId());
                orderDetails.put("totalAmount", order.getTotalAmount());
                orderDetails.put("shippingAmount", order.getShippingAmount());
                orderDetails.put("orderStatus", order.getOrderStatus());
                orderDetails.put("orderDate", order.getOrderDate());

                // Store details
                if (order.getStore() != null) {
                    Map<String, Object> storeInfo = new HashMap<>();
                    storeInfo.put("storeId", order.getStore().getStoreId());
                    storeInfo.put("storeName", order.getStore().getStoreName());
                    orderDetails.put("store", storeInfo);
                }

                // Calculate subtotal for this order (total - shipping)
                BigDecimal orderSubtotal = order.getTotalAmount()
                        .subtract(order.getShippingAmount());
                orderDetails.put("subtotal", orderSubtotal);

                // Add item count for this order
                orderDetails.put("itemCount", order.getOrderItems().size());

                orderDetailsList.add(orderDetails);
            }

            response.put("orders", orderDetailsList);

            // 6. Format delivery address (same for all orders)
            if (!orders.isEmpty() && orders.get(0).getDeliveryAddress() != null) {
                response.put("deliveryAddress", formatAddress(orders.get(0).getDeliveryAddress()));
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserCart(
            @PathVariable Long userId,
            Authentication authentication){
        try{
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            List<CartDto> cartItems = cartService.getUserCart(userId);
            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("data", cartItems);
            response.put("itemCount", cartItems.size());

            Double total = cartItems.stream()
                    .mapToDouble(CartDto::getSubtotal)
                    .sum();
            response.put("total", total);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Long userId,
            @RequestParam Long quantity,
            Authentication authentication
    ) {
        try{
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            CartDto cartDto = cartService.updateCartQuantity(userId, cartItemId, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart updated successfully");
            response.put("data", cartDto);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeFromCart (
            @PathVariable Long cartItemId,
            @RequestParam Long userId,
            Authentication authentication
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            cartService.removeFromCart(userId, cartItemId);
            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("message", "Item removed from cart successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart (
            @PathVariable Long userId,
            Authentication authentication){
        try{
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            cartService.clearCart(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart cleared successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<?> getCartItemCount(
            @PathVariable Long userId,
            Authentication authentication){
        try{
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            Long count = cartService.getCartItemCount(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



    private boolean isUserAuthorized(String authenticatedUsername, Long userId) {
        return userRepo.findByUsername(authenticatedUsername)
                .map(user -> user.getUserId().equals(userId))
                .orElse(false);
    }

    private String formatAddress(User_Addresses address){
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
