package com.example.This_App_Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {
    @Autowired
    private Cloudinary cloudinary;

    // Start of profile photo upload
    public String storeProfilePhoto(MultipartFile file, Long userId) throws IOException {
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "profile_photos",
                    "public_id", "user_" + userId,
                    "overwrite", true,
                    "resource_type", "image");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    // Generic method for any image upload
    public String uploadImage(MultipartFile file, String folder, String publicId) throws IOException {
        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "image");

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        return (String) uploadResult.get("secure_url");
    }

    // Delete image method
    public void deleteImage(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            // This wraps the Cloudinary/Runtime error into the IOException your test expects
            throw new IOException("Failed to delete product image: " + e.getMessage(), e);
        }
    }
    // End of profile photo upload

    //Start of store logo upload
     public String storeStoreLogo(MultipartFile file, String storeId) throws IOException {
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "store_logos",
                    "public_id", "store_" + storeId,
                    "overwrite", true,
                    "resource_type", "image");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Failed to upload store logo: " + e.getMessage(), e);
        }
    }

    public String storeProductImage(MultipartFile file, Long storeId, Long productId) throws IOException {
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "store_" + storeId + "/products",
                    "public_id", "product_" + productId,
                    "overwrite", true,
                    "resource_type", "image");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            throw new IOException("Failed to upload product image: " + e.getMessage(), e);
        }
    }

    public void deleteStoreImage(Long storeId) throws IOException {
        try {
            String publicId = "store_logos/store_" + storeId;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete store image: " + e.getMessage(), e);
        }
    }
    //End of store logo upload

     public void deleteProductImage(Long storeId, Long productId) throws IOException {
        try {
            String publicId = "store_" + storeId + "/products/product_" + productId;
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete product image: " + e.getMessage(), e);
        }
    }

    public void deleteAllStoreImages(Long storeId) throws IOException {
        try {
            // Delete store logo
            deleteStoreImage(storeId);
            
           
        } catch (Exception e) {
            throw new IOException("Failed to delete store images: " + e.getMessage(), e);
        }
    }
}
