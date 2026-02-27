package com.example.This_App_Backend.service;

import lombok.Value;
import org.springframework.stereotype.Service;

@Service
public class MapService {


    @Value("${ors.api.key}")
    private String orsApiKey;
}
