package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.MapDTO.RouteResponseDTO;
import com.example.This_App_Backend.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/map")
//@CrossOrigin(origins = "*")
public class MapController {

    @Autowired
    private MapService mapService;

    @Autowired
    private RestTemplate restTemplate;

    //Returns store coordinates - origins , delivery coords and destination and eta
    @GetMapping("/route/{orderId}")
    public ResponseEntity<RouteResponseDTO> getOrderRoute(
            @PathVariable Long orderId
    ) {
        RouteResponseDTO response = mapService.getRouteForOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-geocode")
    public ResponseEntity<String> testGeocode() {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q=Johannesburg+South+Africa&format=json&limit=1&countrycodes=za";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ThisAppBackend/1.0 (test)");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }


    }
}
