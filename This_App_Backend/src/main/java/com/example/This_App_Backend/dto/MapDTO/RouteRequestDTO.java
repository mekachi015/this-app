package com.example.This_App_Backend.dto.MapDTO;

import lombok.Data;

@Data
public class RouteRequestDTO {
    private double originLat;
    private double originLng;
    private String originAddress;

    private double destLat;
    private double destLng;
    private String destAddress;
}
