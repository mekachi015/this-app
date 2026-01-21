package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.DeliveryAddressDTO.DeliveryAddressDTO;
import com.example.This_App_Backend.dto.DriversDTO.DriverDTO;
import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.dto.OrderDTO.OrderItemsDTO;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.UserRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepo;

    //Get all orders for the user
    public List<OrderDTO> getUserOrders (Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if(user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can view their orders");
        }

        List<CustomerOrders> orders = orderRepo.findByUserOrderByOrderDateDesc(user);
        return  orders.stream()
                .map(this:: convertToDTO)
                .collect(Collectors.toList());
    }

    //Get specific order by id
    public OrderDTO getOrderById(Long userId, Long orderId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can view orders");
        }

        CustomerOrders order = orderRepo.findByOrderIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this user"));

        return convertToDTO(order);
    }

    //get order count for users
    public Long getOrderCount(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getUserType() != User.UserType.CUSTOMER){
            return 0L;
        }

        return orderRepo.countByUser(user);
    }

    //Convert entity to DTO
    private OrderDTO convertToDTO(CustomerOrders orders){
        OrderDTO dto = new OrderDTO();

        dto.setOrderId(orders.getOrderId());
        dto.setOrderStatus(orders.getOrderStatus());
        dto.setTotalAmount(orders.getTotalAmount());
        dto.getShippingAmount(orders.getShippingAmount());

        //Calculate subtotal
        BigDecimal subtotal = orders.getTotalAmount().subtract(
                orders.getShippingAmount() != null ? orders.getShippingAmount() : BigDecimal.ZERO
        );
        dto.setSubTotal(subtotal);

        dto.setOrderDate(orders.getOrderDate());
        dto.setEstimatedDeliveryDate(orders.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(orders.getActualDeliveryDate());
        dto.setIsAssignedDriver(orders.getIsAssignedDriver());
        dto.setCreatedAt(orders.getCreatedAt());
        dto.setUpdatedAt(orders.getUpdatedAt());

        //Store details
        if(orders.getStore() != null){
            dto.setStoreId(orders.getStore().getStoreId());
            dto.setStoreName(orders.getStore().getStoreName());
        }

        //Delivery address
        if(orders.getDeliveryAddress() != null){
            dto.setDeliveryAddress(convertAddressToDTO(orders.getDeliveryAddress()));
        }

        //driver details
        if(orders.getDriver() != null){
            dto.setDriver(convertToDriverDTO(orders.getDriver()));
        }

        //Order items
        List<OrderItemsDTO> itemsDTO = orders.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());

        dto.setItems(itemsDTO);
        dto.setItemCount(itemsDTO.size());

        return dto;
    }

    private OrderItemsDTO convertOrderItemToDTO(Order_Items item){
        OrderItemsDTO dto = new OrderItemsDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());

        //calculate the total item
        BigDecimal itemTotal = item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        dto.setItemTotal(itemTotal);

        return dto;
    }

    private DeliveryAddressDTO convertAddressToDTO(User_Addresses addresses){
        DeliveryAddressDTO dto = new DeliveryAddressDTO();

        dto.setAddressId(addresses.getAddressId());
        dto.setAddressLine1(addresses.getAddressLine1());
        dto.setAddressLine2(addresses.getAddressLine2());
        dto.setCity(addresses.getCity());
        dto.setProvince(addresses.getState());
        dto.setPostalCode(addresses.getPostalCode());

        return dto;
    }

    private DriverDTO convertToDriverDTO(Drivers drivers){
        DriverDTO dto = new DriverDTO();

        dto.setDriverId(drivers.getDriverId());
        dto.setDriverName(drivers.getUser().getFirstName() + " " + drivers.getUser().getLastName());
        dto.setPhoneNumber(drivers.getUser().getPhoneNumber());
        dto.setVehicleInfo(drivers.getVehicleLicensePlate());

        return dto;
    }
}
