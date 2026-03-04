package com.example.This_App_Backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;



import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutInitiateResponse;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.PayFastCheckoutService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class PayfastController {

    @Autowired
    private PayFastCheckoutService checkoutService;

    @Autowired
    private UserRepository userRepo;

    @Value("${platform.shipping.fee}")
    private BigDecimal shippingFee;

    //customer initiates checkout and returns payfast url for the frontend to redirect to
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateCheckout(
        @RequestParam Long userId,
        @RequestParam(required = false) Long deliveryAddressId,
        Authentication authentication
    ){
        try{
            if(authentication == null || !authentication.isAuthenticated()){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            if (!isUserAuthorized(authentication.getName(), userId)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
            }

            CheckoutInitiateResponse response = checkoutService.initiateCheckout(userId, deliveryAddressId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception e){
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to initiate checkout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    
        private boolean isUserAuthorized(String username, Long userId) {
         return userRepo.findByUsername(username)
        .map(user -> user.getUsername().equals(username))
        .orElse(false);
    }


        @PostMapping("/payfast-notify")
        public ResponseEntity<String> payfastNotify(
            @RequestParam Map<String, String> itnParams
        ) {
            try{
                checkoutService.processPayfastItn(itnParams);
                return ResponseEntity.ok("OK");
            } catch(Exception e) {
                System.err.println("ITN processing error: " + e.getMessage());
                return ResponseEntity.ok("OK");
            }
        }

        @GetMapping("/config")
        public ResponseEntity<?> getCheckoutConfig(){
            Map<String, Object> config = new HashMap<>();
            config.put("shippingFee", shippingFee);
            return ResponseEntity.ok(config);
        }


        
}
