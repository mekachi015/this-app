package com.example.This_App_Backend.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_owners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Store_Owners {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long ownerId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Update the mapping to reference the storeOwner field in Stores
    @OneToMany(mappedBy = "storeOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stores> stores = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relationships ---

    // One Store_Owner can own many Stores
    // @OneToMany(mappedBy = "owner_id", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Stores> stores = new ArrayList<>();



    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public Long getOwnerId() {
        return ownerId;
    }
}
