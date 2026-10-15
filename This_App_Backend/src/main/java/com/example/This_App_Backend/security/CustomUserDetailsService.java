package com.example.This_App_Backend.security;

import org.springframework.stereotype.Service;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

   @Override
public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    return userRepo.findByUsername(identifier)
        .or(() -> userRepo.findByEmail(identifier))
        .map(user -> {
            // Convert UserType to Spring Security authority
            String role = "ROLE_" + user.getUserType().name(); // e.g. ROLE_DRIVER, ROLE_CUSTOMER
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
            );
        })
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));
}

}
