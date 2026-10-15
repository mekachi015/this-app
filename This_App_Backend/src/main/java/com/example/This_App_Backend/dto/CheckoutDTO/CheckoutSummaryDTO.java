package com.example.This_App_Backend.dto.CheckoutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSummaryDTO {
    private Long userId;
    private List<StoreOrderSummary> storeOrders;
    private BigDecimal grandTotal;
    private Integer totalStores;
    private Long totalItems;
}
