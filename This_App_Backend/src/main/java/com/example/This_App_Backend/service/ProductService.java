
package com.example.This_App_Backend.service;

import java.math.BigDecimal;
import java.util.List;

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

    public Products createProduct(Long storeId, ProductsDTO productsDTO, String ownerUsername){
        Stores store = storeService.getStoreById(storeId);

        Products product = new Products();
        product.setStore(store);
        product.setProductName(productsDTO.getProductName());
        product.setProductDescription(productsDTO.getProductDescription());
        product.setProductPrice(BigDecimal.valueOf(productsDTO.getProductPrice()));
        product.setCategory(productsDTO.getCategory());
        product.setStockQuantity(productsDTO.getStockQuantity());

        return productRepo.save(product);
    }

    public List<Products> getProductsByStore(Long storeId){
        Stores store = storeService.getStoreById(storeId);
        return productRepo.findByStore(store);
    }

    public Products updateProduct(Long productId, ProductsDTO productDTO){
        Products product = productRepo.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.setProductName(productDTO.getProductName());
        product.setProductDescription(productDTO.getProductDescription());
        product.setProductPrice(BigDecimal.valueOf(productDTO.getProductPrice()));
        product.setCategory(productDTO.getCategory());
        product.setStockQuantity(productDTO.getStockQuantity());
        
        return productRepo.save(product);
    }

    public void deleteProduct(Long productId){
        productRepo.deleteById(productId);
    }

    public String uploadProductImage(Long productId, MultipartFile file) throws IOException{
        Products product = productRepo.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        String imageUrl = fileStorageService.storeProductImage(file, product.getStore().getStoreId(), productId);
        product.setImageUrl(imageUrl);
        productRepo.save(product);  

        return imageUrl;
    }
    

}
