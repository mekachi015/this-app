package com.example.This_App_Backend.testController;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.controller.DriverOrderController;
import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtRequestFilter;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.DriverOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverOrderController.class)
@AutoConfigureMockMvc(addFilters = false) // Filters disabled to bypass JWT chain complexities in unit tests
public class DriverOrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private DriverOrderService driverOrderService;

        // Security Mocks to satisfy the ApplicationContext
        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private JwtRequestFilter jwtRequestFilter;

        private OrderDTO testOrderDTO;
        private List<OrderDTO> testOrderList;

        @BeforeEach
        void setUp() {
                testOrderDTO = new OrderDTO();
                testOrderDTO.setOrderId(100L);
                testOrderDTO.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
                testOrderDTO.setTotalAmount(new java.math.BigDecimal("120.00"));
                testOrderList = List.of(testOrderDTO);
        }

        @BeforeEach
        void setUpMocks() throws Exception {
                // Make JwtRequestFilter a no-op: forward the request
                doAnswer(invocation -> {
                        HttpServletRequest request = invocation.getArgument(0);
                        HttpServletResponse response = invocation.getArgument(1);
                        FilterChain chain = invocation.getArgument(2);
                        chain.doFilter(request, response);
                        return null;
                }).when(jwtRequestFilter).doFilterInternal(any(), any(), any());
        }

        @Test
        @WithMockUser(username = "driver1", roles = "DRIVER")
        void updateOrderStatus_success() throws Exception {

                when(driverOrderService.updateOrderStatus(1L, 100L, "DELIVERED"))
                                .thenReturn(testOrderDTO);

                mockMvc.perform(patch("/api/driver/1/orders/100/status")
                                .param("status", "DELIVERED")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(content().json(objectMapper.writeValueAsString(testOrderDTO)));
        }

        @Test
        @WithMockUser(username = "driver1", roles = "DRIVER")
        void updateOrderStatus_serviceThrowsException_returnsBadRequest() throws Exception {
                // 1. Mock the service
                when(driverOrderService.updateOrderStatus(1L, 100L, "DELIVERED"))
                                .thenThrow(new RuntimeException("Invalid status transition"));

                // 2. Perform request
                mockMvc.perform(patch("/api/driver/1/orders/100/status")
                                .param("status", "DELIVERED")
                                .with(user("driver1").roles("DRIVER")) // Populates SecurityContextHolder
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid status transition"));
        }

        @Test
        void updateOrderStatus_unauthenticated_returnsUnauthorized() throws Exception {
                // No .with(user()) call here simulates the null Authentication object
                mockMvc.perform(patch("/api/driver/1/orders/100/status")
                                .param("status", "DELIVERED")
                                .with(csrf()))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("User must be logged in"));

                verify(driverOrderService, never()).updateOrderStatus(anyLong(), anyLong(), anyString());
        }

        @Test
        void getAvailableOrders_success() throws Exception {
                when(driverOrderService.getAvailableOrder(1L)).thenReturn(testOrderList);

                mockMvc.perform(get("/api/driver/1/orders/available"))
                                .andExpect(status().isOk())
                                .andExpect(content().json(objectMapper.writeValueAsString(testOrderList)));
        }
}
