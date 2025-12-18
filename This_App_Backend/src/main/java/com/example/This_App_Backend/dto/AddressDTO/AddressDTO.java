package com.example.This_App_Backend.dto.AddressDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long addressId;
    private Long userId;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    private String city;
    private String province;
    private String postalCode;
    private String addressType;
    private Boolean isDefault;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
