package com.example.This_App_Backend.dto.StoresDTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductsDTO {
     private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private String category;
    private String imageUrl;
    private Integer stockQuantity;
    private Long storeId;
    //private Long ownerId;
    private Long userId;
}
