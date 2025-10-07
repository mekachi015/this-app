package com.example.This_App_Backend.dto.AuthenticationDTO;

import java.time.LocalDateTime;

import com.example.This_App_Backend.entity.User.UserType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {
    private String jwt;
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String userType;
    private LocalDateTime createdAt;
    private String profilePhotoUrl;

    // Default constructor
    public AuthenticationResponse() {}

    // Constructor with just JWT (for backward compatibility)
    public AuthenticationResponse(String jwt) {
        this.jwt = jwt;
    }

    // Full constructor
    public AuthenticationResponse(String jwt, Long id, String email, String username, 
                                String firstName, String lastName, String userType, LocalDateTime createdAt, String profilePhotoUrl) {
        this.jwt = jwt;
        this.id = id;
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.createdAt = createdAt;
        this.profilePhotoUrl = profilePhotoUrl;
    }

}
    

