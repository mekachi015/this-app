package com.example.This_App_Backend.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/store/{storeId}")
    public ResponseEntity<Products> createProduct(@PathVariable Long storeId, @RequestBody ProductsDTO productsDTO, Principal principal){
        Products product = productService.createProduct(storeId, productsDTO, principal.getName());
        return ResponseEntity.ok(product);
    }

    @GetMapping("store/{storeId}")
    public ResponseEntity<List<Products>> getStoreProducts(@PathVariable Long storeId){
        return ResponseEntity.ok(productService.getProductsByStore(storeId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Products> updateProduct(@PathVariable Long productId, @RequestBody ProductsDTO productsDTO){
        Products product = productService.updateProduct(productId, productsDTO);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/image")
    public ResponseEntity<String> uploadProductImage(@PathVariable Long productId,
                                                  @RequestParam("file") MultipartFile file) 
                                                  throws IOException {
        String imageUrl = productService.uploadProductImage(productId, file);
        return ResponseEntity.ok(imageUrl);
    }
}
