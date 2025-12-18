package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.AddressDTO.AddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.CreateAddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.UpdateDTO;
import com.example.This_App_Backend.dto.AddressResponses.SuccessResponse;
import com.example.This_App_Backend.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/addresses")
@CrossOrigin(origins = "https://localhost:4200")
public class UserAddressController {

    @Autowired
    private AddressService addressService;

    //Create a new address for user
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createAddress(
            @PathVariable Long userId,
            @RequestBody CreateAddressDTO dto){
        try{
            AddressDTO address = addressService.createAddress(userId,dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(address);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(
                    new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())
            );
        }
    }


    //Get all addresses for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAddresses(@PathVariable Long userId){
        try{
            List<AddressDTO> addresses = addressService.getUserAddress(userId);
            return ResponseEntity.ok(addresses);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())

            );
        }
    }

    //get a specific address by id
    @GetMapping("/user/{userId}/address/{addressId}")
    public ResponseEntity<?> getAddressById(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        try {
            AddressDTO address = addressService.getAddressById(userId, addressId);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                                        new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())

            );
        }
    }

    //Update existing address
    @PutMapping("/user/{userId}/address/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @RequestBody UpdateDTO dto){
        try {
            AddressDTO address = addressService.updateAddress(userId, addressId, dto);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())

            );
        }
    }

    //Delete address
    @DeleteMapping("/user/{userId}/address/{addressId}")
    public ResponseEntity<?> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        try{
            addressService.deleteAddress(userId, addressId);
            return ResponseEntity.ok(new SuccessResponse("Address delete succesfully"));
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(
            new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())
            );
        }
    }

    //Set address as default
    @PatchMapping("/user/{userId}/address/{addressId}/set-default")
    public ResponseEntity<?> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        try{
            AddressDTO address = addressService.setDefaultAddress(userId, addressId);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(
                                        new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())

            );
        }
    }

    //Get default address for user
    @GetMapping("/user/{userId}/default")
    public ResponseEntity<?> getDefaultAddress(
            @PathVariable Long userId
    ) {
        try{
            AddressDTO address = addressService.getDefaultAddress(userId);
            return ResponseEntity.ok(address);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                                        new com.example.This_App_Backend.dto.AddressResponses.ErrorResponse(e.getMessage())

            );
        }
    }





}
