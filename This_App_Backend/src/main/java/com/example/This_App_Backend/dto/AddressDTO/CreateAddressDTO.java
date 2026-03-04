package com.example.This_App_Backend.dto.AddressDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAddressDTO {
    private String streetNumber;
    private String streetName;
    private String suburb;

    private String city;
    private String province;
    private String postalCode;
    private String addressType;
    private Boolean isDefault;
}
