package com.example.This_App_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.This_App_Backend.entity.WalletTransactions;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactions, Long>{
    //all transactions for a wallet, newest first
    List<WalletTransactions> findByWallet_WalletIdOrderByCreatedAtDesc(Long walletId);
}
