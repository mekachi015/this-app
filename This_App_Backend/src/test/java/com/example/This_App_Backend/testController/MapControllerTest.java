package com.example.This_App_Backend.testController;

import com.example.This_App_Backend.controller.MapController;
import com.example.This_App_Backend.dto.MapDTO.RouteRequestDTO;
import com.example.This_App_Backend.dto.MapDTO.RouteResponseDTO;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.MapService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import static org.hamcrest.Matchers.containsString;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MapController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MapControllerTest {
   
   @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MapService mapService;

    @MockBean
    private RestTemplate restTemplate;

    private RouteRequestDTO routeRequest;
    private RouteResponseDTO routeResponse;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;
    
    // You may also need this if your JwtRequestFilter uses it
    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        routeRequest = new RouteRequestDTO();
        routeRequest.setOriginLat(-26.195);
        routeRequest.setOriginLng(28.034);
        routeRequest.setDestLat(-26.204);
        routeRequest.setDestLng(28.047);

        routeResponse = new RouteResponseDTO();
        routeResponse.setDistanceKm(5.2);
        routeResponse.setDurationSeconds(600L);
        routeResponse.setEta("10 min away · 5.2 km");
    }

    @Test
    void getRouteByCoords_ShouldReturnOk() throws Exception {
        when(mapService.getRouteByCoords(any(RouteRequestDTO.class))).thenReturn(routeResponse);

        mockMvc.perform(post("/api/map/route/by-coords")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(routeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(5.2))
                .andExpect(jsonPath("$.eta").value("10 min away · 5.2 km"));
    }

    @Test
    void getOrderRoute_ShouldReturnDataFromService() throws Exception {
        when(mapService.getRouteForOrder(1L)).thenReturn(routeResponse);

        mockMvc.perform(get("/api/map/route/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(5.2));
    }

    @Test
    void getMultiStopRoute_ShouldCalculateCorrectly() throws Exception {
        // Prepare complex multi-stop request body
        Map<String, Object> multiStopRequest = Map.of(
                "storeCoordinates", List.of(Map.of("lat", -26.1, "lng", 28.1)),
                "deliveryCoordinates", Map.of("lat", -26.2, "lng", 28.2),
                "storeAddresses", List.of("Store A"),
                "deliveryAddress", "Customer Home"
        );

        // Mock the external ORS API response that RestTemplate would return
        Map<String, Object> mockOrsResponse = Map.of(
                "features", List.of(Map.of(
                        "geometry", Map.of("type", "LineString", "coordinates", List.of()),
                        "properties", Map.of("summary", Map.of("duration", 1200.0, "distance", 10000.0))
                ))
        );

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(mockOrsResponse, HttpStatus.OK));

        mockMvc.perform(post("/api/map/route/multi-stop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(multiStopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(10.0))
                .andExpect(jsonPath("$.eta").value(containsString("20 min away")));
    }

    @Test
    void testAddress_ShouldProxyToPhoton() throws Exception {
        String mockPhotonJson = "{\"features\": []}";
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockPhotonJson, HttpStatus.OK));

        mockMvc.perform(get("/api/map/test-address")
                        .param("q", "Johannesburg"))
                .andExpect(status().isOk())
                .andExpect(content().string(mockPhotonJson));
    }
}
