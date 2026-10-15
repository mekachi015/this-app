
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
public class WalletTransactionDTO {
    private Long transactionId;
    private Long orderId;  //null if not order-related
    private BigDecimal amount;
    private String type;  // CREDIT or DEBIT
    private String description;
    private LocalDateTime createdAt;
}
