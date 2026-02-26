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
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to Users table
    private User user; // Link to the User entity

    @Column(name = "street_number", nullable = false, length = 255) 
    private String streetNumber; // First line of the address (e.g., "123")

    @Column(name = "street_name", nullable = false, length = 255)
    private String streetName; // Second line of the address (optional)

    @Column(name = "suburb", nullable = false, length = 255)
    private String suburb; // Third line of the address (optional)

    @Column(name = "city", nullable = false, length = 100)  
    private String city; // City of the address

    //Change this to province even in the database
    @Column(name = "province", nullable = false, length = 100)
    private String province; // Should store province instead of state

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode; // Postal code of the address

    @Column(name = "address_type", nullable = false, length = 50)
    private String addressType; // Billing, Shipping or both

    @Column(name = "is_default", nullable = false)  
    private boolean isDefault; // Indicates if this is the default address for the user

     // --- Relationships ---
    // One User_Address can be the delivery address for many Orders
    @OneToMany(mappedBy = "deliveryAddress", cascade = CascadeType.ALL)
    private List<CustomerOrders> deliveryOrders = new ArrayList<>();

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
