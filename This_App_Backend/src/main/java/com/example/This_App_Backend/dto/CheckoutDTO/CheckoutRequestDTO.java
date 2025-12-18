package com.example.This_App_Backend.dto.CheckoutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {

    private Long deliveryAddressId;
    private String specialInstructions;

}
