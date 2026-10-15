package com.example.This_App_Backend.testController;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.WishlistController;
import com.example.This_App_Backend.dto.wishlistDTO.WishlistDto;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.WishlistService;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc
public class WishlistControllerTest {
@Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private UserRepository userRepo;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser;
    private WishlistDto mockWishlistDto;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("testuser");

        mockWishlistDto = new WishlistDto();
        // Set fields for your WishlistDto as needed
    }

    // Helper to mock the private isUserAuthorized check
    private void mockAuthorization(boolean authorized) {
        if (authorized) {
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        } else {
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        }
    }

    // ---------- POST /api/wishlist/add/product ----------
    @Test
    void addProductToWishlist_success() throws Exception {
        mockAuthorization(true);
        when(wishlistService.addProductToWishlist(1L, 100L)).thenReturn(mockWishlistDto);

        mockMvc.perform(post("/api/wishlist/add/product")
                        .param("userId", "1")
                        .param("productId", "100")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added to wishlist successfully"));
    }

    @Test
    void addProductToWishlist_forbidden() throws Exception {
        // Mocking as a different user to trigger "Unauthorized access"
        mockAuthorization(false); 

        mockMvc.perform(post("/api/wishlist/add/product")
                        .param("userId", "1")
                        .param("productId", "100")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized access"));
    }

    // ---------- GET /api/wishlist/{userId} ----------
    @Test
    void getUserWishlist_success() throws Exception {
        mockAuthorization(true);
        when(wishlistService.getUserWishlist(1L)).thenReturn(List.of(mockWishlistDto));

        mockMvc.perform(get("/api/wishlist/1")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.itemCount").value(1));
    }

    // ---------- DELETE /api/wishlist/clear/{userId} ----------
    @Test
    void clearWishlist_success() throws Exception {
        mockAuthorization(true);
        doNothing().when(wishlistService).clearWishlist(1L);

        mockMvc.perform(delete("/api/wishlist/clear/1")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Wishlist cleared successfully"));
    }

    // ---------- GET /api/wishlist/check/product ----------
    @Test
    void isProductInWishlist_true() throws Exception {
        mockAuthorization(true);
        when(wishlistService.isProductInWishlist(1L, 100L)).thenReturn(true);

        mockMvc.perform(get("/api/wishlist/check/product")
                        .param("userId", "1")
                        .param("productId", "100")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isInWishlist").value(true));
    }

    // ---------- Test Unauthenticated Access ----------
    @Test
    void getUserWishlist_unauthorized() throws Exception {
        // Not providing .with(user(...))
        mockMvc.perform(get("/api/wishlist/1"))
                .andExpect(status().isUnauthorized());
    }
}
