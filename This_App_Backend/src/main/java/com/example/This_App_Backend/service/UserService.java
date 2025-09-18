package com.example.This_App_Backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import java.util.Optional;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // create a new user
    public User createUser(User user) {

        // check if user exists
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exist");
        }

        // check if email already exists
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepo.save(user);
    }

    // Retrieve all users
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    // Get user by id
    public Optional<User> getUserById(Integer id) {
        return userRepo.findById(id);
    }

    // get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    // Get user by type
    public List<User> getUserByType(User.UserType userType) {
        return userRepo.findByUserType(userType);
    }

    // Update user
    public User updateUser(Integer id, User userDetails) {
        return userRepo.findById(id).map(user -> {
            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            user.setUserType(userDetails.getUserType());

            // Only update password if provided
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            return userRepo.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Delete specific user
    public void deleteUser(Integer id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepo.delete(user);
    }

    // Check if user exists
    public boolean userExists(Integer id) {
        return userRepo.existsById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

}
