package com.example.This_App_Backend.testController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.WalletController;
import com.example.This_App_Backend.dto.WalletDTO.WalletBalanceDTO;
import com.example.This_App_Backend.dto.WalletDTO.WalletTransactionDTO;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.WalletService;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc
public class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    // Security mocks required for the ApplicationContext to load
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    // ---------- GET /api/wallet/{userId}/balance ----------
    @Test
    @WithMockUser
    void getBalance_success() throws Exception {
        WalletBalanceDTO balanceDTO = new WalletBalanceDTO();
        balanceDTO.setBalance(new BigDecimal("250.75"));

        when(walletService.getBalance(1L)).thenReturn(balanceDTO);

        mockMvc.perform(get("/api/wallet/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250.75));
    }

    @Test
    @WithMockUser
    void getBalance_error() throws Exception {
        when(walletService.getBalance(99L)).thenThrow(new RuntimeException("Wallet not found"));

        mockMvc.perform(get("/api/wallet/99/balance"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Wallet not found"));
    }

    // ---------- GET /api/wallet/{userId}/transaction ----------
    @Test
    @WithMockUser
    void getTransactions_success() throws Exception {
        WalletTransactionDTO tx1 = new WalletTransactionDTO();
        tx1.setAmount(new BigDecimal("50.00"));
        tx1.setType("CREDIT");

        WalletTransactionDTO tx2 = new WalletTransactionDTO();
        tx2.setAmount(new BigDecimal("20.00"));
        tx2.setType("DEBIT");

        when(walletService.getTransactions(1L)).thenReturn(List.of(tx1, tx2));

        mockMvc.perform(get("/api/wallet/1/transaction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[1].type").value("DEBIT"));
    }

    @Test
    @WithMockUser
    void getTransactions_emptyList() throws Exception {
        when(walletService.getTransactions(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/wallet/1/transaction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
