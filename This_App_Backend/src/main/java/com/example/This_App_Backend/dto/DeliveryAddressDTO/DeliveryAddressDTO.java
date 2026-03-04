package com.example.This_App_Backend.dto.DeliveryAddressDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAddressDTO {
  private Long addressId;
  private String StreetNumber;
  private String StreetName;
  private String Suburb;
  private String City;
  private String province;
  private String postalCode;
}
