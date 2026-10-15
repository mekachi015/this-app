package com.example.This_App_Backend.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory_Transactions {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    // Many Inventory_Transactions relate to one Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Maps to product_id in inventory_transactions table
    private Products product;

    // Many Inventory_Transactions relate to one Store
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false) // Maps to store_id in inventory_transactions table
    private Stores store;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // 'stock_in', 'stock_out', 'adjustment', 'return'

    @Column(name = "quantity_changed", nullable = false)
    private Integer quantityChanged;

    @Column(name = "new_stock_level", nullable = false)
    private Integer newStockLevel;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

     // Optional: Many Inventory_Transactions can be performed by one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id") // Maps to performed_by_user_id (can be null)
    private User performedByUser;

    @Column(name = "comment_on_transaction", columnDefinition = "TEXT")
    private String commentOnTransaction;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    // No @LastModifiedDate as transactions are usually immutable
   

}
