package com.example.This_App_Backend.dto.DriversDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDTO {

    private Long driverId;
    private String driverName;
    private String phoneNumber;
    private String vehicleInfo;
}
