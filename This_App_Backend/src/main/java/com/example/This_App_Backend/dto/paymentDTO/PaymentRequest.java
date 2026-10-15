package com.example.This_App_Backend.dto.paymentDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private Long userId;
    private Long deliveryMethodId;
    private Long deliveryAddressId;
    private List<CartItemRequest> cartItems;
}
