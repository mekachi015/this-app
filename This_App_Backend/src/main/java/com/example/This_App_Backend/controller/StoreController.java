package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.dto.StoresDTO.StoreDTO;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.service.StoreService;
import com.example.This_App_Backend.service.OrderService;

import jakarta.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;





@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class StoreController {
    
    @Autowired
    private StoreService storeService;

    @Autowired
    private OrderService orderService;

    /**
     * Create a new store (Authenticated users only)
     * POST /api/stores
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStore(
            @RequestPart("storeData") StoreDTO storeDTO,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Stores store;

            if (logoFile != null && !logoFile.isEmpty()) {
                store = storeService.createStore(storeDTO, username, logoFile);
            } else {
                store = storeService.createStore(storeDTO, username, null);
            }

            StoreDTO responseDTO = storeService.convertToDTO(store);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating store: " + e.getMessage());
        }
    }

    /**
     * Get all stores owned by the logged-in user
     * GET /api/stores/my-stores
     */
    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Stores> stores = storeService.getMyStores(username);
            List<StoreDTO> storeDTOs = stores.stream()
                    .map(storeService::convertToDTO)
                    .toList();
            return ResponseEntity.ok(storeDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error fetching stores: " + e.getMessage());
        }
    }

    /**
     * Get a specific store owned by the logged-in user
     * GET /api/stores/my-stores/{id} - owner id
     */
    @GetMapping("/my-stores/{userId}")
    public ResponseEntity<?> getStoresByUserId(@PathVariable Long userId) {
        try {
            List<Stores> stores = storeService.getStoresByUserId(userId);
            List<StoreDTO> storeDTOs = stores.stream()
                    .map(storeService::convertToDTO)
                    .toList();
            return ResponseEntity.ok(storeDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error fetching stores: " + e.getMessage());
        }
    }


    /**
     * Update a store (Owner only)
     * PUT /api/stores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStore(
            @PathVariable Long id,
            @RequestBody StoreDTO storeDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Stores updatedStore = storeService.updateStore(id, storeDTO, username);
            StoreDTO responseDTO = storeService.convertToDTO(updatedStore);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating store: " + e.getMessage());
        }
    }

    /**
     * Delete a store (Owner only)
     * DELETE /api/stores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStore(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            storeService.deleteStore(id, username);
            return ResponseEntity.ok("Store deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error deleting store: " + e.getMessage());
        }
    }

    /**
     * Upload store logo (Owner only)
     * POST /api/stores/{id}/logo
     */
    @PostMapping("/{id}/logo")
    public ResponseEntity<?> uploadStoreLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String logoUrl = storeService.uploadStoreLogo(id, file, username);
            return ResponseEntity.ok(logoUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading logo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Get all stores (Public)
     * GET /api/stores/public
     */
    @GetMapping("/public")
    public ResponseEntity<?> getAllStores() {
        try {
            List<Stores> stores = storeService.getAllStores();
            List<StoreDTO> storeDTOs = stores.stream()
                    .map(storeService::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(storeDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching stores: " + e.getMessage());
        }
    }

    /**
     * Get a specific store by ID (Public)
     * GET /api/stores/public/{id}
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getStoreById(@PathVariable Long id) {
        try {
            Stores store = storeService.getStoreById(id);
            StoreDTO storeDTO = storeService.convertToDTO(store);
            return ResponseEntity.ok(storeDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Store not found: " + e.getMessage());
        }
    }

    /** 
     * Search for stores (Public)
     * Get 
    */
   @GetMapping("/search")
   public ResponseEntity<?> searchStores(@RequestParam(required = false) String q) {
        try{
            List<Stores> stores = storeService.searchStores(q);
            List<StoreDTO> storeDTOs = stores.stream()
            .map(storeService::convertToDTO)
            .collect(Collectors.toList());
            
            return ResponseEntity.ok(storeDTOs);
        } catch(RuntimeException e){
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error searching stores: " + e.getMessage());   
        }
   }

   /**
     * Get all orders for all stores owned by a specific owner
     * GET /api/stores/owner/{ownerId}/orders
     */
    @GetMapping("/owner/{ownerId}/orders")
    public ResponseEntity<?> getOrdersByOwnerId(@PathVariable Long ownerId) {
        try {
            List<OrderDTO> orders = orderService.getOrdersByOwnerId(ownerId);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse(e.getMessage())
            );
        }
    }

    /**
     * Get all orders for the logged-in user's stores
     * GET /api/stores/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyStoreOrders(@RequestParam Long userId) {
        try {
            List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
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
