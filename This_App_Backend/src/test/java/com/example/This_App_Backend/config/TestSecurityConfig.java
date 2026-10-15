package com.example.This_App_Backend.config;

import com.example.This_App_Backend.security.JwtRequestFilter;
import com.example.This_App_Backend.security.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for @WebMvcTest.
 * Provides mock beans for JWT components that would otherwise fail to initialize.
 * This prevents UnsatisfiedDependencyException during test context loading.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Mock JwtUtil to prevent initialization errors in tests.
     * Real tests should mock this at the test level as needed.
     */
    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return mock(JwtUtil.class);
    }

    /**
     * Mock JwtRequestFilter to prevent initialization errors.
     * The filter won't be active in @WebMvcTest since it's only testing the controller layer.
     */
    @Bean
    @Primary
    public JwtRequestFilter jwtRequestFilter(JwtUtil jwtUtil) {
        return mock(JwtRequestFilter.class);
    }
}
