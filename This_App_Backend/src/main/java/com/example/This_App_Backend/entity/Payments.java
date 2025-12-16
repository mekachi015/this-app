package com.example.This_App_Backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.This_App_Backend.Enuma.PaymentStatus;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    //Ozow specific
    @Column(name ="transaction_reference", unique = true, nullable = false)
    private String transactionReference;

    @Column(name = "ozow_transactions_id")
    private String ozowTransactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Many Payments belong to one Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false) // Maps to order_id in payments table
    private CustomerOrders order;

    //the store that recieved the payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Stores store;

    //Owner who recieved the money
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_owner_id", nullable = false)
    private Store_Owners storeOwners;

    //Customer who paid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(name ="payment_status", nullable = false)
    private PaymentStatus paymentStatus;


    // Many Payments use one Payment_Method
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false) // Maps to payment_method_id in payments table
    private Payment_Methods paymentMethod;


}
