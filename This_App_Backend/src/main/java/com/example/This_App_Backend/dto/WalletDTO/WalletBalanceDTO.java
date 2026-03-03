package com.example.This_App_Backend.dto.WalletDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WalletBalanceDTO {
    private Long walletId;
    private BigDecimal balance;
    private String ownerName; // first name + last name
    private String ownerType; //Admin or Driver
    private LocalDateTime updatedAt;
}
