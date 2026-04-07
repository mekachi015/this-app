package com.example.This_App_Backend.testController;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.ProductsControllerRedefined;
import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.ProductsRedefined;
import com.example.This_App_Backend.service.UserService;

@WebMvcTest(ProductsControllerRedefined.class)
@AutoConfigureMockMvc
public class ProductsRedefineControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private ProductsRedefined productsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(1L);
        mockUser.setUsername("testadmin");
    }

    // ---------- POST /api/products/redefine/post/products ----------
    @Test
    void createProduct_success() throws Exception {
        when(userService.getUserByUsername("testadmin")).thenReturn(Optional.of(mockUser));

        MockMultipartFile logoFile = new MockMultipartFile(
                "logoFile", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        Products mockProduct = new Products();
        // You are setting 'productId' here
        mockProduct.setProductId(101L);

        when(productsService.createProduct(anyLong(), any(ProductsDTO.class), anyLong(), any()))
                .thenReturn(mockProduct);

        mockMvc.perform(multipart("/api/products/redefine/post/products")
                .file(logoFile)
                .param("storeId", "1")
                .param("productName", "Cool Shoes")
                .with(user("testadmin"))
                .with(csrf()))
                .andExpect(status().isCreated())
                // Change $.id to $.productId
                .andExpect(jsonPath("$.productId").value(101));
    }

    @Test
    void createProduct_invalidFileType() throws Exception {
        when(userService.getUserByUsername("testadmin")).thenReturn(Optional.of(mockUser));

        MockMultipartFile textFile = new MockMultipartFile(
                "logoFile", "test.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());

        mockMvc.perform(multipart("/api/products/redefine/post/products")
                .file(textFile)
                .param("storeId", "1")
                .with(user("testadmin"))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only image files are allowed for the product photo"));
    }

    // ---------- PUT /api/products/redefine/{productId} ----------
    @Test
    void updateProduct_success() throws Exception {
        when(userService.getUserByUsername("testadmin")).thenReturn(Optional.of(mockUser));

        Products updatedProduct = new Products();
        updatedProduct.setProductId(1L);

        when(productsService.updateProduct(anyLong(), any(ProductsDTO.class), anyLong(), any()))
                .thenReturn(updatedProduct);

        // For PUT with Multipart, we use multipart() and specify the method as PUT
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/products/redefine/1")
                .param("productName", "Updated Name")
                .with(user("testadmin"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // ---------- DELETE /api/products/redefine/{productId} ----------
    @Test
    void deleteProduct_success() throws Exception {
        when(userService.getUserByUsername("testadmin")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(delete("/api/products/redefine/1")
                .with(user("testadmin"))
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_notFound() throws Exception {
        when(userService.getUserByUsername("testadmin")).thenReturn(Optional.of(mockUser));

        // Correct syntax for void methods
        Mockito.doThrow(new IllegalArgumentException("PRODUCT_NOT_FOUND"))
                .when(productsService).deleteProduct(anyLong(), anyLong());

        mockMvc.perform(delete("/api/products/redefine/99")
                .with(user("testadmin"))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    // ---------- GET /api/products/redefine/search ----------
    @Test
    @WithMockUser
    void searchProducts_success() throws Exception {
        ProductsDTO dto = new ProductsDTO();
        dto.setProductName("Laptop");

        when(productsService.searchProducts("lap")).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/products/redefine/search")
                .param("q", "lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Laptop"));
    }

    // ---------- GET /api/products/redefine/stores/{storesId}/products/count
    // ----------
    @Test
    @WithMockUser
    void getProductsCount_success() throws Exception {
        when(productsService.getProductsCountByStore(1L)).thenReturn(25L);

        mockMvc.perform(get("/api/products/redefine/stores/1/products/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(25))
                .andExpect(jsonPath("$.storeId").value(1));
    }
}
