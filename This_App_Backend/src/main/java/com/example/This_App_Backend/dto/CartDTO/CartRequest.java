package com.example.This_App_Backend.dto.CartDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    private Long productId;
    private Long quantity;
}
