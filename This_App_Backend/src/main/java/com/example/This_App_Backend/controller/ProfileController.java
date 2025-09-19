package com.example.This_App_Backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.service.UserService;
import com.example.This_App_Backend.service.FileStorageService;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfileController {
    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("profilePhoto") MultipartFile file,
            Authentication authentication) {
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please select a file to upload"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only image files are allowed"));
            }

            // Validate file size (5MB max)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "File size must be less than 5MB"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }

            User user = userOpt.get();
            
            // Store the file and get URL
            String photoUrl = fileStorageService.storeProfilePhoto(file, user.getUserId());
            
            // Update user's profile photo URL
            user.setProfilePhotoUrl(photoUrl);
            userService.updateUser(user);

            return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to upload photo: " + e.getMessage()));
        }
    }

    @GetMapping("/files/profile-photos/{filename}")
public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
    try {
        Path file = Paths.get("uploads/profile-photos/").resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (Exception e) {
        return ResponseEntity.notFound().build();
    }
}
}
