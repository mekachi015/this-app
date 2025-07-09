package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id", "store_id"}) // Ensures uniqueness for user/product/store combo
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Integer wishlistId;

    // Many Wishlists items belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Maps to user_id in wishlists table
    private User user;

    // A wishlist item can be for a Product (can be null if wishing a store)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // Maps to product_id in wishlists table (can be null)
    private Products product;

    // A wishlist item can be for a Store (can be null if wishing a product)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // Maps to store_id in wishlists table (can be null)
    private Stores store;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
