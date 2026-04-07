package com.example.This_App_Backend.dto.AuthenticationDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

    private String email; // can be either email or username    
    private String username;
    private String password;

    // public AuthenticationRequest() {}

    // public AuthenticationRequest(String username, String email, String password) {
    //     this.username = username;
    //     this.email = email;
    //     this.password = password;
    // }
}
