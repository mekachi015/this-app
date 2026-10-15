package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.CartDTO.CartDto;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.*;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import  com.example.This_App_Backend.Enuma.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

      // Check if user already has items from a different store
      List<Cart> existingCartItems = cartRepo.findByUser(user);
      if (!existingCartItems.isEmpty()) {
          Stores existingStore = existingCartItems.get(0).getStore();
          if (!existingStore.getStoreId().equals(store.getStoreId())) {
              throw new RuntimeException("Cannot add items from different store yet");
          }
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
    public List<CustomerOrders> checkout(Long userId, Long deliveryAddressId) {
        // 1. Validate User
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can checkout");
        }

        // 2. Get cart items
        List<Cart> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty. Nothing to checkout");
        }

        // 3. Validate and get delivery address
        User_Addresses deliveryAddress = validateAndGetDeliveryAddress(userId, user, deliveryAddressId);

        // 4. Group cart items by store
        Map<Stores, List<Cart>> itemsByStore = cartItems.stream()
                .collect(Collectors.groupingBy(Cart::getStore));

        // 5. Create separate orders for each store
        List<CustomerOrders> createdOrders = new ArrayList<>();
        BigDecimal shippingAmountPerStore = new BigDecimal("150.00"); // R150 shipping per store

        for (Map.Entry<Stores, List<Cart>> entry : itemsByStore.entrySet()) {
            Stores store = entry.getKey();
            List<Cart> storeCartItems = entry.getValue();

            // Calculate total amount for this store's items
            BigDecimal storeTotalAmount = BigDecimal.ZERO;

            for (Cart cartItem : storeCartItems) {
                Products product = cartItem.getProduct();

                // Check stock availability
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new RuntimeException(
                            String.format("Insufficient stock for %s. Available: %d, Requested: %d",
                                    product.getProductName(),
                                    product.getStockQuantity(),
                                    cartItem.getQuantity())
                    );
                }

                BigDecimal itemTotal = product.getProductPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                storeTotalAmount = storeTotalAmount.add(itemTotal);
            }

            // Create order for this store
            CustomerOrders order = new CustomerOrders();
            order.setUser(user);
            order.setStore(store); // Set the associated store
            order.setOrderStatus(OrderStatus.PENDING);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(storeTotalAmount.add(shippingAmountPerStore)); // Include shipping
            order.setShippingAmount(shippingAmountPerStore); // Set shipping amount
            order.setDeliveryAddress(deliveryAddress);
            order.setIsAssignedDriver(false);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // Create order items for this store
            List<Order_Items> orderItems = new ArrayList<>();
            for (Cart cartItem : storeCartItems) {
                Products product = cartItem.getProduct();

                Order_Items orderItem = new Order_Items();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity().intValue());
                orderItem.setPriceAtPurchase(product.getProductPrice());
                orderItem.setCreatedAt(LocalDateTime.now());

                orderItems.add(orderItem);

                // Update stock
                product.setStockQuantity(
                        product.getStockQuantity() - cartItem.getQuantity().intValue()
                );
                productRepo.save(product);
            }

            order.setOrderItems(orderItems);

            // Save the order
            CustomerOrders savedOrder = orderRepo.save(order);
            createdOrders.add(savedOrder);

            // Print success message for this store's order
            System.out.println("------------------------------------");
            System.out.println("ORDER CREATED FOR STORE: " + store.getStoreName());
            System.out.println("ORDER ID: " + savedOrder.getOrderId());
            System.out.println("SUBTOTAL: " + storeTotalAmount);
            System.out.println("SHIPPING: " + shippingAmountPerStore);
            System.out.println("TOTAL AMOUNT: " + savedOrder.getTotalAmount());
            System.out.println("------------------------------------");
        }

        // 8. Clear the cart after successful order creation
        cartRepo.deleteAll(cartItems);

        // 9. Print overall success message
        System.out.println("====================================");
        System.out.println("CHECKOUT SUCCESSFUL FOR USER ID: " + userId);
        System.out.println("TOTAL ORDERS CREATED: " + createdOrders.size());
        BigDecimal grandTotal = createdOrders.stream()
                .map(CustomerOrders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("GRAND TOTAL: " + grandTotal);
        System.out.println("====================================");

        return createdOrders;
    }



    //Helper method to validate and get delivery address
    private User_Addresses validateAndGetDeliveryAddress(Long userId, User user, Long deliveryAddressId) {
        User_Addresses deliveryAddress;

        if (deliveryAddressId != null) {
            // Use the specified address
            deliveryAddress = userAddressRepo.findById(deliveryAddressId)
                    .orElseThrow(() -> new RuntimeException("Delivery address not found"));

            // Security check: Ensure it belongs to the user
            if (!deliveryAddress.getUser().getUserId().equals(userId)) {
                throw new RuntimeException("This address does not belong to the authenticated user");
            }
        } else {
            // Try to get default address first
            deliveryAddress = userAddressRepo.findFirstByUserAndIsDefault(user, true)
                    .orElseGet(() -> userAddressRepo.findFirstByUser(user)
                            .orElseThrow(() -> new RuntimeException(
                                    "No delivery address found. Please add a delivery address before checkout."
                            )));
        }

        // Validate address completeness
        if (deliveryAddress.getStreetNumber() == null ||
                deliveryAddress.getStreetName() == null ||
                deliveryAddress.getSuburb() == null ||
                deliveryAddress.getCity() == null ||
                deliveryAddress.getProvince() == null ||
                deliveryAddress.getPostalCode() == null) {
            throw new RuntimeException("Delivery address is incomplete. Please update your address.");
        }

        return deliveryAddress;
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
