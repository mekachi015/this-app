package com.example.This_App_Backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.This_App_Backend.Enuma.PendingCheckoutStatus;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pending_checkout")
@Data
@NoArgsConstructor
@Getter
@Setter
public class PendingCheckout {

    @Id
    @Column(name = "pending_checkout_id", length = 36)
    private String pendingCheckoutId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "delivery_address_id", nullable = false)
    private Long deliveryAddressId;

    @Column(name = "total_amount", nullable = false, precision = 10 , scale = 2)
    private BigDecimal totalAmount;

    @Column(name =  "shipping_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "store_id", nullable = false)
    private Long storeId;


    //Json snapshot of cart at moment checkout is initiated
    @Column(name = "cart_snapshot", columnDefinition = "TEXT", nullable = false)
    private String cartSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PendingCheckoutStatus status;

    @Column(name = "payfast_payment_id")
    private String payfastPaymentId; // payfast's own payment ID from ITN

    @Column(name = "expited_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
