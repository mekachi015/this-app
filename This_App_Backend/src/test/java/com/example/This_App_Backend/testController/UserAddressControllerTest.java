package com.example.This_App_Backend.testController;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.example.This_App_Backend.controller.UserAddressController;
import com.example.This_App_Backend.dto.AddressDTO.AddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.CreateAddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.UpdateDTO;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtRequestFilter;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserAddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private JwtUtil jwtUtil;

    private CreateAddressDTO buildCreateAddressDto() {
        CreateAddressDTO dto = new CreateAddressDTO();
        dto.setStreetNumber("123");
        dto.setStreetName("Main St");
        dto.setSuburb("Central");
        dto.setCity("Capetown");
        dto.setProvince("Western Cape");
        dto.setPostalCode("8000");
        dto.setAddressType("SHIPPING");
        dto.setIsDefault(true);
        return dto;
    }

    private UpdateDTO buildUpdateDto() {
        UpdateDTO dto = new UpdateDTO();
        dto.setStreetNumber("456");
        dto.setStreetName("Market Ave");
        dto.setSuburb("Harbor");
        dto.setCity("Capetown");
        dto.setProvince("Western Cape");
        dto.setPostalCode("8001");
        dto.setAddressType("BILLING");
        dto.setIsDefault(false);
        return dto;
    }

    private AddressDTO buildAddressDto(Long id, Long userId, boolean isDefault) {
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(id);
        dto.setUserId(userId);
        dto.setStreetNumber("123");
        dto.setStreetName("Main St");
        dto.setSuburb("Central");
        dto.setCity("Capetown");
        dto.setProvince("Western Cape");
        dto.setPostalCode("8000");
        dto.setAddressType("SHIPPING");
        dto.setIsDefault(isDefault);
        return dto;
    }

    @Test
    void createAddress_whenValid_shouldReturnCreatedAddress() throws Exception {
        CreateAddressDTO request = buildCreateAddressDto();
        AddressDTO response = buildAddressDto(1L, 1L, true);

        when(addressService.createAddress(eq(1L), any(CreateAddressDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/addresses/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.streetName").value("Main St"))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void getUserAddresses_whenUserHasAddresses_shouldReturnList() throws Exception {
        AddressDTO first = buildAddressDto(1L, 1L, false);
        AddressDTO second = buildAddressDto(2L, 1L, true);
        when(addressService.getUserAddress(1L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/addresses/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].addressId").value(2))
                .andExpect(jsonPath("$[1].isDefault").value(true));
    }

    @Test
    void getAddressById_whenExists_shouldReturnAddress() throws Exception {
        AddressDTO response = buildAddressDto(10L, 1L, false);
        when(addressService.getAddressById(1L, 10L)).thenReturn(response);

        mockMvc.perform(get("/api/addresses/user/1/address/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(10))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void updateAddress_whenValid_shouldReturnUpdatedAddress() throws Exception {
        UpdateDTO request = buildUpdateDto();
        AddressDTO response = buildAddressDto(10L, 1L, false);
        response.setStreetNumber("456");
        response.setStreetName("Market Ave");
        response.setAddressType("BILLING");

        when(addressService.updateAddress(eq(1L), eq(10L), any(UpdateDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/addresses/user/1/address/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(10))
                .andExpect(jsonPath("$.streetName").value("Market Ave"))
                .andExpect(jsonPath("$.addressType").value("BILLING"));
    }

    @Test
    void deleteAddress_whenExists_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(addressService).deleteAddress(1L, 10L);

        mockMvc.perform(delete("/api/addresses/user/1/address/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Address delete succesfully"));
    }

    @Test
    void setDefaultAddress_whenValid_shouldReturnUpdatedAddress() throws Exception {
        AddressDTO response = buildAddressDto(10L, 1L, true);
        when(addressService.setDefaultAddress(1L, 10L)).thenReturn(response);

        mockMvc.perform(patch("/api/addresses/user/1/address/10/set-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(10))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void getDefaultAddress_whenExists_shouldReturnDefaultAddress() throws Exception {
        AddressDTO response = buildAddressDto(10L, 1L, true);
        when(addressService.getDefaultAddress(1L)).thenReturn(response);

        mockMvc.perform(get("/api/addresses/user/1/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(10))
                .andExpect(jsonPath("$.isDefault").value(true));
    }

    @Test
    void getAddressById_whenNotFound_shouldReturnBadRequest() throws Exception {
        when(addressService.getAddressById(1L, 99L)).thenThrow(new RuntimeException("Address not found"));

        mockMvc.perform(get("/api/addresses/user/1/address/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Address not found"));
    }
}
