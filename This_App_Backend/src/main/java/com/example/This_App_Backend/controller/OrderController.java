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
@CrossOrigin (origins = "http://localhost:4200", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //Get all orders for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        try{
            if(authentication == null || !authentication.isAuthenticated()){
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
        } catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    //Get orders by specific Id
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long orderId,
            @RequestParam Long userId,
            Authentication authentication
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("syccess", false, "message", "user must be logged in "));
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

    //Get order count
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> getOrderCount (
            @PathVariable Long userId,
            Authentication authentication
    ) {
        try{
            if (authentication == null || !authentication.isAuthenticated()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            Long count = orderService.getOrderCount(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order count retrieved successfully");
            response.put("count", count);

            return ResponseEntity.ok(response);
        } catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    //get all orders for a store
    @GetMapping("{storeId}/orders")
     public ResponseEntity<?> getStoreOrders(@PathVariable Long storeId) {
        try {
            List<OrderDTO> orders = orderService.getStoreOrders(storeId);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage())
            );
        }
    }

    //get specific order by id for a store
     @GetMapping("/{storeId}/orders/{orderId}")
    public ResponseEntity<?> getStoreOrderById(
            @PathVariable Long storeId,
            @PathVariable Long orderId) {
        try {
            OrderDTO order = orderService.getStoreOrderById(storeId, orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage())
            );
        }
    }

    //total order count for a store
      @GetMapping("/{storeId}/orders/count")
    public ResponseEntity<?> getStoreOrderCount(@PathVariable Long storeId) {
        try {
            Long count = orderService.getStoreOrderCount(storeId);
            return ResponseEntity.ok(new CountResponse(count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage())
            );
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
