package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.entity.CustomerOrders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<CustomerOrders, Long> {
    List<CustomerOrders> findByOrderStatusAndIsAssignedDriverFalseOrderByOrderDateDesc(OrderStatus orderStatus);

}
