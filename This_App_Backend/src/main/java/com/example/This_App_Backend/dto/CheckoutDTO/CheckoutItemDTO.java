package com.example.This_App_Backend.dto.CheckoutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutItemDTO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private Long quantity;
    private BigDecimal subTotal;
}
