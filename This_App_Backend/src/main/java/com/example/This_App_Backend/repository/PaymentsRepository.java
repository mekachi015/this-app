package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.Enuma.EscrowStatus;
import com.example.This_App_Backend.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {
    //find the held payment for a specific order for release
    Optional<Payments> findByOrder_OrderIdAndEscrowStatus(Long orderId, EscrowStatus escrowStatus);
}
