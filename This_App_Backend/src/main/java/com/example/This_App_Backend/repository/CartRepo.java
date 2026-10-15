package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    List<Cart> findByUser(User user);

    List<Cart> findByUser_UserId(Long userId);

    // This is the correct method - keep this one
    Optional<Cart> findByUserAndProduct(User user, Products product);

    Optional<Cart> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    // REMOVE THIS LINE - it's causing the error
    // Optional<Cart> findByUserAndUserProduct(User user, Products product);

    void deleteByUser(User user);

    Long countByUser_UserId(Long userId);
}
