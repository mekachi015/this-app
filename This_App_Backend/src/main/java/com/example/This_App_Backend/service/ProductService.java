
package com.example.This_App_Backend.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.productsImageUpload.ProductsImageUpload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreRepository;

import java.io.IOException;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductsRepository productRepo;

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private ProductsImageUpload productsImageUpload;

    @Autowired
    private UserRepository userRepo;



    public Products createProduct(Long storeId, ProductsDTO productsDTO, String ownerUsername, MultipartFile logoFile){
        // Validate store exists
        Stores store = storeService.getStoreById(storeId);

        // Validate user
        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("User does not have permission to create products");
        }

        // Create product entity first (WITHOUT image URL)
        Products product = new Products();
        product.setStore(store);
        product.setProductName(productsDTO.getProductName());
        product.setProductDescription(productsDTO.getProductDescription());
        product.setProductPrice(BigDecimal.valueOf(productsDTO.getProductPrice()));
        product.setCategory(productsDTO.getCategory());
        product.setStockQuantity(productsDTO.getStockQuantity());

        // Save product FIRST to get the product ID
        Products savedProduct = productRepo.save(product);
        
        System.out.println("✅ Product saved with ID: " + savedProduct.getProductId());

        // Then upload image if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String imageUrl = fileStorageService.storeProductImage(
                    logoFile,
                    storeId,
                    savedProduct.getProductId()  // ✅ Now we have the product ID
                );
                
                System.out.println("✅ Image uploaded: " + imageUrl);
                
                // Update product with image URL
                savedProduct.setImageUrl(imageUrl);
                savedProduct = productRepo.save(savedProduct);
                
                System.out.println("✅ Product updated with image URL");
            } catch (IOException e) {
                // Log the error but don't fail the entire operation
                System.err.println("⚠️ Failed to upload product image: " + e.getMessage());
                // Optionally: throw new RuntimeException("Failed to upload product image: " + e.getMessage());
            }
        }
        
        return savedProduct;
    }


    public List<Products> getProductsByStore(Long storeId, String ownerUsername){

        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stores store = storeService.getStoreById(storeId);
        return productRepo.findByStore(store);
    }

    public Products updateProduct(Long productId, ProductsDTO productDTO, String ownerUsername, MultipartFile logoFile){
        Products product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("User does not have permission to update products");
        }

        // Update product fields
        product.setProductName(productDTO.getProductName());
        product.setProductDescription(productDTO.getProductDescription());
        product.setProductPrice(BigDecimal.valueOf(productDTO.getProductPrice()));
        product.setCategory(productDTO.getCategory());
        product.setStockQuantity(productDTO.getStockQuantity());
        
        Products savedProduct = productRepo.save(product);

        // Upload new image if provided
        if (logoFile != null && !logoFile.isEmpty()){
            try{
                String imageUrl = fileStorageService.storeProductImage(
                    logoFile,
                    savedProduct.getStore().getStoreId(),
                    savedProduct.getProductId()
                );
                savedProduct.setImageUrl(imageUrl);
                savedProduct = productRepo.save(savedProduct);
            } catch (IOException e){
                throw new RuntimeException("Failed to upload product image: " + e.getMessage());
            }
        }
        
        return savedProduct;

    }

    public void deleteProduct(Long productId, String ownerUsername){

        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("User does not have permission to create stores");
        }

        productRepo.deleteById(productId);
    }

    public String uploadProductImage(Long productId, MultipartFile file, String ownerUsername) throws IOException {
          Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("User does not have permission to upload product images");
        }

        String imageUrl = fileStorageService.storeProductImage(
            file,
            product.getStore().getStoreId(),
            productId
        );

        product.setImageUrl(imageUrl);
        productRepo.save(product);

        return imageUrl;
    }
    

    private Products convertToEntity(ProductsDTO dto) {
        Products entity = new Products();
        
        entity.setProductName(dto.getProductName());
        entity.setProductDescription(dto.getProductDescription());
        entity.setCategory(dto.getCategory());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setStoreId(dto.getStoreId()); // Assuming Product entity stores this        // ... etc.

        // --- FIX IS HERE ---
        // Convert Double from DTO to BigDecimal for Entity
        if (dto.getProductPrice() != null) {
            entity.setProductPrice(BigDecimal.valueOf(dto.getProductPrice()));
        } else {
            entity.setProductPrice(null); // Handle null price
        }
        
        return entity;
    }

    private ProductsDTO convertToDTO(Products entity) {
        ProductsDTO dto = new ProductsDTO();
        // ... map all fields from entity to dto
        dto.setProductId(entity.getProductId());
        dto.setProductName(entity.getProductName());
        dto.setImageUrl(entity.getImageUrl());
        // ... etc.
        return dto;
    }

    @Transactional // Ensures this all happens in one database transaction
    public ProductsDTO createProductWithImage(ProductsDTO productDTO, MultipartFile file) throws IOException {

        // --- Step 1: Save product with null URL to get an ID ---
        Products product = convertToEntity(productDTO);
        product.setImageUrl(null); // Ensure URL is null for initial save
        
        Products savedProduct = productRepo.save(product);
        Long newProductId = savedProduct.getProductId();
        
        // --- Step 2: Upload image using the new product ID ---
        // We get storeId from the original DTO
        String imageUrl = null;
        try {
            imageUrl = productsImageUpload.storeProductImage(file, productDTO.getStoreId(), newProductId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // --- Step 3: Update the product with the image URL ---
        savedProduct.setImageUrl(imageUrl);
        Products finalProduct = productRepo.save(savedProduct);

        return convertToDTO(finalProduct);
    }

    @Transactional
    public ProductsDTO updateProductWithImage(Long productId, ProductsDTO productDTO, MultipartFile file) throws IOException {
        
        // Find the existing product
        Products existingProduct = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Update text/data fields from the DTO
        existingProduct.setProductName(productDTO.getProductName());
        existingProduct.setProductDescription(productDTO.getProductDescription());
        // Convert Double from DTO to BigDecimal for Entity
        if (productDTO.getProductPrice() != null) {
            existingProduct.setProductPrice(BigDecimal.valueOf(productDTO.getProductPrice()));
        } else {
            existingProduct.setProductPrice(null);
        }        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());
        // ... etc.

        // --- Handle Image Update ---
        if (file != null && !file.isEmpty()) {
            String newImageUrl = null;
            try {
                newImageUrl = productsImageUpload.storeProductImage(
                        file, 
                        existingProduct.getStore().getStoreId(), // Use existing storeId
                        existingProduct.getProductId()        // Use existing productId
                );
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            existingProduct.setImageUrl(newImageUrl);
        }
        
        // Save the updated product
        Products updatedProduct = productRepo.save(existingProduct);
        
        return convertToDTO(updatedProduct);
    }
}
