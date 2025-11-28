package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.CartDTO.CartRequest;
import com.example.This_App_Backend.dto.CartDTO.CartResponse;
import com.example.This_App_Backend.dto.CartDTO.QuantityUpdateRequest;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartContoller {

    @Autowired
    private CartService cartService;

    //Add products to cart
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addToCart (
            @PathVariable Long userId,
            @RequestBody CartRequest cartRequest
    ) {
        Cart cartItemDto = cartService.addToCart( // Call now returns a DTO
                userId,
                cartRequest.getProductId(),
                cartRequest.getQuantity()
        );

        return ResponseEntity.created(null).bodygi(cartItemDto);
    }

    //Get entire cart for a specific user
    @GetMapping("/{userId}")
    public ResponseEntity<List<CartResponse>> getCart (@PathVariable Long userId){
        List<CartResponse> cartItems = cartService.getCart(userId); // Call now returns List<CartResponse>
        return ResponseEntity.ok(cartItems);
    }

    //Update the quantity of a specific cart item
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody QuantityUpdateRequest updateRequest
    ){
        CartResponse updateCartItemDto = cartService.updateQuantity(cartItemId, updateRequest.getQuantity()); // Call now returns a DTO

        return ResponseEntity.ok(updateCartItemDto);
    }

    //Remove a specific item from a user cart
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeCartItem (
            @PathVariable Long userId,
            @PathVariable Long productId
    ){
        cartService.removeCartItem(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
