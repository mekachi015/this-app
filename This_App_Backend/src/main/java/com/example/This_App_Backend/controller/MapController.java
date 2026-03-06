package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.MapDTO.RouteRequestDTO;
import com.example.This_App_Backend.dto.MapDTO.RouteResponseDTO;
import com.example.This_App_Backend.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/map")
//@CrossOrigin(origins = "*")
public class MapController {

    @Autowired
    private MapService mapService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${openrouteservice.api.key:eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjJlNTg1M2Q3ZDBmMzRmZmNiM2Q4ZjUxN2IzZTgwM2ZhIiwiaCI6Im11cm11cjY0In0=}")
    private String orsApiKey;

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

    /**
     * Multi-stop routing endpoint for orders with multiple pickup locations.
     * Accepts pre-geocoded store coordinates + delivery coordinates,
     * calls OpenRouteService with all waypoints, and returns the optimized route.
     */
    @PostMapping("/route/multi-stop")
    public ResponseEntity<?> getMultiStopRoute(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Double>> storeCoords = (List<Map<String, Double>>) request.get("storeCoordinates");
            @SuppressWarnings("unchecked")
            Map<String, Double> deliveryCoords = (Map<String, Double>) request.get("deliveryCoordinates");
            @SuppressWarnings("unchecked")
            List<String> storeAddresses = (List<String>) request.get("storeAddresses");
            String deliveryAddress = (String) request.get("deliveryAddress");

            // Build coordinates array: all stores + delivery at the end
            double[][] coordinates = new double[storeCoords.size() + 1][2];
            for (int i = 0; i < storeCoords.size(); i++) {
                coordinates[i][0] = storeCoords.get(i).get("lng");
                coordinates[i][1] = storeCoords.get(i).get("lat");
            }
            coordinates[storeCoords.size()][0] = deliveryCoords.get("lng");
            coordinates[storeCoords.size()][1] = deliveryCoords.get("lat");

            // Call OpenRouteService directions API with waypoints
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", orsApiKey);
            headers.set("Content-Type", "application/json");

            Map<String, Object> orsBody = Map.of("coordinates", coordinates);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(orsBody, headers);

            ResponseEntity<Map<String, Object>> orsResponse = restTemplate.exchange(
                    "https://api.openrouteservice.org/v2/directions/driving-car/geojson",
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>)(Class<?>)Map.class
            );

            Map<String, Object> orsData = orsResponse.getBody();
            if (orsData == null) {
                return ResponseEntity.status(500).body(Map.of("error", "No response from routing service"));
            }

            // Extract geometry and summary
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> features = (List<Map<String, Object>>) orsData.get("features");
            Map<String, Object> feature = features.get(0);
            Map<String, Object> geometry = (Map<String, Object>) feature.get("geometry");
            Map<String, Object> properties = (Map<String, Object>) feature.get("properties");
            Map<String, Object> summary = (Map<String, Object>) properties.get("summary");
            
            // Extract segments for color differentiation
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> segments = (List<Map<String, Object>>) properties.get("segments");

            double durationSeconds = ((Number) summary.get("duration")).doubleValue();
            double distanceMeters = ((Number) summary.get("distance")).doubleValue();

            // Format ETA
            int minutes = (int) Math.round(durationSeconds / 60);
            double km = distanceMeters / 1000;
            String eta;
            if (minutes < 60) {
                eta = String.format("%d min away · %.1f km", minutes, km);
            } else {
                int hours = minutes / 60;
                int remainingMins = minutes % 60;
                eta = String.format("%dh %dmin away · %.1f km", hours, remainingMins, km);
            }

            // Build response
            Map<String, Object> response = Map.of(
                    "storeCoordinates", storeCoords,
                    "deliveryCoordinates", deliveryCoords,
                    "storeAddresses", storeAddresses,
                    "deliveryAddress", deliveryAddress,
                    "geometry", geometry,
                    "segments", segments,
                    "eta", eta,
                    "durationSeconds", durationSeconds,
                    "distanceKm", km
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Multi-stop routing failed: " + e.getMessage()));
        }
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
