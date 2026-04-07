package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.service.OrderService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Get all orders for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            List<OrderDTO> orders = orderService.getUserOrders(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order retrieved successfully");
            response.put("data", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get orders by specific Id
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "user must be logged in "));
            }

            OrderDTO order = orderService.getOrderById(userId, orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order retrieved successfully");
            response.put("data", order);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get order count
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> getOrderCount(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            Long count = orderService.getOrderCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order count retrieved successfully");
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // get all orders for a store
    @GetMapping("{storeId}/orders")
    public ResponseEntity<?> getStoreOrders(@PathVariable Long storeId) {
        try {
            List<OrderDTO> orders = orderService.getStoreOrders(storeId);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage()));
        }
    }

    // get specific order by id for a store
    @GetMapping("/{storeId}/orders/{orderId}")
    public ResponseEntity<?> getStoreOrderById(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getStoreOrderById(storeId, orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage()));
        }
    }

    // get All orders according to
    @GetMapping("/owner/{userId}/all")
    public ResponseEntity<?> getAllOrdersByUserId(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            List<OrderDTO> orders = orderService.getAllOrdersByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("data", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return switch (e.getMessage()) {
                case "USER_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found"));
                case "USER_NOT_ADMIN" -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "User is not an admin"));
                case "STORE_OWNER_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "No store owner profile found for this user"));
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Unexpected error"));
            };
        }
    }

    @GetMapping("/owner/count/{userId}")
    public ResponseEntity<?> getOrderCountByOwner(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            Long count = orderService.getOrderCountByUserId(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "totalOrders", count));

        } catch (IllegalArgumentException e) {
            return switch (e.getMessage()) {
                case "USER_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found"));
                case "USER_NOT_ADMIN" -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "User is not an admin"));
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Unexpected error"));
            };
        }
    }

    // Update order status (store owner only)
    @PatchMapping("/store/{storeId}/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @RequestParam String status,
            Authentication authentication) {
        try {

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }
            String username = authentication.getName();
            OrderDTO updatedOrder = orderService.updateOrderStatus(storeId, orderId, status, username);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order status updated successfully",
                    "order", updatedOrder));

        } catch (IllegalArgumentException e) {
            return switch (e.getMessage()) {
                case "ORDER_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Order not found"));
                case "STORE_NOT_FOUND" -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Store not found"));
                case "UNAUTHORIZED" -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "You are not authorized to update this order"));
                case "INVALID_STATUS" -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Invalid order status"));
                case "INVALID_STATUS_TRANSITION" -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message",
                                "Can only mark PENDING orders as READY_FOR_DELIVERY"));
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Unexpected error: " + e.getMessage()));
            };
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred: " + e.getMessage()));
        }
    }

    // Response classes
    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class CountResponse {
        private Long count;

        public CountResponse(Long count) {
            this.count = count;
        }

        public Long getCount() {
            return count;
        }
    }

}
