package com.example.This_App_Backend.service.productsImageUpload;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class ProductsImageUpload {

    @Autowired
    private Cloudinary cloudinary;

    public String storeProductImage(MultipartFile file, Long storeId, Long productId )throws Exception{
        try {
           Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "store_" + storeId + "/products",
                    "public_id", "product_" + productId,
                    "overwrite", true,
                    "resource_type", "image");
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e){
            throw new Exception("Cloudinary upload failed" + e.getMessage(), e);
        }
    }
}
