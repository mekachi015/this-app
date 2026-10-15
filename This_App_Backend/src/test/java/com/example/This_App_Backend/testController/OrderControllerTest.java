package com.example.This_App_Backend.testController;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.controller.OrderController;
import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User.UserType;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    @MockBean
    private JwtUtil jwtUtil;

    private OrderDTO sampleOrderDTO;
    private List<OrderDTO> sampleOrderList;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setUserType(UserType.CUSTOMER);
        
        sampleOrderDTO = new OrderDTO();
        sampleOrderDTO.setOrderId(1L);
        sampleOrderDTO.setTotalAmount(new BigDecimal("120.00"));
        sampleOrderDTO.setOrderStatus(OrderStatus.PENDING); // ✅ Use enum

        sampleOrderList = List.of(sampleOrderDTO);
    }

    // ---------- GET /user/{userId} ----------
    @Test
    @WithMockUser
    void getUserOrders_success() throws Exception {
        when(orderService.getUserOrders(1L)).thenReturn(sampleOrderList);

        mockMvc.perform(get("/api/orders/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.data[0].orderId").value(1))
                .andExpect(jsonPath("$.data[0].orderStatus").value("PENDING")) // enum name
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    void getUserOrders_serviceThrowsException() throws Exception {
        when(orderService.getUserOrders(1L)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/orders/user/{userId}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ---------- GET /{orderId}?userId=... ----------
    @Test
    @WithMockUser
    void getOrderById_success() throws Exception {
        when(orderService.getOrderById(1L, 100L)).thenReturn(sampleOrderDTO);

        mockMvc.perform(get("/api/orders/{orderId}", 100L)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(1));
    }

    @Test
    @WithMockUser
    void getOrderById_serviceThrowsException() throws Exception {
        when(orderService.getOrderById(1L, 100L)).thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/orders/{orderId}", 100L)
                        .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

    // ---------- GET /user/{userId}/count ----------
    @Test
    @WithMockUser
    void getOrderCount_success() throws Exception {
        when(orderService.getOrderCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/orders/user/{userId}/count", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @WithMockUser
    void getOrderCount_serviceThrowsException() throws Exception {
        when(orderService.getOrderCount(1L)).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/orders/user/{userId}/count", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ---------- GET /{storeId}/orders ----------
    @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void getStoreOrders_success() throws Exception {
        when(orderService.getStoreOrders(10L)).thenReturn(sampleOrderList);

        mockMvc.perform(get("/api/orders/{storeId}/orders", 10L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(sampleOrderList)));
    }

    @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void getStoreOrders_serviceThrowsException() throws Exception {
        when(orderService.getStoreOrders(10L)).thenThrow(new RuntimeException("Store not found"));

        mockMvc.perform(get("/api/orders/{storeId}/orders", 10L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Store not found"));
    }

    // ---------- GET /{storeId}/orders/{orderId} ----------
    @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void getStoreOrderById_success() throws Exception {
        when(orderService.getStoreOrderById(10L, 100L)).thenReturn(sampleOrderDTO);

        mockMvc.perform(get("/api/orders/{storeId}/orders/{orderId}", 10L, 100L))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(sampleOrderDTO)));
    }

    @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void getStoreOrderById_serviceThrowsException() throws Exception {
        when(orderService.getStoreOrderById(10L, 100L)).thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/api/orders/{storeId}/orders/{orderId}", 10L, 100L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

    // ---------- GET /owner/{userId}/all ----------
    @Test
    @WithMockUser
    void getAllOrdersByUserId_success() throws Exception {
        when(orderService.getAllOrdersByUserId(1L)).thenReturn(sampleOrderList);

        mockMvc.perform(get("/api/orders/owner/{userId}/all", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.data[0].orderId").value(1))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    void getAllOrdersByUserId_userNotFound() throws Exception {
        when(orderService.getAllOrdersByUserId(1L)).thenThrow(new IllegalArgumentException("USER_NOT_FOUND"));

        mockMvc.perform(get("/api/orders/owner/{userId}/all", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser
    void getAllOrdersByUserId_notAdmin() throws Exception {
        when(orderService.getAllOrdersByUserId(1L)).thenThrow(new IllegalArgumentException("USER_NOT_ADMIN"));

        mockMvc.perform(get("/api/orders/owner/{userId}/all", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not an admin"));
    }

    // ---------- GET /owner/count/{userId} ----------
    @Test
    @WithMockUser
    void getOrderCountByOwner_success() throws Exception {
        when(orderService.getOrderCountByUserId(1L)).thenReturn(10L);

        mockMvc.perform(get("/api/orders/owner/count/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalOrders").value(10));
    }

    @Test
    @WithMockUser
    void getOrderCountByOwner_userNotFound() throws Exception {
        when(orderService.getOrderCountByUserId(1L)).thenThrow(new IllegalArgumentException("USER_NOT_FOUND"));

        mockMvc.perform(get("/api/orders/owner/count/{userId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser
    void getOrderCountByOwner_notAdmin() throws Exception {
        when(orderService.getOrderCountByUserId(1L)).thenThrow(new IllegalArgumentException("USER_NOT_ADMIN"));

        mockMvc.perform(get("/api/orders/owner/count/{userId}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not an admin"));
    }

    // ---------- PATCH /store/{storeId}/orders/{orderId}/status ----------
   @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void updateOrderStatus_success() throws Exception {
        OrderDTO updatedOrder = new OrderDTO();
        updatedOrder.setOrderId(100L);
        updatedOrder.setOrderStatus(OrderStatus.READY_FOR_DELIVERY);
        
        // Match username "storeowner" from @WithMockUser
        when(orderService.updateOrderStatus(eq(10L), eq(100L), eq("READY_FOR_DELIVERY"), eq("storeowner")))
                .thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/orders/store/{storeId}/orders/{orderId}/status", 10L, 100L)
                        .param("status", "READY_FOR_DELIVERY")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // ✅ Add CSRF
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"))
                .andExpect(jsonPath("$.order.orderId").value(100));
    }

   @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void updateOrderStatus_orderNotFound() throws Exception {
        when(orderService.updateOrderStatus(eq(10L), eq(100L), eq("READY_FOR_DELIVERY"), eq("storeowner")))
                .thenThrow(new IllegalArgumentException("ORDER_NOT_FOUND"));

        mockMvc.perform(patch("/api/orders/store/{storeId}/orders/{orderId}/status", 10L, 100L)
                        .param("status", "READY_FOR_DELIVERY")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // ✅ Add CSRF
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

   @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void updateOrderStatus_unauthorized() throws Exception {
        when(orderService.updateOrderStatus(eq(10L), eq(100L), eq("READY_FOR_DELIVERY"), eq("storeowner")))
                .thenThrow(new IllegalArgumentException("UNAUTHORIZED"));

        mockMvc.perform(patch("/api/orders/store/{storeId}/orders/{orderId}/status", 10L, 100L)
                        .param("status", "READY_FOR_DELIVERY")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // ✅ Add CSRF
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to update this order"));
    }

    @Test
    @WithMockUser(username = "user")
    void updateOrderStatus_invalidStatus() throws Exception {
        // Match the default "user" username from @WithMockUser
        when(orderService.updateOrderStatus(eq(10L), eq(100L), eq("INVALID"), eq("user")))
                .thenThrow(new IllegalArgumentException("INVALID_STATUS"));

        mockMvc.perform(patch("/api/orders/store/{storeId}/orders/{orderId}/status", 10L, 100L)
                        .param("status", "INVALID")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // ✅ Add CSRF
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid order status"));
    }

   @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void updateOrderStatus_invalidTransition() throws Exception {
        when(orderService.updateOrderStatus(eq(10L), eq(100L), eq("READY_FOR_DELIVERY"), eq("storeowner")))
                .thenThrow(new IllegalArgumentException("INVALID_STATUS_TRANSITION"));

        mockMvc.perform(patch("/api/orders/store/{storeId}/orders/{orderId}/status", 10L, 100L)
                        .param("status", "READY_FOR_DELIVERY")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // ✅ Add CSRF
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Can only mark PENDING orders as READY_FOR_DELIVERY"));
    }

    @Test
    @WithMockUser(username = "storeowner", roles = "ADMIN")
    void updateOrderStatus_genericException() throws Exception {
        when(orderService.updateOrderStatus(eq(10L), eq(100L), eq("READY_FOR_DELIVERY"), eq("storeowner")))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(patch("/api/orders/store/{storeId}/orders/{orderId}/status", 10L, 100L)
                        .param("status", "READY_FOR_DELIVERY")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())) // ✅ Add CSRF
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An error occurred: Database error"));
    }
}
