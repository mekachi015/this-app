package com.example.This_App_Backend.dto.CheckoutDTO;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutInitiateResponse {
    private String pendingCheckoutId;
    private String paymentUrl; // full payfaast redirect URL
    private BigDecimal totalAmount;
    private String storeName;
    private BigDecimal shippingAmount;
}
