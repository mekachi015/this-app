package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User_Addresses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to Users table
    private User user; // Link to the User entity

    @Column(name = "address_line1", nullable = false, length = 255) 
    private String addressLine1; // First line of the address

    @Column(name = "address_line2", length = 255)
    private String addressLine2; // Second line of the address (optional)

    @Column(name = "address_line3", length = 255)
    private String addressLine3; // Third line of the address (optional)

    @Column(name = "city", nullable = false, length = 100)  
    private String city; // City of the address

    //Change this to province even in the database
    @Column(name = "state", nullable = false, length = 100)
    private String state; // Should store province instead of state

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode; // Postal code of the address

    @Column(name = "address_type", nullable = false, length = 50)
    private String addressType; // Billing, Shipping or both

    @Column(name = "is_default", nullable = false)  
    private boolean isDefault; // Indicates if this is the default address for the user

     // --- Relationships ---
    // One User_Address can be the delivery address for many Orders
    @OneToMany(mappedBy = "deliveryAddress", cascade = CascadeType.ALL)
    private List<Orders> deliveryOrders = new ArrayList<>();

    // One User_Address can be the billing address for many Payment_Methods
    @OneToMany(mappedBy = "billingAddress", cascade = CascadeType.ALL)
    private List<Payment_Methods> paymentMethods = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
