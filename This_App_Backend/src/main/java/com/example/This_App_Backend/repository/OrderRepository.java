package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.User;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrders,Long> {

    //find all orders for a specific user
    List<CustomerOrders> findByUserOrderByOrderDateDesc(User user);

    //Find by order id and user id
    Optional<CustomerOrders> findByOrderIdAndUser(Long orderId, User user);

    //Find order by status for user
    List<CustomerOrders> findByUserAndOrderStatusOrderByOrderDateDesc(User user, String orderStatus);

    //Count orders for a user
    Long countByUser(User user);
}


