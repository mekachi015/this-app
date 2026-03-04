package com.example.This_App_Backend.dto.CartDTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Data
@Getter
@Setter
public class ProductResponse {
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String productDescription;
    private String imageUrl;
}
