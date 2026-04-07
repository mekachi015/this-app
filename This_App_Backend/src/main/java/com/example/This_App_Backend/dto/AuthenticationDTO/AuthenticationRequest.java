package com.example.This_App_Backend.dto.AuthenticationDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {

    private String email; // can be either email or username    
    private String username;
    private String password;

    public AuthenticationRequest() {}

    public AuthenticationRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
