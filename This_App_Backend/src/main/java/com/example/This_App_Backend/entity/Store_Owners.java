package com.example.This_App_Backend.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_owners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store_Owners {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Integer ownerId;

    @OneToOne // One store owner entity links to one user entity
    @JoinColumn(name = "user_id", unique = true, nullable = false) // Foreign key to Users table
    private User user; // Link to the User entity

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relationships ---

    // One Store_Owner can own many Stores
    @OneToMany(mappedBy = "store_Owners", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stores> stores = new ArrayList<>();



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
