package com.example.This_App_Backend.dto.CheckoutDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreOrderSummary {
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private List<CheckoutItemDTO> items;
    private BigDecimal storeTotal;
}
