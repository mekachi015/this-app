package com.example.This_App_Backend.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Create a new product (Authenticated users only)
     * POST /api/products/store/{storeId}
     */
    public ResponseEntity<ProductsDTO> createProduct(
            @ModelAttribute ProductsDTO productDTO,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // Delegate all logic to the service layer
            ProductsDTO createdProduct = productService.createProductWithImage(productDTO, file);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (IOException e) {
            // Handle file upload exception
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all products for a specific store (Public)
     * GET /api/products/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getStoreProducts(@PathVariable Long storeId) {

        try {

            List<Products> products = productService.getProductsByStore(storeId);

            // map to DTOs
            List<ProductsDTO> productDTOs = products.stream().map(product -> {
                ProductsDTO dto = new ProductsDTO();
                dto.setProductId(product.getProductId());
                dto.setProductName(product.getProductName());
                dto.setProductDescription(product.getProductDescription());
                dto.setProductPrice(product.getProductPrice());
                dto.setCategory(product.getCategory());
                dto.setImageUrl(product.getImageUrl());
                dto.setStockQuantity(product.getStockQuantity());
                dto.setStoreId(product.getStore().getStoreId());
                return dto;
            }).toList();

            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error fetching products: " + e.getMessage());
        }
    }

@PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductsDTO> updateProduct(
            @PathVariable Long productId,
            @ModelAttribute ProductsDTO productDTO,
            @RequestParam(value = "file", required = false) MultipartFile file) {
            
        try {
            // Delegate all logic to the service layer
            ProductsDTO updatedProduct = productService.updateProductWithImage(productId, productDTO, file);
            return ResponseEntity.ok(updatedProduct);
        } catch (IOException e) {
            // Handle file upload exception
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) { // e.g., ProductNotFoundException
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a product (Owner only)
     * DELETE /api/products/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            productService.deleteProduct(productId, username);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error deleting product: " + e.getMessage());
        }
    }
}
