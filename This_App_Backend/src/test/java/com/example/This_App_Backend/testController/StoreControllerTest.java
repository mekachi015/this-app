package com.example.This_App_Backend.testController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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

import com.example.This_App_Backend.controller.StoreController;
import com.example.This_App_Backend.dto.StoresDTO.StoreDTO;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.OrderService;
import com.example.This_App_Backend.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.With;

@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc
public class StoreControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private StoreDTO sampleStoreDTO;
    private Stores sampleStoreEntity;

    @BeforeEach
    void setUp() {
        sampleStoreDTO = new StoreDTO();
        sampleStoreDTO.setStoreId(1L);
        sampleStoreDTO.setStoreName("Tech Haven");

        sampleStoreEntity = new Stores();
        sampleStoreEntity.setStoreId(1L);
        sampleStoreEntity.setStoreName("Tech Haven");
    }

    // ---------- POST /api/stores (Create Store) ----------
    @Test
    void createStore_success() throws Exception {
        // Mocking behavior
        when(storeService.createStore(any(StoreDTO.class), eq("testuser"), any())).thenReturn(sampleStoreEntity);
        when(storeService.convertToDTO(any(Stores.class))).thenReturn(sampleStoreDTO);

        // Prepare Multi-part data
        // 1. The JSON DTO part
        MockMultipartFile storeDataPart = new MockMultipartFile(
                "storeData", "", "application/json", objectMapper.writeValueAsBytes(sampleStoreDTO));

        // 2. The File part
        MockMultipartFile logoPart = new MockMultipartFile(
                "logoFile", "logo.png", MediaType.IMAGE_PNG_VALUE, "image content".getBytes());

        mockMvc.perform(multipart("/api/stores")
                .file(storeDataPart)
                .file(logoPart)
                .with(user("testuser"))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeName").value("Tech Haven"));
    }

    // ---------- GET /api/stores/my-stores ----------
    @Test
    void getMyStores_success() throws Exception {
        when(storeService.getMyStores("testuser")).thenReturn(List.of(sampleStoreEntity));
        when(storeService.convertToDTO(any())).thenReturn(sampleStoreDTO);

        mockMvc.perform(get("/api/stores/my-stores")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storeName").value("Tech Haven"));
    }

    // ---------- PUT /api/stores/{id} ----------
    @Test
    void updateStore_success() throws Exception {
        when(storeService.updateStore(eq(1L), any(StoreDTO.class), eq("testuser"))).thenReturn(sampleStoreEntity);
        when(storeService.convertToDTO(any())).thenReturn(sampleStoreDTO);

        mockMvc.perform(put("/api/stores/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleStoreDTO))
                .with(user("testuser"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Tech Haven"));
    }

    // ---------- DELETE /api/stores/{id} ----------
    @Test
    void deleteStore_success() throws Exception {
        doNothing().when(storeService).deleteStore(1L, "testuser");

        mockMvc.perform(delete("/api/stores/1")
                .with(user("testuser"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Store deleted successfully"));
    }

    // ---------- GET /api/stores/public (Public Access) ----------
    @Test
    @org.springframework.security.test.context.support.WithMockUser // Still good practice in WebMvcTest
    void getAllStores_success() throws Exception {
        when(storeService.getAllStores()).thenReturn(List.of(sampleStoreEntity));
        when(storeService.convertToDTO(any())).thenReturn(sampleStoreDTO);

        mockMvc.perform(get("/api/stores/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storeName").value("Tech Haven"));
    }

    // ---------- GET /api/stores/search ----------
    @Test
    @WithMockUser
    void searchStores_success() throws Exception {
        when(storeService.searchStores("Tech")).thenReturn(List.of(sampleStoreEntity));
        when(storeService.convertToDTO(any())).thenReturn(sampleStoreDTO);

        mockMvc.perform(get("/api/stores/search")
                .param("q", "Tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storeName").value("Tech Haven"));
    }

    @Test
    @WithMockUser
    void getStoreById_notFound() throws Exception {
        when(storeService.getStoreById(99L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/stores/public/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Store not found")));
    }
}
