package com.example.This_App_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.This_App_Backend.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    //Find a wallet by the user who owns it 
    Optional<Wallet> findByUser_UserId(Long userId);
}
