package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart,Long> {

    Optional<Cart> findByUserUserIdAndProductProductId(Long userId, Long productId);

    List<Cart> findByUserUserId(Long userId);

    void deleteByUserUserIdAndProductProductId(Long userId, Long productId);

    Optional<Cart> findById(Long cartItemId);
}
