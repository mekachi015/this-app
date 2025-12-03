package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // Find all wishlist items for a specific user
    List<Wishlist> findByUser(User user);

    // Find all wishlist items for a user by user ID
    List<Wishlist> findByUser_UserId(Long userId);

    // Check if a product is in user's wishlist
    Optional<Wishlist> findByUserAndProduct(User user, Products product);

    // Check if a store is in user's wishlist
    Optional<Wishlist> findByUserAndStore(User user, Stores store);

    // Find by user ID and product ID
    Optional<Wishlist> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    // Find by user ID and store ID
    Optional<Wishlist> findByUser_UserIdAndStore_StoreId(Long userId, Long storeId);

    // Delete all wishlist items for a user
    void deleteByUser(User user);

    // Count wishlist items for a user
    Long countByUser_UserId(Long userId);
}
