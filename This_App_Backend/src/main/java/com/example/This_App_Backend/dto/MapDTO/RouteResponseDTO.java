package com.example.This_App_Backend.dto.MapDTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RouteResponseDTO {

    private double originLat;
    private double originLng;
    private String originAddress;

    private double destinationLat;
    private double destinationLng;
    private String destinationAddress;

    private String eta;
    private double distanceKm;
    private long durationSeconds;
}
