package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.MapDTO.RouteResponseDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class MapService {

    @Value("${ors.api.key}")
    private String orsApiKey;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private RestTemplate restTemplate;

    public RouteResponseDTO getRouteForOrder(Long orderId) {

        // Load the order
        CustomerOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        Stores store = order.getStore();
        User_Addresses delivery = order.getDeliveryAddress();

        if (store == null) throw new RuntimeException("Store Not Found");
        if (delivery == null) throw new RuntimeException("Delivery Address Not Found");

        // ✅ CHANGED: use buildStoreQuery() to strip postal code before geocoding
        String storeQuery = buildStoreQuery(store.getStoreAddress());
        double[] storeCoords = geocodeWithFallback(storeQuery);

        // ✅ CHANGED: use geocodeWithFallback() instead of geocode()
        //             also fixed: was using getStreetName() twice, now uses getStreetNumber()
        String deliveryQuery = delivery.getStreetNumber() + " " + delivery.getStreetName()
                + " " + delivery.getSuburb() + " " + delivery.getCity();
        double[] deliveryCoords = geocodeWithFallback(deliveryQuery);

        return fetchRoute(storeCoords, deliveryCoords, store.getStoreAddress(),
                delivery.getStreetName() + ", " + delivery.getCity());
    }

    // ✅ NEW: strips trailing postal codes from store addresses
    // e.g. "73 Juta St, Braamfontein, Johannesburg, 2001" → "73 Juta St, Braamfontein, Johannesburg"
    private String buildStoreQuery(String storeAddress) {
        if (storeAddress == null) return "";
        return storeAddress.replaceAll(",?\\s*\\d{4,5}\\s*$", "").trim();
    }

    // ✅ NEW: retries geocoding with progressively simpler queries if Photon returns no results
    // Attempt 1: full address + "South Africa"
    // Attempt 2: drop street number/name, keep suburb + city
    // Attempt 3: city only (last resort)
    private double[] geocodeWithFallback(String query) {
        // Attempt 1: full query
        double[] result = tryGeocode(query + " South Africa");
        if (result != null) return result;

        // Attempt 2: drop the first comma-separated part (usually street)
        String[] parts = query.split(",");
        if (parts.length > 1) {
            String simplified = String.join(",",
                    Arrays.copyOfRange(parts, 1, parts.length)).trim();
            result = tryGeocode(simplified + " South Africa");
            if (result != null) return result;
        }

        // Attempt 3: city only
        String cityOnly = parts[parts.length - 1].trim();
        result = tryGeocode(cityOnly + " South Africa");
        if (result != null) return result;

        throw new RuntimeException(
                "Could not geocode address after multiple attempts: \"" + query + "\"");
    }

    // ✅ NEW: replaces the old geocode() method — returns null instead of throwing
    //         so geocodeWithFallback() can try the next simplification
    private double[] tryGeocode(String query) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://photon.komoot.io/api/")
                    .queryParam("q", query)
                    .queryParam("limit", 1)
                    .queryParam("lang", "en")
                    .toUriString();

            System.out.println("Geocoding attempt: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (compatible; ThisAppBackend/1.0)");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> result = response.getBody();
            if (result == null) return null;

            List<Map<String, Object>> features =
                    (List<Map<String, Object>>) result.get("features");
            if (features == null || features.isEmpty()) return null;

            Map<String, Object> geometry =
                    (Map<String, Object>) features.get(0).get("geometry");
            List<Double> coordinates = (List<Double>) geometry.get("coordinates");

            // Photon returns [lng, lat] — we store as [lat, lng]
            double lng = coordinates.get(0);
            double lat = coordinates.get(1);
            return new double[]{lat, lng};

        } catch (Exception e) {
            System.err.println("Geocode attempt failed for: " + query + " — " + e.getMessage());
            return null;
        }
    }

    // ✅ UNCHANGED: fetchRoute() is exactly as before
    private RouteResponseDTO fetchRoute(double[] from, double[] to,
                                        String originAddress, String destAddress) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", orsApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ORS expects coordinates as [lng, lat]
        Map<String, Object> body = Map.of(
                "coordinates", List.of(
                        List.of(from[1], from[0]),  // store  [lng, lat]
                        List.of(to[1], to[0])        // delivery [lng, lat]
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> data = response.getBody();
        if (data == null) throw new RuntimeException("ORS returned null response");

        List<Map<String, Object>> features = (List<Map<String, Object>>) data.get("features");
        Map<String, Object> properties = (Map<String, Object>) features.get(0).get("properties");
        Map<String, Object> summary = (Map<String, Object>) properties.get("summary");

        double durationSeconds = ((Number) summary.get("duration")).doubleValue();
        double distanceMeters = ((Number) summary.get("distance")).doubleValue();

        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setOriginLat(from[0]);
        dto.setOriginLng(from[1]);
        dto.setOriginAddress(originAddress);
        dto.setDestinationLat(to[0]);
        dto.setDestinationLng(to[1]);
        dto.setDestinationAddress(destAddress);
        dto.setDurationSeconds((long) durationSeconds);
        dto.setDistanceKm(distanceMeters / 1000.0);
        dto.setEta(formatEta((long) durationSeconds, distanceMeters));

        return dto;
    }

    // ✅ UNCHANGED: formatEta() is exactly as before
    private String formatEta(long seconds, double meters) {
        long minutes = Math.round(seconds / 60.0);
        String km = String.format("%.1f", meters / 1000.0);

        if (minutes < 60) {
            return minutes + " min away · " + km + " km";
        }

        long hours = minutes / 60;
        long remainingMins = minutes % 60;
        return hours + "h " + remainingMins + "min away · " + km + " km";
    }
}