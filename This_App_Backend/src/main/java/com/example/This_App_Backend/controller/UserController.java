package com.example.This_App_Backend.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.This_App_Backend.service.FileStorageService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.service.UserService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;


    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get user by type
    @GetMapping("/type/{userType}")
    public ResponseEntity<List<User>> getUserByType(@PathVariable User.UserType userType) {
        List<User> users = userService.getUserByType(userType);

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User newUser = userService.createUser(user);

            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Updatae user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //Get user profile picture url
    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<Map<String, String>> getUserProfilePicture(
            @PathVariable Long id
    ){
        try {
            Optional<User> user = userService.getUserById(id);

            if(user.isPresent() && user.get().getProfilePhotoUrl() != null){
                Map<String, String> response = new HashMap<>();

                response.put("profilePhotoUrl", user.get().getProfilePhotoUrl());
                return  new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Upload profile picture
    @PostMapping("/{id}/profile-photo")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @PathVariable Long id,
           @RequestParam("profilePhoto") MultipartFile file,
           @RequestHeader("Authorization") String authHeader
            ){
        try{
            //Validate file
            if (file.isEmpty()){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            //upload to cloudinary
            String photoUrl =  fileStorageService.storeProfilePhoto(file, id);

            Optional<User> userOptional = userService.getUserById(id);
            if (userOptional.isPresent()){
                User user = userOptional.get();
                user.setProfilePhotoUrl(photoUrl);
                userService.updateUser(user);

                Map<String, String> response = new HashMap<>();
                response.put("photoUrl", photoUrl);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Delete profile picture
    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<Void> deleteProfilePicture(@PathVariable Long id){
        try{
            Optional<User> userOptional = userService.getUserById(id);
            if(userOptional.isPresent()){
                User user = userOptional.get();

                //Delete from cloudinary
                if(user.getProfilePhotoUrl() != null){
                    String publicId = "profile_photos/user" + id;
                    fileStorageService.deleteImage(publicId);
                }

                //remove url from database
                user.setProfilePhotoUrl(null);
                userService.updateUser(user);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e){
            return  new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
