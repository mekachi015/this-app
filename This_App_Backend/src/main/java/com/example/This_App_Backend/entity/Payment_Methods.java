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
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment_Methods {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "method_id")
    private Integer methodId; // Unique identifier for the payment method

    @ManyToOne(fetch = FetchType.LAZY)  
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to Users table
    private User user; // Link to the User entity

    @Column(name = "method_type", nullable = false, length = 50)
    private String methodType; // Type of payment method (e.g., Credit Card, PayPal, etc.)

    @Column(name = "card_last_four", length = 4)    
    private String cardLastFour; // Last four digits of the card (if applicable)

    @Column(name = "card_brand", length = 50)
    private String cardBrand; // Brand of the card (e.g., Visa, MasterCard, etc.)

    @Column(name = "expiration_date", length = 7) // Format: MM/YYYY
    private String expirationDate; // Expiration date of the card (if applicable)   

    @Column(name = "is_default")
    private Boolean isDefault; // Indicates if this is the default payment method for the user

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

     // Optional: Many Payment_Methods can link to one User_Address for billing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id") // Maps to billing_address_id in payment_methods table (can be null)
    private User_Addresses billingAddress;

    // --- Relationships ---
    // One Payment_Method can be used for many Payments
    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.ALL)
    private List<Payments> payments = new ArrayList<>();
}
