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
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drivers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Integer driverId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true) // Maps to user in drivers table (can be null )
    private User user; // Link to the User entity

    @Column(name = "first_name", nullable = false, length = 50) 
    private String firstName; // First name of the driver

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName; // Last name of the driver

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber; // Phone number of the driver

    @Column(name = "email", unique = true, length = 100)    
    private String email; // Email address of the driver

    @Column(name = "status", length = 20)
    private String status; // e.g., 'available', 'busy', 'offline'

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType; // Type of vehicle the driver uses

    @Column(name = "vehicle_license_plate", unique = true, length = 20  )
    private String vehicleLicensePlate; // License plate of the driver's vehicle

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Relationships ---


    // One Driver can be assigned to many Orders
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();
    

    
}
