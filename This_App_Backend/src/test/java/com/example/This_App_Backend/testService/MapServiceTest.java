package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.This_App_Backend.dto.MapDTO.RouteRequestDTO;
import com.example.This_App_Backend.dto.MapDTO.RouteResponseDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.service.MapService;

public class MapServiceTest {
    @Mock
    private OrderRepository orderRepo;

    @Mock
    private RestTemplate restTemplate;

    private MapService mapService;

    @BeforeEach
    void setUp() {
        // This line is what actually creates the mocks — must be first
        MockitoAnnotations.openMocks(this);

        // Now create the service and inject the freshly created mocks
        mapService = new MapService();
        ReflectionTestUtils.setField(mapService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(mapService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(mapService, "orsApiKey", "test-ors-key");
    }

    // ── getRouteForOrder ──────────────────────────────────────────────────────

    @Test
    void getRouteForOrder_throwsWhenOrderNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapService.getRouteForOrder(99L));

        assertTrue(ex.getMessage().contains("Order Not Found"));
    }

    @Test
    void getRouteForOrder_throwsWhenStoreIsNull() {
        CustomerOrders order = new CustomerOrders();
        order.setStore(null);
        order.setDeliveryAddress(buildAddress());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapService.getRouteForOrder(1L));

        assertTrue(ex.getMessage().contains("Store Not Found"));
    }

    @Test
    void getRouteForOrder_throwsWhenDeliveryAddressIsNull() {
        CustomerOrders order = new CustomerOrders();
        order.setStore(buildStore());
        order.setDeliveryAddress(null);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapService.getRouteForOrder(1L));

        assertTrue(ex.getMessage().contains("Delivery Address Not Found"));
    }

    @Test
    void getRouteForOrder_returnsRouteWhenEverythingValid() {
        CustomerOrders order = new CustomerOrders();
        order.setStore(buildStore());
        order.setDeliveryAddress(buildAddress());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        mockPhotonResponse(-26.2041, 28.0473);
        mockOrsResponse(1200.0, 15000.0);

        RouteResponseDTO result = mapService.getRouteForOrder(1L);

        assertNotNull(result);
        assertEquals(15.0, result.getDistanceKm(), 0.01);
        assertEquals(1200L, result.getDurationSeconds());
        assertNotNull(result.getEta());
    }

    // ── getRouteByCoords ──────────────────────────────────────────────────────

    @Test
    void getRouteByCoords_returnsRouteDTO() {
        mockOrsResponse(900.0, 8500.0);

        RouteResponseDTO result = mapService.getRouteByCoords(buildCoordsRequest());

        assertNotNull(result);
        assertEquals(-26.2041, result.getOriginLat());
        assertEquals(28.0473, result.getOriginLng());
        assertEquals(8.5, result.getDistanceKm(), 0.01);
        assertEquals(900L, result.getDurationSeconds());
        assertEquals("Origin", result.getOriginAddress());
        assertEquals("Destination", result.getDestinationAddress());
    }

    // ── ETA formatting ────────────────────────────────────────────────────────

    @Test
    void eta_formatsUnderOneHourCorrectly() {
        mockOrsResponse(1200.0, 15000.0);

        RouteResponseDTO result = mapService.getRouteByCoords(buildCoordsRequest());

        String eta = result.getEta();

        // Only assert on the parts we can reliably check
        // Avoid the middle-dot character (encoding varies by OS)
        // Avoid exact decimal format ("15.0" vs "15,0" depends on system locale)
        assertTrue(eta.contains("20 min away"),
                "Expected '20 min away' in: " + eta);
        assertTrue(eta.contains("15") && eta.contains("km"),
                "Expected distance in km in: " + eta);
    }

    @Test
    void eta_formatsOverOneHourCorrectly() {
        mockOrsResponse(4500.0, 80000.0); // 75 min = 1h 15min

        RouteResponseDTO result = mapService.getRouteByCoords(buildCoordsRequest());

        String eta = result.getEta();

        assertTrue(eta.contains("1h"),
                "Expected '1h' in: " + eta);
        assertTrue(eta.contains("15min away"),
                "Expected '15min away' in: " + eta);
        assertTrue(eta.contains("80") && eta.contains("km"),
                "Expected distance in km in: " + eta);
    }

   

    // ── Geocode fallback ──────────────────────────────────────────────────────

    @Test
    void getRouteForOrder_throwsWhenGeocodeFailsAllAttempts() {
        CustomerOrders order = new CustomerOrders();
        order.setStore(buildStore());
        order.setDeliveryAddress(buildAddress());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        mockPhotonEmptyResponse();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapService.getRouteForOrder(1L));

        assertTrue(ex.getMessage().contains("Could not geocode address"),
                "Expected geocode failure message but got: " + ex.getMessage());
    }

    @Test
    void getRouteForOrder_succeedsOnFallbackGeocode() {
        CustomerOrders order = new CustomerOrders();
        order.setStore(buildStore());
        order.setDeliveryAddress(buildAddress());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        mockPhotonFallbackResponse();
        mockOrsResponse(600.0, 5000.0);

        RouteResponseDTO result = mapService.getRouteForOrder(1L);

        assertNotNull(result);
    }

    // ── ORS error handling ────────────────────────────────────────────────────

    @Test
    void getRouteByCoords_throwsReadableMessageOnOrsError2010() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        "{\"error\":{\"code\":2010,\"message\":\"no route\"}}".getBytes(),
                        StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapService.getRouteByCoords(buildCoordsRequest()));

        assertTrue(ex.getMessage().contains("No drivable road found"),
                "Expected user-friendly error but got: " + ex.getMessage());
    }

    // ── Mock helpers ──────────────────────────────────────────────────────────

    private void mockPhotonResponse(double lat, double lng) {
        Map<String, Object> geometry = Map.of("coordinates", List.of(lng, lat));
        Map<String, Object> feature = Map.of("geometry", geometry);
        Map<String, Object> body = Map.of("features", List.of(feature));

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private void mockPhotonEmptyResponse() {
        Map<String, Object> body = Map.of("features", List.of());

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private void mockPhotonFallbackResponse() {
        Map<String, Object> empty = Map.of("features", List.of());

        Map<String, Object> geometry = Map.of("coordinates", List.of(28.0473, -26.2041));
        Map<String, Object> feature = Map.of("geometry", geometry);
        Map<String, Object> success = Map.of("features", List.of(feature));

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(empty, HttpStatus.OK)) // 1st fails
                .thenReturn(new ResponseEntity<>(success, HttpStatus.OK)) // 2nd fallback
                .thenReturn(new ResponseEntity<>(success, HttpStatus.OK)); // delivery geocode
    }

    private void mockOrsResponse(double durationSeconds, double distanceMeters) {
        Map<String, Object> summary = Map.of(
                "duration", durationSeconds,
                "distance", distanceMeters);
        Map<String, Object> properties = Map.of("summary", summary);
        Map<String, Object> feature = Map.of("properties", properties);
        Map<String, Object> body = Map.of("features", List.of(feature));

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    // ── Entity builders ───────────────────────────────────────────────────────

    private Stores buildStore() {
        Stores store = new Stores();
        store.setStoreName("Test Store");
        store.setStoreAddress("73 Juta Street, Braamfontein, Johannesburg, 2001");
        return store;
    }

    private User_Addresses buildAddress() {
        User_Addresses address = new User_Addresses();
        address.setStreetNumber("12");
        address.setStreetName("Main Road");
        address.setSuburb("Sandton");
        address.setCity("Johannesburg");
        address.setPostalCode("2196");
        return address;
    }

    private RouteRequestDTO buildCoordsRequest() {
        RouteRequestDTO req = new RouteRequestDTO();
        req.setOriginLat(-26.2041);
        req.setOriginLng(28.0473);
        req.setDestLat(-26.1952);
        req.setDestLng(28.0340);
        req.setOriginAddress("Origin");
        req.setDestAddress("Destination");
        return req;
    }
}
