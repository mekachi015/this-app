package com.example.This_App_Backend.dto;

public class AuthenticationResponse {
    private String jwt;

    public AuthenticationResponse(){}

    public AuthenticationResponse(String jwt){
        this.jwt = jwt;
    }

     public String getJwt() { 
        return jwt; 
    }

    public void setJwt(String jwt) { 
        this.jwt = jwt; 
    }
}
    

