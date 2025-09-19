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
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
