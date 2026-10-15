package com.example.This_App_Backend.dto.paymentDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    private Long cartItemId;
    private Long productId;
    private Long quantity;
}
