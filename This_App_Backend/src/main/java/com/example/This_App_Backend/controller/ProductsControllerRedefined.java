package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.ProductsRedefined;
import com.example.This_App_Backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Added for security context
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/redefine/")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductsControllerRedefined {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ProductsRedefined productsService; // the name of the class is Product-Redefined

    /**
     * POST: Creates a new product for the given store.
     * Uses Authentication to get the ownerUsername.
     */
    @PostMapping(value = "/post/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @ModelAttribute ProductsDTO productsDTO,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            Authentication authentication) {

        String ownerUsername = authentication.getName();
        User authenticatedUser = userService.getUserByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        Long authenticatedUserId = authenticatedUser.getUserId();

        try {
            // 1. Validate the file if it's provided
            if (logoFile != null && !logoFile.isEmpty()) {
                String contentType = logoFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Only image files are allowed for the product photo"));
                }
                if (logoFile.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Product photo file size must be less than 5MB"));
                }
            }

            // 2. Get storeId from the DTO
            Long storeId = productsDTO.getStoreId();
            if (storeId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Store ID is required"));
            }

            // 3. Call the service
            Products newProduct = productsService.createProduct(
                    storeId,
                    productsDTO,
                    authenticatedUserId,
                    logoFile
            );

            // 4. Return success response
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Failed to create product: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    /**
     * PUT: Updates an existing product.
     * Uses Authentication to get the ownerUsername.
     */
    @PutMapping(path = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId, // Only this one is needed
            @ModelAttribute ProductsDTO productsDTO,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            Authentication authentication) {

        String ownerUsername = authentication.getName();
        User authenticatedUser = userService.getUserByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        Long authenticatedUserId = authenticatedUser.getUserId();

        try {
            // 1. Optional file validation here (similar to the POST method)
            if (logoFile != null && !logoFile.isEmpty()) {
                // ... (add file validation logic here) ...
            }

            // 3. Call the service
            Products updatedProduct = productsService.updateProduct(
                    productId,
                    productsDTO,
                    authenticatedUserId,
                    logoFile
            );

            // 4. Return success response
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Check if it's a "Not Found" error to return 404
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            // Otherwise, typically a permission issue (403 Forbidden)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Failed to update product: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get : Retrieve all products for a specific strore
     *
     */
   @GetMapping("stores/{storeId}/products")
   public ResponseEntity<?> getProductsForSpecificStore(@PathVariable Long storeId){
       return ResponseEntity.ok(productsService.getAllProductsByStore(storeId));
   }

    @GetMapping("stores/{storeId}/products/public")
    public ResponseEntity<?> getAllProductsByStorePublic(@PathVariable Long storeId) {
        return ResponseEntity.ok(productsService.getAllPublicProductsForStore(storeId));
    }


}
