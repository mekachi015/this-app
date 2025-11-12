package com.example.This_App_Backend.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.service.ProductService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Create a new product (Authenticated users only)
     * POST /api/products/store/{storeId}
     */
    @PostMapping(value = "/store/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @PathVariable Long storeId,
            @RequestPart("productData") ProductsDTO productsDTO,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Products product = productService.createProduct(storeId, productsDTO, username, logoFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    /**
     * Get all products for a specific store (Public)
     * GET /api/products/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getStoreProducts(@PathVariable Long storeId, Authentication authentication) {

        try {
            String username = authentication.getName();
            List<Products> products = productService.getProductsByStore(storeId, username);

            // map to DTOs
            List<ProductsDTO> productDTOs = products.stream().map(product -> {
                ProductsDTO dto = new ProductsDTO();
                dto.setProductId(product.getProductId());
                dto.setProductName(product.getProductName());
                dto.setProductDescription(product.getProductDescription());
                dto.setProductPrice(product.getProductPrice().doubleValue());
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

//    @PutMapping("/{productId}")
//    public ResponseEntity<Products> updateProduct(@PathVariable Long productId, @RequestBody ProductsDTO productsDTO){
//        Products product = productService.updateProduct(productId, productsDTO);
//        return ResponseEntity.ok(product);
//    }

    /**
     * Update a product (Owner only)
     * PUT /api/products/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductsDTO productsDTO,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Products product = productService.updateProduct(productId, productsDTO, username);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating product: " + e.getMessage());
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
