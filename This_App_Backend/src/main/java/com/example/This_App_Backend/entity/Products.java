package com.example.This_App_Backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @ManyToOne // Many products can belong to one store
    @JoinColumn(name = "store_id", nullable = false) // Foreign key to Stores table
    private Stores store;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_description")
    private String productDescription;

    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Relationships ---

    // One Product can be in many Order_Items
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Order_Items> orderItems = new ArrayList<>();

    // One Product can have many Inventory_Transactions
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Inventory_Transactions> inventoryTransactions = new ArrayList<>();

    // One Product can have many Reviews_And_Ratings
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review_And_Ratings> reviews = new ArrayList<>();

    // One Product can be in many Wishlists items
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Wishlist> wishlists = new ArrayList<>();

    // One Product can be in many Cart items
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
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
