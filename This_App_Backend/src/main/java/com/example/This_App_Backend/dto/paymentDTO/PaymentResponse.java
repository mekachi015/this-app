package com.example.This_App_Backend.dto.paymentDTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String clientSecret;
    private Long orderId;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String message;
}
