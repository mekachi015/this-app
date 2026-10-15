package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.wishlistDTO.WishlistDto;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/add/product")
    public ResponseEntity<?> addProductToWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            WishlistDto wishlistDto = wishlistService.addProductToWishlist(userId, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product added to wishlist successfully");
            response.put("data", wishlistDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/add/store")
    public ResponseEntity<?> addStoreToWishlist(
            @RequestParam Long userId,
            @RequestParam Long storeId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            WishlistDto wishlistDto = wishlistService.addStoreToWishlist(userId, storeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Store added to wishlist successfully");
            response.put("data", wishlistDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserWishlist(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            List<WishlistDto> wishlistItems = wishlistService.getUserWishlist(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", wishlistItems);
            response.put("itemCount", wishlistItems.size());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long wishlistId,
            @RequestParam Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            wishlistService.removeFromWishlist(userId, wishlistId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item removed from wishlist successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearWishlist(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            wishlistService.clearWishlist(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Wishlist cleared successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<?> getWishlistItemCount(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            Long count = wishlistService.getWishlistItemCount(userId);
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

    @GetMapping("/check/product")
    public ResponseEntity<?> isProductInWishlist(
            @RequestParam Long userId,
            @RequestParam Long productId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            boolean isInWishlist = wishlistService.isProductInWishlist(userId, productId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isInWishlist", isInWishlist);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/check/store")
    public ResponseEntity<?> isStoreInWishlist(
            @RequestParam Long userId,
            @RequestParam Long storeId,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "User must be logged in"));
            }

            if (!isUserAuthorized(authentication.getName(), userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Unauthorized access"));
            }

            boolean isInWishlist = wishlistService.isStoreInWishlist(userId, storeId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isInWishlist", isInWishlist);
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
}