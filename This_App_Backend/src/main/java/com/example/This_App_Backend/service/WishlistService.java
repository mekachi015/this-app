package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.wishlistDTO.WishlistDto;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.Wishlist;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProductsRepository productRepo;

    @Autowired
    private StoreRepository storeRepo;


    @Transactional
    public WishlistDto addProductToWishlist(Long userId, Long productId) {
        // Validate user exists and is a customer
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can add items to wishlist. User type: " + user.getUserType());
        }

        // Validate product exists
        Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // Check if product already in wishlist
        if (wishlistRepo.findByUserAndProduct(user, product).isPresent()) {
            throw new RuntimeException("Product already in wishlist");
        }

        // Create wishlist item
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setStore(product.getStore());
        wishlist.setCreatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());

        Wishlist savedWishlist = wishlistRepo.save(wishlist);
        return convertToDTO(savedWishlist);
    }

    @Transactional
    public WishlistDto addStoreToWishlist(Long userId, Long storeId) {
        // Validate user exists and is a customer
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can add stores to wishlist. User type: " + user.getUserType());
        }

        // Validate store exists
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        // Check if store already in wishlist
        if (wishlistRepo.findByUser_UserIdAndStore_StoreId(user.getUserId(), store.getStoreId()).isPresent()) {
            throw new RuntimeException("Store already in wishlist");
        }

        // Create wishlist item
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setStore(store);
        wishlist.setProduct(null);
        wishlist.setCreatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());

        Wishlist savedWishlist = wishlistRepo.save(wishlist);
        return convertToDTO(savedWishlist);
    }

    @Transactional(readOnly = true)
    public List<WishlistDto> getUserWishlist(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can view wishlist");
        }

        List<Wishlist> wishlistItems = wishlistRepo.findByUser(user);
        return wishlistItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long wishlistId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can remove items from wishlist");
        }

        Wishlist wishlist = wishlistRepo.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found with ID: " + wishlistId));

        if (!wishlist.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Wishlist item does not belong to this user");
        }

        wishlistRepo.delete(wishlist);
    }

    @Transactional
    public void clearWishlist(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can clear wishlist");
        }

        wishlistRepo.deleteByUser(user);
    }

    @Transactional(readOnly = true)
    public Long getWishlistItemCount(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            return 0L;
        }

        return wishlistRepo.countByUser_UserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isProductInWishlist(Long userId, Long productId) {
        return wishlistRepo.findByUser_UserIdAndStore_StoreId(userId, productId).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isStoreInWishlist(Long userId, Long storeId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User id not found:" + userId));

        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException(" store if with id: not found:" + storeId));

        return wishlistRepo.findByUser_UserIdAndStore_StoreId(user.getUserId(), store.getStoreId()).isPresent();
    }

    private WishlistDto convertToDTO(Wishlist wishlist) {
        WishlistDto dto = new WishlistDto();
        dto.setWishlistId(wishlist.getWishlistId());
        dto.setUserId(wishlist.getUser().getUserId());

        if (wishlist.getProduct() != null) {
            dto.setProductId(wishlist.getProduct().getProductId());
            dto.setProductName(wishlist.getProduct().getProductName());
            dto.setProductImage(wishlist.getProduct().getImageUrl());
            dto.setProductPrice(wishlist.getProduct().getProductPrice().doubleValue());
        }

        if (wishlist.getStore() != null) {
            dto.setStoreId(wishlist.getStore().getStoreId());
            dto.setStoreName(wishlist.getStore().getStoreName());
        }

        dto.setCreatedAt(wishlist.getCreatedAt());
        dto.setUpdatedAt(wishlist.getUpdatedAt());
        return dto;
    }
}
