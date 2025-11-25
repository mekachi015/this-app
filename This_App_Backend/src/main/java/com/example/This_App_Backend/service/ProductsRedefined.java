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
import java.math.BigDecimal;
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
        product.setProductPrice(BigDecimal.valueOf(productsDTO.getProductPrice()));
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

    public Products updateProduct (Long productId, ProductsDTO productsDTO, Long userId, MultipartFile logoFile){

        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id:" + productId));

        User user = userRepository.findById(userId).
                orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.ADMIN){
            throw new RuntimeException("User does not have permission to update this store");
        }

        product.setProductName(product.getProductName());
        product.setProductDescription(product.getProductDescription());
        product.setProductPrice(product.getProductPrice());
        product.setCategory(product.getCategory());
        product.setStockQuantity(product.getStockQuantity());

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
    public List<Products> getAllProductsByStore(Long storeId){
        Stores store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));
        return productsRepository.findByStore(store);
    }



}
