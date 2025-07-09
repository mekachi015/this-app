package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cart", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"}) // Ensures a user has only one entry per product
})
@Data
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    // Many Cart items belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Maps to user_id in cart table
    private User user;

    // Many Cart items refer to one Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Maps to product_id in cart table
    private Products product;

    // Many Cart items relate to one Store
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false) // Maps to store_id in cart table
    private Stores store;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // CHECK (quantity > 0) is handled by DB schema

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
