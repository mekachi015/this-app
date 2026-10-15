package com.example.This_App_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.This_App_Backend.Enuma.PendingCheckoutStatus;
import com.example.This_App_Backend.entity.PendingCheckout;

@Repository
public interface PendingCheckoutRepository extends JpaRepository<PendingCheckout, String> {

    Optional<PendingCheckout> findByPendingCheckoutIdAndStatus(
        String id, PendingCheckoutStatus status
    );
}
