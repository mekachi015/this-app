package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.CustomerOrders;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrders,Long> {
}


