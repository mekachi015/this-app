package com.example.This_App_Backend.dto.OrderDTO;

import com.example.This_App_Backend.dto.DeliveryAddressDTO.DeliveryAddressDTO;
import com.example.This_App_Backend.dto.DriversDTO.DriverDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal shippingAmount;
    private BigDecimal subTotal;
    private LocalDateTime orderDate;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private Boolean isAssignedDriver;

    //Store details
    private Long storeId;
    private String storeName;

    //Delivery address
    private DeliveryAddressDTO deliveryAddress;

    //driver details (if assigned)
    private DriverDTO driver;

    //Order items
    private List<OrderItemsDTO> items;
    private Integer itemCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void getShippingAmount(BigDecimal shippingAmount) {
        return;
    }
}
