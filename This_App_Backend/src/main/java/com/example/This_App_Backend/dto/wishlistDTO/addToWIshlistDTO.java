package com.example.This_App_Backend.dto.wishlistDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class addToWIshlistDTO {
    private Long productId;
    private Long storeId;
}
