package com.example.This_App_Backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users") //maps user entity to "Users" table
@Data
@NoArgsConstructor
//@AllArgsConstructor

public class User {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true, length = 50)    
    private String username; // find a way to generate unique usernames, remeberable names based on first and last name

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "password", nullable = false, length = 100)
    private String password; // use jwt for password hashing and security

    @Column(name = "user_type", nullable = false, length = 20)   
    private String userType; // e.g., driver, customer, admin find way to distinguish between user types, maybe use enum

    @Column(name = "created_at", updatable = false) // updatable = false means JPA won't try to update this field
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relationships ---

    // One User can have many User_Addresses
    // 'mappedBy' indicates that the 'user' field in UserAddress is the owning side of the relationship
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User_Addresses> addresses = new ArrayList<>();

    // One User can place many Orders
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();

    // One User can have many Payment_Methods
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment_Methods> paymentMethods = new ArrayList<>();

    // One User can write many Reviews_And_Ratings
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review_And_Ratings> reviews = new ArrayList<>();

    // One User can have many Wishlists items
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wishlist> wishlists = new ArrayList<>();

    // One User can have many Cart items
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> cartItems = new ArrayList<>();

    // One User can be a Store_Owner (One-to-One relationship)
    // 'mappedBy' indicates that the 'user' field in StoreOwner is the owning side
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Store_Owners storeOwner;

    // One User can be a Driver (One-to-One relationship)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Drivers driver;


     @PrePersist // Called before an entity is first saved to the database
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate // Called before an entity is updated in the database
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
