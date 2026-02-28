package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.MapDTO.RouteRequestDTO;
import com.example.This_App_Backend.dto.MapDTO.RouteResponseDTO;
import com.example.This_App_Backend.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/map")
//@CrossOrigin(origins = "*")
public class MapController {

    @Autowired
    private MapService mapService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Accepts pre-geocoded coordinates from the frontend.
     * The Angular service geocodes via Photon (browser) then posts here,
     * so the backend only needs to call ORS routing — no server-side geocoding.
     */
    @PostMapping("/route/by-coords")
    public ResponseEntity<RouteResponseDTO> getRouteByCoords(
            @RequestBody RouteRequestDTO request
    ) {
        RouteResponseDTO response = mapService.getRouteByCoords(request);
        return ResponseEntity.ok(response);
    }

    // Returns store coordinates, delivery coords and ETA by looking up the order in the DB.
    // Requires server-side geocoding — use /route/by-coords instead if geocoding fails.
    @GetMapping("/route/{orderId}")
    public ResponseEntity<RouteResponseDTO> getOrderRoute(
            @PathVariable Long orderId
    ) {
        RouteResponseDTO response = mapService.getRouteForOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-address")
    public ResponseEntity<String> testAddress(@RequestParam(defaultValue = "73 Juta Street Braamfontein Johannesburg South Africa") String q) {
        try {
            String url = "https://photon.komoot.io/api/?q=" + java.net.URLEncoder.encode(q, "UTF-8") + "&limit=3";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ThisAppBackend/1.0");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug-nominatim")
    public ResponseEntity<String> debugNominatim(@RequestParam String q) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("q", q)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .queryParam("countrycodes", "za")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "ThisAppBackend/1.0");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
