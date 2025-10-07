package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "stores") //maps user entity to "Users" table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stores {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    // Update the mapping to match Store_Owners entity
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Store_Owners storeOwner;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "store_description", length = 500)
    private String storeDescription;

    @Column(name = "store_address", nullable = false, length = 255)
    private String storeAddress;

    @Column(name = "store_phone_number", length = 15)
    private String storePhoneNumber;

    @Column(name = "store_email", length = 100)
    private String storeEmail;

    @Column(name = "store_business_hours", length = 100)
    private String storeBusinessHours;

    @Column(name = "store_logo", length = 255)
    private String storeLogo;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One Store can have many Products
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Products> products = new ArrayList<>();

    // One Store can have many Orders (if orders are per-store)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Orders> orders = new ArrayList<>();

    // One Store can have many Reviews_And_Ratings
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Review_And_Ratings> reviews = new ArrayList<>();

    // One Store can have many Inventory_Transactions
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Inventory_Transactions> inventoryTransactions = new ArrayList<>();

    // One Store can be in many Wishlists (via Wishlist items)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Wishlist> wishlists = new ArrayList<>();

    // One Store can have many Cart items
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Cart> cartItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}














