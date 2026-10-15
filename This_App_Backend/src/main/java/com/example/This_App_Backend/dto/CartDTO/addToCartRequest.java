package com.example.This_App_Backend.dto.CartDTO;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class addToCartRequest {
    private Long productId;
    private Long quantity;
}
