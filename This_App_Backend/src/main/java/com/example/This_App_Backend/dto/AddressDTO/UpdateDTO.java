package com.example.This_App_Backend.dto.AddressDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDTO {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    private String city;
    private String province;
    private String postalCode;

    private String addressType;
    private Boolean isDefault;
}
