package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.CartDTO.CartDto;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.*;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @Autowired
  private OrderRepository orderRepo;

  @Autowired
  private OrderItemsRepository orderItemsRepo;

  @Autowired
  private UserAddressesRepository userAddressRepo;

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

    @Transactional
    public CustomerOrders checkout(Long userId, Long deliveryAddressId) {
        // Validate user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can checkout");
        }

        List<Cart> cartItems = cartRepo.findByUser(user);
        if(cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Nothing to checkout.");
        }

        // Check if deliveryAddressId is provided
        User_Addresses deliveryAddress = null;

        if (deliveryAddressId != null) {
            // Use the provided address
            deliveryAddress = userAddressRepo.findById(deliveryAddressId)
                    .orElseThrow(() -> new RuntimeException("Delivery address not found"));

            if(!deliveryAddress.getUser().getUserId().equals(userId)) {
                throw new RuntimeException("Delivery address does not belong to this user");
            }
        } else {
            // Try to find a default address for the user
            deliveryAddress = userAddressRepo.findFirstByUserAndIsDefault(user, true)
                    .orElseGet(() -> userAddressRepo.findFirstByUser(user)
                            .orElseThrow(() -> new RuntimeException(
                                    "No delivery address found. Please add a delivery address before checkout."
                            )));
        }

        // Additional validations
        if (deliveryAddress == null) {
            throw new RuntimeException("Delivery address is required for checkout");
        }

        // Validate address completeness
        if (deliveryAddress.getAddressLine1() == null || deliveryAddress.getAddressLine1().trim().isEmpty()) {
            throw new RuntimeException("Delivery address is incomplete. Please update your address.");
        }

        // Check all items are from the same store
        Stores primaryStore = cartItems.get(0).getStore();
        boolean allSameStore = cartItems.stream()
                .allMatch(item -> item.getStore().getStoreId().equals(primaryStore.getStoreId()));

        if (!allSameStore) {
            throw new RuntimeException("All items must be from the same store for checkout");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Create order
        CustomerOrders newCustomerOrders = new CustomerOrders();
        newCustomerOrders.setUser(user);
        newCustomerOrders.setStore(primaryStore);
        newCustomerOrders.setOrderStatus("PENDING");
        newCustomerOrders.setDeliveryAddress(deliveryAddress);
        newCustomerOrders.setIsAssignedDriver(false);
        newCustomerOrders.setOrderDate(LocalDateTime.now());
        newCustomerOrders.setCreatedAt(LocalDateTime.now());
        newCustomerOrders.setUpdatedAt(LocalDateTime.now());

        // Create order items and calculate total amount
        List<Order_Items> orderItems = new ArrayList<>();
        for (Cart item : cartItems) {
            Products product = item.getProduct();
            BigDecimal priceAtPurchase = product.getProductPrice();

            // Check stock availability
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException(
                        String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                                product.getProductName(), product.getStockQuantity(), item.getQuantity())
                );
            }

            Order_Items itemsOrdered = new Order_Items();
            itemsOrdered.setOrder(newCustomerOrders);
            itemsOrdered.setProduct(product);
            itemsOrdered.setQuantity(item.getQuantity().intValue());
            itemsOrdered.setPriceAtPurchase(priceAtPurchase);
            itemsOrdered.setCreatedAt(LocalDateTime.now());

            orderItems.add(itemsOrdered);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity().intValue());
            productRepo.save(product);

            BigDecimal itemTotal = priceAtPurchase.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        newCustomerOrders.setTotalAmount(totalAmount);
        newCustomerOrders.setOrderItems(orderItems);

        CustomerOrders savedOrder = orderRepo.save(newCustomerOrders);

        // Clear the cart after successful order creation
        cartRepo.deleteAll(cartItems);

        return savedOrder;
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
