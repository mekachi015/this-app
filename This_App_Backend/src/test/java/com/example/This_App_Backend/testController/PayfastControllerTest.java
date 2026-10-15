package com.example.This_App_Backend.testController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.PayfastController;
import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutInitiateResponse;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.PayFastCheckoutService;

@WebMvcTest(PayfastController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = "platform.shipping.fee=50")
public class PayfastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayFastCheckoutService checkoutService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // ---------- GET /api/checkout/config ----------
    @Test
    @org.springframework.security.test.context.support.WithMockUser
    void getCheckoutConfig_success() throws Exception {
        mockMvc.perform(get("/api/checkout/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingFee").value(50.00));
    }

    // ---------- POST /api/checkout/initiate ----------
    @Test
    void initiateCheckout_success() throws Exception {
        // Mock the User for isUserAuthorized check
        User mockUser = new User();
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // Setup the DTO response
        CheckoutInitiateResponse response = new CheckoutInitiateResponse();
        response.setPaymentUrl("https://sandbox.payfast.co.za/eng/process?blah");
        response.setTotalAmount(new BigDecimal("150.00"));
        response.setStoreName("Test Store");

        when(checkoutService.initiateCheckout(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/api/checkout/initiate")
                        .param("userId", "1")
                        .param("deliveryAddressId", "10")
                        .with(user("testuser")) // Authentication.getName() returns "testuser"
                        .with(csrf()))          // Prevents 403 Forbidden
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").value("https://sandbox.payfast.co.za/eng/process?blah"))
                .andExpect(jsonPath("$.storeName").value("Test Store"));
    }

    @Test
    void initiateCheckout_forbidden() throws Exception {
        // Return a different user or empty to trigger the forbidden block
        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/checkout/initiate")
                        .param("userId", "1")
                        .with(user("wronguser"))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Forbidden"));
    }

    @Test
    void initiateCheckout_badRequest() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        when(checkoutService.initiateCheckout(anyLong(), any()))
                .thenThrow(new RuntimeException("Empty Cart"));

        mockMvc.perform(post("/api/checkout/initiate")
                        .param("userId", "1")
                        .with(user("testuser"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Empty Cart"));
    }

    // ---------- POST /api/checkout/payfast-notify ----------
    @Test
    @WithMockUser(username = "testuser")
    void payfastNotify_success() throws Exception {
        // No specific auth check in your controller for this endpoint
        mockMvc.perform(post("/api/checkout/payfast-notify")
                        .param("payment_status", "COMPLETE")
                        .param("m_payment_id", "order_123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
