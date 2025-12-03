package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.CartDTO.CartDto;
import com.example.This_App_Backend.dto.CartDTO.CartResponse;
import com.example.This_App_Backend.dto.CartDTO.ProductResponse;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.CartRepo;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

  @Autowired
    private CartRepo cartRepo;

  @Autowired
    private UserRepository userRepo;

  @Autowired
    private ProductsRepository productRepo;

  @Autowired
    private StoreRepository storeRepo;

  @Transactional
    public CartDto addToCart (Long userId, Long productId, Long quantity){
      //Validate the user
      User user = userRepo.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found error"));

      if(user.getUserType() != User.UserType.CUSTOMER){
          throw new RuntimeException("Only customers can add to the cart");
      }

      //Validate product exist
      Products products = productRepo.findById(productId)
              .orElseThrow(() -> new RuntimeException("Product not found"));

      //get store from product
      Stores store = products.getStore();
      if (store == null) {
          throw new RuntimeException("Product is not associated with store");
      }

      //Check if product already exists
      Cart cart = cartRepo.findByUserAndProduct(user, products)
              .orElse(new Cart());

      if (cart.getCartItemId() != null){
          //update existing cart  item
          cart.setQuantity(cart.getQuantity() + quantity);
          cart.setUpdatedAt(LocalDateTime.now());
      } else {
          //create a new cart item
          cart.setUser(user);
          cart.setProduct(products);
          cart.setStore(store);
          cart.setQuantity(quantity);
          cart.setCreatedAt(LocalDateTime.now());
          cart.setUpdatedAt(LocalDateTime.now());
      }

      Cart savedCart = cartRepo.save(cart);
      return convertToDto(savedCart);
  }

  @Transactional(readOnly = true)
    public List<CartDto> getUserCart(Long userId){
      User user = userRepo.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found"));

      if (user.getUserType() !=  User.UserType.CUSTOMER){
          throw new RuntimeException("Only customers can view carts");
      }

      List<Cart> cartItem = cartRepo.findByUser(user);
      return cartItem.stream()
              .map(this::convertToDto)
              .collect(Collectors.toList());
  }

  @Transactional
    public CartDto updateCartQuantity(Long userId,  Long cartItemId, Long quantity){
      User user = userRepo.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found"));

      if(user.getUserType() != User.UserType.CUSTOMER){
          throw new RuntimeException("Only customers can update a cart");
      }

      Cart cart = cartRepo.findById(cartItemId)
              .orElseThrow(() -> new RuntimeException("cart item does not exist"));

      if(!cart.getUser().getUserId().equals(userId)){
          throw new RuntimeException("Cart item does not belong to this user");
      }

      if (quantity <= 0){
          throw new RuntimeException("Quantity must be greater than 0");
      }

      cart.setQuantity(quantity);
      cart.setUpdatedAt(LocalDateTime.now());

      Cart updateCart = cartRepo.save(cart);
      return convertToDto(updateCart);
  }

  @Transactional
    public void removeFromCart(Long userId, Long cartItemId){
      User user = userRepo.findById(userId)
              .orElseThrow(() -> new RuntimeException("User not found "));

      if (user.getUserType() != User.UserType.CUSTOMER){
          throw new RuntimeException("Only customers can remover cart items");
      }

      Cart cart = cartRepo.findById(cartItemId)
              .orElseThrow(() -> new RuntimeException("Cart item not found"));

      if (!cart.getUser().getUserId().equals(userId)){
          throw new RuntimeException("Cart item does not belong to this user");
      }

      cartRepo.delete(cart);
  }

  @Transactional
    public void clearCart(Long userId){
      User user = userRepo.findById(userId)
              .orElseThrow(()-> new RuntimeException("User not found"));

      if (user.getUserType() != User.UserType.CUSTOMER){
          throw new RuntimeException("Only customers can delete cart items");
      }

      cartRepo.deleteByUser(user);
  }

  @Transactional (readOnly = true)
    public Long getCartItemCount (Long userId){
      User user = userRepo.findById(userId)
              .orElseThrow(() ->  new RuntimeException("User not found with ID:" + userId));

      if (user.getUserType() != User.UserType.CUSTOMER){
          return 0L;
      }

      return cartRepo.countByUser_UserId(userId);
  }

  private CartDto convertToDto (Cart cart) {
      CartDto dto = new CartDto();

      dto.setCartItemId(cart.getCartItemId());
      dto.setUserId(cart.getUser().getUserId());
      dto.setProductId(cart.getProduct().getProductId());
      dto.setProductName(cart.getProduct().getProductName());
      dto.setProductImage(cart.getProduct().getImageUrl());
      dto.setProductPrice(cart.getProduct().getProductPrice().doubleValue());
      dto.setStoreId(cart.getStore().getStoreId());
      dto.setStoreName(cart.getStore().getStoreName());
      dto.setQuantity(cart.getQuantity());
      dto.setSubtotal(cart.getProduct().getProductPrice().doubleValue() * cart.getQuantity());
      dto.setCreatedAt(cart.getCreatedAt());
      dto.setUpdatedAt(cart.getUpdatedAt());

      return dto;
  }
}
