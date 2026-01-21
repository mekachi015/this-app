package com.example.This_App_Backend.dto.DeliveryAddressDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddressDTO {
  private Long addressId;
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String province;
  private String postalCode;
}
