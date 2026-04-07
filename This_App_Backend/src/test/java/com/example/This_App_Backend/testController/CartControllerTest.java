package com.example.This_App_Backend.testController; // Fixed 'fixpackage' typo

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.controller.CartContoller;
import com.example.This_App_Backend.dto.CartDTO.CartDto;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtRequestFilter;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartContoller.class)
//@AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil; // Or whatever your JWT service is named

    // @MockBean
    // private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private CartDto testCartDto;
    private CustomerOrders testOrder1;
    private CustomerOrders testOrder2;
    private User_Addresses testAddress;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        testCartDto = new CartDto();
        testCartDto.setCartItemId(100L);
        testCartDto.setProductId(200L);
        testCartDto.setProductName("Test Product");
        testCartDto.setQuantity(2L);
        testCartDto.setProductPrice(19.99); // Simplified double initialization
        testCartDto.setSubtotal(39.98);

        Stores store = new Stores();
        store.setStoreId(10L);
        store.setStoreName("Test Store");

        testAddress = new User_Addresses();
        testAddress.setAddressId(500L);
        testAddress.setStreetNumber("10");
        testAddress.setStreetName("Main St");
        testAddress.setSuburb("Downtown");
        testAddress.setCity("Metropolis");
        testAddress.setProvince("State");
        testAddress.setPostalCode("12345");

        testOrder1 = new CustomerOrders();
        testOrder1.setOrderId(1000L);
        testOrder1.setStore(store);
        testOrder1.setTotalAmount(new BigDecimal("39.98"));
        testOrder1.setShippingAmount(new BigDecimal("5.00"));
        testOrder1.setOrderStatus(OrderStatus.PENDING);
        testOrder1.setOrderDate(LocalDateTime.now());
        testOrder1.setDeliveryAddress(testAddress);
        testOrder1.setOrderItems(List.of(new Order_Items()));

        testOrder2 = new CustomerOrders();
        testOrder2.setOrderId(1001L);
        testOrder2.setStore(store);
        testOrder2.setTotalAmount(new BigDecimal("29.99"));
        testOrder2.setShippingAmount(new BigDecimal("3.00"));
        testOrder2.setOrderStatus(OrderStatus.PENDING);
        testOrder2.setOrderDate(LocalDateTime.now());
        testOrder2.setDeliveryAddress(testAddress);
        testOrder2.setOrderItems(List.of(new Order_Items()));
    }

//     @BeforeEach
// void setUpFilter() throws Exception {
//     // Make the filter pass through without any action
//     doAnswer(invocation -> {
//         var chain = invocation.getArgument(2, FilterChain.class);
//         chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
//         return null;
//     }).when(jwtRequestFilter).doFilterInternal(any(), any(), any());
// }

    @Test
    @WithMockUser(username = "testuser")
    void addToCart_success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cartService.addToCart(eq(1L), eq(200L), eq(2L))).thenReturn(testCartDto);

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "1")
                .param("productId", "200")
                .param("quantity", "2")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added to cart successfully"))
                .andExpect(jsonPath("$.data.cartItemId").value(100));

        verify(cartService).addToCart(1L, 200L, 2L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void addToCart_unauthorized_whenUserNotMatch() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/cart/add")
                .param("userId", "2") // ID 2 != testUser.id 1
                .param("productId", "200")
                .param("quantity", "1")
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized access"));

        verify(cartService, never()).addToCart(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void checkout_success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cartService.checkout(eq(1L), eq(500L))).thenReturn(List.of(testOrder1, testOrder2));

        mockMvc.perform(post("/api/cart/checkout")
                .param("userId", "1")
                .param("deliveryAddressId", "500")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.orderCount").value(2))
                .andExpect(jsonPath("$.orders[0].orderId").value(1000));

        verify(cartService).checkout(1L, 500L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserCart_success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cartService.getUserCart(1L)).thenReturn(List.of(testCartDto));

        mockMvc.perform(get("/api/cart/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].cartItemId").value(100))
                .andExpect(jsonPath("$.total").value(39.98));

        verify(cartService).getUserCart(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeFromCart_success() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(cartService).removeFromCart(1L, 100L);

        mockMvc.perform(delete("/api/cart/{cartItemId}", 100L)
                .param("userId", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item removed from cart successfully"));

        verify(cartService).removeFromCart(1L, 100L);
    }
}