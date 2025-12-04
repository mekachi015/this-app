package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.Order_Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemsRepository extends JpaRepository<Order_Items, Long> {
}
