package com.example.This_App_Backend.testController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.ProfileController;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.UserService;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc
public class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");
        mockUser.setProfilePhotoUrl("http://images.com/old.jpg");
    }

    // ---------- POST /api/profile/upload-photo ----------
    @Test
    void uploadProfilePhoto_success() throws Exception {
        // 1. Mock the user lookup
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(mockUser));
        
        // 2. Mock the file storage result
        String newUrl = "http://localhost:8080/api/profile/files/profile-photos/abc.jpg";
        when(fileStorageService.storeProfilePhoto(any(), anyLong())).thenReturn(newUrl);

        // 3. Create a mock image file
        MockMultipartFile file = new MockMultipartFile(
                "profilePhoto", 
                "avatar.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/profile/upload-photo")
                        .file(file)
                        .with(user("testuser")) // Mocks Authentication object
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value(newUrl));
    }

    @Test
    void uploadProfilePhoto_invalidType() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "profilePhoto", "test.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());

        mockMvc.perform(multipart("/api/profile/upload-photo")
                        .file(textFile)
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only image files are allowed"));
    }

    // ---------- GET /api/profile/me ----------
    @Test
    void getCurrentUser_success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/profile/me")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getCurrentUser_notFound() throws Exception {
        when(userService.getUserByUsername("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/profile/me")
                        .with(user("unknown")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ---------- GET /api/profile/files/profile-photos/{filename} ----------
    @Test
    @WithMockUser(username = "testuser")
    void serveFile_notFound() throws Exception {
        // Testing the 404 block of the file server
        mockMvc.perform(get("/api/profile/files/profile-photos/nonexistent.jpg"))
                .andExpect(status().isNotFound());
    }
}
