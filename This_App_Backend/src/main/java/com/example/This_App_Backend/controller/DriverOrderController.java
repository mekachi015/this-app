package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.service.DriverOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/{userId}/orders")
public class DriverOrderController {

    @Autowired
    private DriverOrderService driverOrderService;


    // Get all unclaimed orders ready for pickup
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableOrders(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(driverOrderService.getAvailableOrder(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all orders assigned to this driver
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(driverOrderService.getMyOrder(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get a specific order assigned to this driver
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(driverOrderService.getOrderById(userId, orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Claim an available order
    @PostMapping("/{orderId}/claim")
    public ResponseEntity<?> claimOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(driverOrderService.claimOrder(userId, orderId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update status of a claimed order
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @PathVariable Long userId,
            @RequestParam String status
            //Authentication authentication
        ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            OrderDTO updated = driverOrderService.updateOrderStatus(userId, orderId, status);

            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}






