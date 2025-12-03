package com.example.This_App_Backend.dto.wishlistDTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WishlistDto {
    private Long wishlistId;
    private Long userId;
    private Long productId;
    private String productName;
    private String productImage;
    private Double productPrice;
    private Long storeId;
    private String storeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
