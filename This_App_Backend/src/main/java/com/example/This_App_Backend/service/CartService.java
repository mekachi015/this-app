package com.example.This_App_Backend.service;

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
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private StoreRepository storeRepos;

   private CartResponse maptoResponse(Cart cart){
       ProductResponse productDTO = ProductResponse.builder()
               .productId(cart.getProduct().getProductId())
               .productName(cart.getProduct().getProductName())
               .productPrice(cart.getProduct().getProductPrice())
               .productDescription(cart.getProduct().getProductDescription())
               .imageUrl(cart.getProduct().getImageUrl())
               .build();

       return CartResponse.builder()
               .cartId(cart.getCartItemId())
               .quantity(cart.getQuantity())
               .productResponse(productDTO)
               .build();
   }


   private User validateCustomer(Long userId){
       User user = userRepo.findById(userId)
               .orElseThrow(() -> new RuntimeException("User has not been found"));

       if(user.getUserType() != User.UserType.CUSTOMER){
           throw new SecurityException("Access denied: Only customer can interact with the cart");
       }
       return user;
   }

    public Cart addToCart(Long userId, Long productId, Long quantity) {

        User user = validateCustomer(userId);

        Products product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Stores store = storeRepos.findById(product.getStore().getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Check if product already exists in cart
        Optional<Cart> existing = cartRepo
                .findByUserUserIdAndProductProductId(userId, productId);

        //ensure quantity is positive
        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must not be less than 0");
        }
        Cart savedCart;
        if (existing.isPresent()) {
           Cart cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setUpdatedAt(LocalDateTime.now()); // Update timestamp on modification
            savedCart = cartRepo.save(cartItem);
        }else {
            // Create new cart entry
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setProduct(product);
            cart.setStore(store);
            cart.setQuantity(quantity);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            savedCart = cartRepo.save(cart);
        }



        return cartRepo.save(savedCart);
    }

    //Retrieve all cart items for a specific user
    public List<CartResponse> getCart(Long userId){
        validateCustomer(userId);

        List<Cart> cartEntities = cartRepo.findByUserUserId((userId));

        return cartEntities.stream()
                .map(this::maptoResponse)
        .toList();
    }

    //remove a specific product froma users cart
    public void removeCartItem(Long userId, Long productId){
        validateCustomer(userId);

        Products products = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Products not found"));


        cartRepo.deleteByUserUserIdAndProductProductId(userId, productId);
    }

    //Update the quantities of a specific item in the catt
    public CartResponse updateQuantity(Long cartItemId, Long quantity){
        Cart cart = cartRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        validateCustomer(cart.getUser().getUserId());

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero. Use removeCartItem to delete.");
        }

        cart.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart updatedCart = cartRepo.save(cart);

        // Return the mapped DTO instead of the raw entity
        return maptoResponse(updatedCart);
    }


}
