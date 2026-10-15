package com.example.This_App_Backend.dto.CheckoutDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutInitiateRequest {
    private Long deliveryAddress; // falls back to default address
}
