package com.example.This_App_Backend.dto.StoresDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreDTO {

    private Long storeId;
    private String storeName;
    private String storeDescription;
    private String storeAddress;
    private String storeEmail;
    private String storePhoneNumber;
    private String storeBusinessHours;
    private String storeLogo;
    private Long ownerId;

}
