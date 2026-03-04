package com.example.This_App_Backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.This_App_Backend.dto.AuthenticationDTO.AuthenticationRequest;
import com.example.This_App_Backend.dto.AuthenticationDTO.AuthenticationResponse;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200") // Allow Angular frontend
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        // Get user details to return in response
        Optional<User> userOpt = userService.getUserByUsername(authenticationRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOpt.get();

        // Create comprehensive response
        AuthenticationResponse response = new AuthenticationResponse();
        response.setJwt(jwt);
        response.setId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUserType(user.getUserType().toString());
        response.setCreatedAt(user.getCreatedAt());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Check if user already exists
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Email already exists"));
            }

            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Username already exists"));
            }

            User newUser = userService.createUser(user);

            
            // Generate JWT token for immediate login
            final UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails);

            // Remove password from response for security
            newUser.setPassword(null);

            // Create response with token
            AuthenticationResponse response = new AuthenticationResponse();
            response.setJwt(jwt);
            response.setId(newUser.getUserId());
            response.setEmail(newUser.getEmail());
            response.setUsername(newUser.getUsername());
            response.setFirstName(newUser.getFirstName());
            response.setLastName(newUser.getLastName());
            response.setUserType(newUser.getUserType().toString());
            response.setCreatedAt(newUser.getCreatedAt());
            response.setProfilePhotoUrl(user.getProfilePhotoUrl());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // @GetMapping("/users/profile")
    // public ResponseEntity<User> getUserProfile(Authentication authentication) {
    // String username = authentication.getName();
    // User user = userService.findByUsername(username);
    // user.setPassword(null); // Don't return password
    // return ResponseEntity.ok(user);
    // }

    // Inner class for error responses
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @PostMapping("/login/driver")
    public ResponseEntity<?> createDriverAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        return authenticateUser(authenticationRequest, User.UserType.DRIVER);
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> createAdminAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        return authenticateUser(authenticationRequest, User.UserType.ADMIN);
    }

    @PostMapping("/register/driver")
    public ResponseEntity<?> registerDriver(@RequestBody User user) {
        return registerUserWithType(user, User.UserType.DRIVER);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        return registerUserWithType(user, User.UserType.ADMIN);
    }

    private ResponseEntity<?> authenticateUser(AuthenticationRequest authenticationRequest,
            User.UserType expectedUserType) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        Optional<User> userOpt = userService.getUserByUsername(authenticationRequest.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOpt.get();

        // Validate user type
        if (user.getUserType() != expectedUserType) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied for user type"));
        }

        AuthenticationResponse response = new AuthenticationResponse();
        response.setJwt(jwt);
        response.setId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUserType(user.getUserType().toString());
        response.setCreatedAt(user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> registerUserWithType(User user, User.UserType userType) {
        try {
            // Check if user already exists
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Email already exists"));
            }

            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse("Username already exists"));
            }

            // Set the user type
            user.setUserType(userType);
            User newUser = userService.createUser(user);

            

            // Generate JWT token for immediate login
            final UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails);

            // Remove password from response for security
            newUser.setPassword(null);

            // Create response with token
            AuthenticationResponse response = new AuthenticationResponse();
            response.setJwt(jwt);
            response.setId(newUser.getUserId());
            response.setEmail(newUser.getEmail());
            response.setUsername(newUser.getUsername());
            response.setFirstName(newUser.getFirstName());
            response.setLastName(newUser.getLastName());
            response.setUserType(newUser.getUserType().toString());
            response.setCreatedAt(newUser.getCreatedAt());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
