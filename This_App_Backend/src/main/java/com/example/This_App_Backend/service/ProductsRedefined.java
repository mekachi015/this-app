package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class ProductsRedefined {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserRepository userRepository;

    public Products createProduct(Long storeId, ProductsDTO productsDTO, Long userId, MultipartFile logoFile){
        Stores store = storeService.getStoreById(storeId);

        User user = userRepository.findByUserId(userId).
                orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getUserType() != User.UserType.ADMIN){
            throw new RuntimeException("User does not have permission to create products");
        }

        Products product = new Products();
        product.setStore(store);
        product.setCreatedBy(user); // Set the User entity here
        product.setProductName(productsDTO.getProductName());
        product.setProductDescription(productsDTO.getProductDescription());
        product.setProductPrice(productsDTO.getProductPrice());
        product.setCategory(productsDTO.getCategory());
        product.setStockQuantity(productsDTO.getStockQuantity());

        Products savedProduct = productsRepository.save(product);

        System.out.println("✅ Product saved with ID: " + savedProduct.getProductId());

        if(logoFile != null && !logoFile.isEmpty()){
            try {
                String imageUrl = fileStorageService.storeProductImage(
                        logoFile,
                        storeId,
                        savedProduct.getProductId()
                );

                savedProduct.setImageUrl(imageUrl);
                savedProduct = productsRepository.save(savedProduct);
            } catch(IOException e){
                System.err.println("Failed to upload product image" + e.getMessage());
            }
        }

        return  savedProduct;
    }

    public Products updateProduct(Long productId, ProductsDTO productsDTO, Long userId, MultipartFile logoFile){

        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id:" + productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.ADMIN){
            throw new RuntimeException("User does not have permission to update this product");
        }

        // Update fields from DTO
        product.setProductName(productsDTO.getProductName());
        product.setProductDescription(productsDTO.getProductDescription());
        product.setProductPrice(productsDTO.getProductPrice());
        product.setCategory(productsDTO.getCategory());
        product.setStockQuantity(productsDTO.getStockQuantity());

        Products savedProduct = productsRepository.save(product);

        // Upload new image if provided
        if (logoFile != null && !logoFile.isEmpty()){
            try{
                String imageUrl = fileStorageService.storeProductImage(
                        logoFile,
                        savedProduct.getStore().getStoreId(),
                        savedProduct.getProductId()
                );
                savedProduct.setImageUrl(imageUrl);
                savedProduct = productsRepository.save(savedProduct);
            } catch (IOException e){
                throw new RuntimeException("Failed to upload product image: " + e.getMessage());
            }
        }

        return savedProduct;
    }

    //get all products for a specific store
    public List<ProductsDTO> getAllProductsByStore(Long storeId){
        Stores store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        List<Products> products = productsRepository.findByStore(store);

        return products.stream()
                .map(product -> {
                    ProductsDTO dto = new ProductsDTO();
                    dto.setProductId(product.getProductId());
                    dto.setProductName(product.getProductName());
                    dto.setProductDescription(product.getProductDescription());
                    dto.setProductPrice(product.getProductPrice());
                    dto.setCategory(product.getCategory());
                    dto.setStockQuantity(product.getStockQuantity());
                    dto.setImageUrl(product.getImageUrl());
                    dto.setStoreId(product.getStore().getStoreId());
                    dto.setUserId(product.getCreatedBy().getUserId());
                    return dto;
                })
                .toList();
    }

    public List<ProductsDTO> getAllPublicProductsForStore(Long storeId){
        Stores store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with store id"));

        List<Products> products = productsRepository.findByStore(store);

        return products.stream()
                .map(product -> {
                    ProductsDTO dto = new ProductsDTO();
                    dto.setProductId(product.getProductId());
                    dto.setProductName(product.getProductName());
                    dto.setProductDescription(product.getProductDescription());
                    dto.setProductPrice(product.getProductPrice());
                    dto.setCategory(product.getCategory());
                    dto.setStockQuantity(product.getStockQuantity());
                    dto.setImageUrl(product.getImageUrl());
                    dto.setStoreId(product.getStore().getStoreId());
                    dto.setUserId(product.getCreatedBy().getUserId());
                    return dto;
                })
                .toList();

    }


    /**
     * Search products across all stores
     */
    public List<ProductsDTO> searchProducts(String searchTerm){
        if(searchTerm == null || searchTerm.trim().isEmpty()){
            return productsRepository.findAll().stream()
            .map(this::convertToDTO)
            .toList();
        }

        String search = searchTerm.toLowerCase().trim();
         return productsRepository.findAll().stream()
            .filter(product -> 
                product.getProductName().toLowerCase().contains(search) ||
                (product.getProductDescription() != null && 
                 product.getProductDescription().toLowerCase().contains(search)) ||
                (product.getCategory() != null && 
                 product.getCategory().toLowerCase().contains(search))
            )
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Search products within a specific store
     */
    public List<ProductsDTO> searchProductsByStore(Long storeId, String searchTerm){
        Stores store = storeRepository.findById(storeId)
        .orElseThrow(() -> new RuntimeException("Store not found with id:"+ storeId));

        List<Products> products =  productsRepository.findByStore(store);

        if(searchTerm == null || searchTerm.trim().isEmpty()){
            return products.stream()
            .map(this::convertToDTO)
            .toList();
        }

        String search = searchTerm.toLowerCase().trim();
          return products.stream()
            .filter(product -> 
                product.getProductName().toLowerCase().contains(search) ||
                (product.getProductDescription() != null && 
                 product.getProductDescription().toLowerCase().contains(search)) ||
                (product.getCategory() != null && 
                 product.getCategory().toLowerCase().contains(search))
            )
            .map(this::convertToDTO)
            .toList();
    }

    /**
     * Helper method
     */
    private ProductsDTO convertToDTO(Products product) {
        ProductsDTO dto = new ProductsDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setProductPrice(product.getProductPrice());
        dto.setCategory(product.getCategory());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setStoreId(product.getStore().getStoreId());
        dto.setUserId(product.getCreatedBy().getUserId());
        return dto;
    }

}
