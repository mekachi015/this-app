package com.example.This_App_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 

import com.example.This_App_Backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    //Find user by user name
    Optional<User> findByUsername(String username);

    //find user by email
    Optional<User> findByEmail(String email);

    //find by email or username
    

    // find user by type
    List<User> findByUserType(User.UserType userType);

   // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    //find user by first and last name full name
    //List<User> findByFirstAndLastName(String firstName, String lastName);

    // //Custom query for future
    // @Query("SELECT u FROM User WHERE u.firstname LIKE %:name% OR u.lastName Like %:name%")
    // List<User> findByNameContaining(@Param("name") String name );

}
