package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews_and_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review_And_Ratings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    // Many Reviews_And_Ratings belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Maps to user_id in reviews_and_ratings table
    private User user;

    // A review can be for a Product (can be null if reviewing a store)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // Maps to product_id in reviews_and_ratings table (can be null)
    private Products product;

    // A review can be for a Store (can be null if reviewing a product)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // Maps to store_id in reviews_and_ratings table (can be null)
    private Stores store;

    @Column(name = "rating", nullable = false)
    private Integer rating; // CHECK (rating >= 1 AND rating <= 5) is handled by DB schema

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
