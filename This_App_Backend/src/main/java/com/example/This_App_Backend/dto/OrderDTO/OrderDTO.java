package com.example.This_App_Backend.dto.OrderDTO;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.dto.DeliveryAddressDTO.DeliveryAddressDTO;
import com.example.This_App_Backend.dto.DriversDTO.DriverDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
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
    private OrderStatus orderStatus;
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
    private String storeAddress; // for delivery start position

    //Delivery address
    private DeliveryAddressDTO deliveryAddress;

    //driver details (if assigned)
    private DriverDTO driver;

    //Order items
    private List<OrderItemsDTO> items;
    private Integer itemCount;

    private String checkoutSessionId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void getShippingAmount(BigDecimal shippingAmount) {
        return;
    }
}
