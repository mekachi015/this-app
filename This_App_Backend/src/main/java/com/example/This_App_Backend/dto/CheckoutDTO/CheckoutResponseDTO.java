package com.example.This_App_Backend.dto.CheckoutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private Boolean success;
    private String message;
    private Integer totalOrders;
    private BigDecimal grandTotal;
    private List<OrderSummaryDTO> orders;
}
