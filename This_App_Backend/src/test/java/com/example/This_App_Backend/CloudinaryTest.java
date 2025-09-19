package com.example.This_App_Backend;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cloudinary.Cloudinary;

@SpringBootTest
public class CloudinaryTest {
     @Autowired
    private Cloudinary cloudinary;
    
    @Test
    void testCloudinaryConnection() {
        try {
            cloudinary.api().ping(Map.of());
            System.out.println("Cloudinary connection successful");
        } catch (Exception e) {
            System.out.println("Cloudinary connection failed: " + e.getMessage());
        }
    }
}
