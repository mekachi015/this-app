package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.DriverRepository;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriverOrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderService orderService;

    //View orders with pending payment status
    public List<OrderDTO> getAvailableOrder(Long userId) {
        verifyDriver(userId);

        List<CustomerOrders> orders = orderRepo.
                findByOrderStatusAndIsAssignedDriverFalseOrderByOrderDateDesc("PENDING_PAYMENT");

        return orders.stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());
    }

    //claim an order
    public OrderDTO claimOrder(Long userId, Long orderId) {
        User driver = verifyDriver(userId);

        CustomerOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if(!order.getOrderStatus().equals("PENDING_PAYMENT")) {
            throw new RuntimeException("Order is not available to be claimed");
        }

        if(Boolean.TRUE.equals(order.getIsAssignedDriver())){
            throw new RuntimeException("Order has already been claimed");
        }

        order.setAssignedDriver(driver); //update entity
        order.setIsAssignedDriver(true);
        order.setOrderStatus("OUT_FOR_DELIVERY");
        return orderService.convertToDTO(orderRepo.save(order));
    }

    //Update order status of a claimed order
    public OrderDTO updateOrderStatus(Long userId, Long orderId, String newStatus) {
        User driver = verifyDriver(userId);

        CustomerOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if order has a driver assigned at all
        if (order.getAssignedDriver() == null) {
            throw new RuntimeException("This order has no assigned driver");
        }

        // Check if the assigned driver matches the requesting user
        if (!order.getAssignedDriver().getUserId().equals(driver.getUserId())) {
            throw new RuntimeException("This order is not assigned to you");
        }

        validateStatusTransition(order.getOrderStatus(), newStatus);
        order.setOrderStatus(newStatus);

        return orderService.convertToDTO(orderRepo.save(order));
    }

    //View all orders assigned to this driver
    public List<OrderDTO> getMyOrder(Long userId) {
        User driver = verifyDriver(userId);

        List<CustomerOrders> orders = orderRepo
                .findByAssignedDriverOrderByOrderDateDesc(driver);

        return orders.stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());
    }

    //get order by id
    public OrderDTO getOrderById(Long userId, Long orderId) {
        User driver = verifyDriver(userId);

        CustomerOrders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if(order.getAssignedDriver() == null ||
        !order.getAssignedDriver().getUserId().equals(driver.getUserId())){
            throw new RuntimeException("Order is not assigned to you");
        }

        return orderService.convertToDTO(order);
    }

    //---------------Helper methods---------------
    private User verifyDriver(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.DRIVER){
            throw new RuntimeException("Access Denied: User is not driver");
        }

        return user;
    }

    private void validateStatusTransition(String current, String next){
        switch (current) {
            case "OUT_FOR_DELIVERY":
                if (next.equals("DELIVERED") || next.equals("FAILED_DELIVERY")) return;
                break;
            case "DELIVERED":
            case "FAILED_DELIVERY":
                throw new RuntimeException("Order is already in a terminal state: " + current);
            default:
                throw new RuntimeException("Cannot update status from: " + current);
        }
        throw new RuntimeException("Invalid status transition from " + current + " to " + next);
    }




}
