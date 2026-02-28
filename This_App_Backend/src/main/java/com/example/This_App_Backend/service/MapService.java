package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.MapDTO.RouteRequestDTO;
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
        CustomerOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order Not Found"));

        Stores store = order.getStore();
        User_Addresses delivery = order.getDeliveryAddress();

        if (store == null) throw new RuntimeException("Store Not Found");
        if (delivery == null) throw new RuntimeException("Delivery Address Not Found");

        String storeQuery = buildStoreQuery(store.getStoreAddress());
        double[] storeCoords = geocodeWithFallback(storeQuery);

        String deliveryQuery = delivery.getStreetNumber() + " " + delivery.getStreetName()
                + ", " + delivery.getSuburb() + ", " + delivery.getCity();

        double[] deliveryCoords = geocodeWithFallback(deliveryQuery);

        return fetchRoute(storeCoords, deliveryCoords, store.getStoreAddress(),
                delivery.getStreetName() + ", " + delivery.getCity());
    }

    /**
     * Called by POST /api/map/route/by-coords.
     * The frontend geocodes with Photon (works in the browser) and passes
     * the resulting coordinates here — no server-side geocoding needed.
     */
    public RouteResponseDTO getRouteByCoords(RouteRequestDTO req) {
        double[] from = {req.getOriginLat(), req.getOriginLng()};
        double[] to   = {req.getDestLat(),   req.getDestLng()};
        return fetchRoute(from, to, req.getOriginAddress(), req.getDestAddress());
    }

    // strips trailing postal codes from store addresses
    // e.g. "73 Juta St, Braamfontein, Johannesburg, 2001" → "73 Juta St, Braamfontein, Johannesburg"
    private String buildStoreQuery(String storeAddress) {
        if (storeAddress == null) return "";
        // Strip postal code
        String cleaned = storeAddress.replaceAll(",?\\s*\\d{4,5}\\s*$", "").trim();
        // Replace "St" with "Street", "Ave" with "Avenue" etc. for better Nominatim matching
        cleaned = cleaned.replaceAll("\\bSt\\b", "Street")
                .replaceAll("\\bAve\\b", "Avenue")
                .replaceAll("\\bRd\\b", "Road")
                .replaceAll("\\bDr\\b", "Drive");
        return cleaned;
    }

    // Retries geocoding with progressively simpler queries if ORS returns no results.
    // Attempt 1: full address
    // Attempt 2: suburb + city (drop street)
    // Attempt 3: city only (last resort)
    private double[] geocodeWithFallback(String query) {
        double[] result = tryGeocode(query);
        if (result != null) return result;

        String[] parts = query.split(",");
        if (parts.length > 1) {
            String simplified = String.join(",",
                    Arrays.copyOfRange(parts, 1, parts.length)).trim();
            result = tryGeocode(simplified);
            if (result != null) return result;
        }

        String cityOnly = parts[parts.length - 1].trim();
        result = tryGeocode(cityOnly);
        if (result != null) return result;

        throw new RuntimeException(
                "Could not geocode address after multiple attempts: \"" + query + "\"");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // Uses Photon (photon.komoot.io) — free, no API key, same service used by the Angular frontend.
    // Returns null on failure so geocodeWithFallback() can try a simpler query.
    private double[] tryGeocode(String query) {
        try {
            // Photon works best without commas — replace with spaces, just like the Angular frontend does
            String normalised = query.replace(",", " ").replaceAll("\\s+", " ").trim();

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://photon.komoot.io/api/")
                    .queryParam("q", normalised + " South Africa")
                    .queryParam("limit", 1)
                    .toUriString();

            System.out.println("Geocoding attempt (Photon): " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ThisAppBackend/1.0");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            System.out.println("HTTP Status: " + response.getStatusCode());

            Map<String, Object> body = response.getBody();
            if (body == null) return null;

            List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");
            System.out.println("Results size: " + (features == null ? "null" : features.size()));

            if (features == null || features.isEmpty()) return null;

            Map<String, Object> geometry = (Map<String, Object>) features.get(0).get("geometry");
            List<Double> coordinates = (List<Double>) geometry.get("coordinates");

            // GeoJSON returns [lng, lat]
            double lng = coordinates.get(0);
            double lat = coordinates.get(1);
            System.out.println("lat=" + lat + " lng=" + lng);

            return new double[]{lat, lng};

        } catch (Exception e) {
            System.err.println("Photon geocode EXCEPTION for: \"" + query + "\" — "
                    + e.getClass().getName() + ": " + e.getMessage());
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