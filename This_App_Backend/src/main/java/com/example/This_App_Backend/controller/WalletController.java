package com.example.This_App_Backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.This_App_Backend.dto.WalletDTO.WalletBalanceDTO;
import com.example.This_App_Backend.dto.WalletDTO.WalletTransactionDTO;
import com.example.This_App_Backend.service.WalletService;
import java.util.List;


@RestController
@RequestMapping("/api/wallet")
public class WalletController {


    @Autowired
    private WalletService walletService;

    //Returns the current wallet balance for an admin or driver
    @GetMapping("/{userId}/balance")
    public ResponseEntity<?> getBalance(
        @PathVariable Long userId
    ) {
        try{
            WalletBalanceDTO balance = walletService.getBalance(userId);
            return ResponseEntity.ok(balance);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Return the full transaction history for an admin or driver
    @GetMapping("/{userId}/transaction")
    public ResponseEntity<?> getTransactions(
        @PathVariable Long userId
    ){
        try{
            List<WalletTransactionDTO> transactions = walletService.getTransactions(userId);
            return ResponseEntity.ok(transactions);       
        } catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
