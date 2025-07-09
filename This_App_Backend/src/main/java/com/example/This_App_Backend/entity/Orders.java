package com.example.This_App_Backend.entity;

import java.math.BigDecimal;
import java.sql.Driver;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties.Store;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to Users table
    private User user; // Link to the User entity

    @ManyToOne(fetch = FetchType.LAZY)
    private Stores store; // Link to the Store entity

    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus; // e.g., 'pending', 'shipped', 'delivered', 'cancelled'

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "is_assigned_driver")
    private Boolean isAssignedDriver;

    // Many Orders can be assigned to one Driver (can be null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id") // Maps to driver_id in orders table (can be null)
    private Drivers driver;

    // Many Orders have one delivery User_Address
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", nullable = false) // Maps to delivery_address_id in orders table
    private User_Addresses deliveryAddress;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Relationships ---
    // One Order has many Order_Items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order_Items> orderItems = new ArrayList<>();

    // One Order can have many Payments
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Payments> payments = new ArrayList<>();


}
