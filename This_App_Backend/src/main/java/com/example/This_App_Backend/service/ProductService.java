
package com.example.This_App_Backend.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
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
    private UserRepository userRepo;



    public Products createProduct(Long storeId, ProductsDTO productsDTO, String ownerUsername, MultipartFile logoFile){
        Stores store = storeService.getStoreById(storeId);

        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("User does not have permission to create stores");
        }

        Products product = new Products();
        product.setStore(store);
        product.setProductName(productsDTO.getProductName());
        product.setProductDescription(productsDTO.getProductDescription());
        product.setProductPrice(BigDecimal.valueOf(productsDTO.getProductPrice()));
        product.setCategory(productsDTO.getCategory());
        product.setStockQuantity(productsDTO.getStockQuantity());

        Products savedProduct = productRepo.save(product);

        if (logoFile != null && !logoFile.isEmpty()){
            try{
                String productUrl = fileStorageService.storeProductImage(logoFile,
                        savedProduct.getStore().getStoreId(),
                        savedProduct.getProductId() + System.currentTimeMillis());
                savedProduct.setImageUrl(productUrl);
                return productRepo.save(savedProduct);
            } catch (IOException e){
                throw new RuntimeException("Failed to upload product image" + e.getMessage());
            }
        }
        return savedProduct;
    }

    public List<Products> getProductsByStore(Long storeId){
        Stores store = storeService.getStoreById(storeId);
        return productRepo.findByStore(store);
    }

    public Products updateProduct(Long productId, ProductsDTO productDTO, String ownerUsername){
        Products product = productRepo.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("User does not have permission to create stores");
        }

        product.setProductName(productDTO.getProductName());
        product.setProductDescription(productDTO.getProductDescription());
        product.setProductPrice(BigDecimal.valueOf(productDTO.getProductPrice()));
        product.setCategory(productDTO.getCategory());
        product.setStockQuantity(productDTO.getStockQuantity());
        
        return productRepo.save(product);
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

        String imageUrl = fileStorageService.storeProductImage(file,
                product.getStore().getStoreId(),
                productId + System.currentTimeMillis());

        product.setImageUrl(imageUrl);
        productRepo.save(product);

        return imageUrl;
    }
    

}
