package com.example.This_App_Backend.dto.CartDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponse {
    private Long cartId;

    private Long quantity;

    private ProductResponse productResponse;
}
