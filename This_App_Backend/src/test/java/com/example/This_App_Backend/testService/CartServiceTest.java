package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.This_App_Backend.dto.CartDTO.CartDto;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.CartRepo;
import com.example.This_App_Backend.repository.DriverRepository;
import com.example.This_App_Backend.repository.OrderItemsRepository;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreOwnerRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserAddressesRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.CartService;
import com.example.This_App_Backend.service.WalletService;

public class CartServiceTest {

    @Mock private CartRepo cartRepo;
    @Mock private UserRepository userRepo;
    @Mock private ProductsRepository productRepo;
    @Mock private StoreRepository storeRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private OrderItemsRepository orderItemsRepo;
    @Mock private UserAddressesRepository userAddressRepo;
    @Mock private StoreOwnerRepository storeOwnerRepo;

    @InjectMocks
    private CartService cartService;

     @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // CartService
        cartService = new CartService();
        ReflectionTestUtils.setField(cartService, "cartRepo", cartRepo);
        ReflectionTestUtils.setField(cartService, "userRepo", userRepo);
        ReflectionTestUtils.setField(cartService, "productRepo", productRepo);
        ReflectionTestUtils.setField(cartService, "storeRepo", storeRepo);
        ReflectionTestUtils.setField(cartService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(cartService, "orderItemsRepo", orderItemsRepo);
        ReflectionTestUtils.setField(cartService, "userAddressRepo", userAddressRepo);
    }

    @Test
    void addToCart_throwsWhenUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> cartService.addToCart(1L, 1L, 1L));
    }

    @Test
    void addToCart_throwsWhenUserIsNotCustomer() {
        User admin = buildUser(1L, User.UserType.ADMIN);
        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(1L, 1L, 1L));

        assertTrue(ex.getMessage().contains("Only customers can add to the cart"));
    }

    @Test
    void addToCart_throwsWhenProductNotFound() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> cartService.addToCart(1L, 99L, 1L));
    }

    @Test
    void addToCart_throwsWhenProductHasNoStore() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Products product = buildProduct(1L, "Widget", 10, new BigDecimal("50.00"));
        product.setStore(null);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(1L, 1L, 1L));

        assertTrue(ex.getMessage().contains("not associated with store"));
    }

    @Test
    void addToCart_throwsWhenAddingItemFromDifferentStore() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Stores store1 = buildStore(1L, "Store One");
        Stores store2 = buildStore(2L, "Store Two");

        Products product = buildProduct(1L, "Widget", 10, new BigDecimal("50.00"));
        product.setStore(store2); // product belongs to store2

        // Cart already has item from store1
        Cart existingItem = new Cart();
        existingItem.setStore(store1);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(existingItem));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(1L, 1L, 1L));

        assertTrue(ex.getMessage().contains("different store"));
    }

    @Test
    void addToCart_createsNewCartItemWhenNotExists() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Stores store = buildStore(1L, "Test Store");
        Products product = buildProduct(1L, "Widget", 10, new BigDecimal("50.00"));
        product.setStore(store);

        Cart savedCart = new Cart();
        savedCart.setCartItemId(1L);
        savedCart.setUser(customer);
        savedCart.setProduct(product);
        savedCart.setStore(store);
        savedCart.setQuantity(2L);
        savedCart.setCreatedAt(LocalDateTime.now());
        savedCart.setUpdatedAt(LocalDateTime.now());

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUser(customer)).thenReturn(Collections.emptyList());
        when(cartRepo.findByUserAndProduct(customer, product)).thenReturn(Optional.empty());
        when(cartRepo.save(any(Cart.class))).thenReturn(savedCart);

        CartDto result = cartService.addToCart(1L, 1L, 2L);

        assertNotNull(result);
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    void addToCart_updatesQuantityWhenItemAlreadyInCart() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Stores store = buildStore(1L, "Test Store");
        Products product = buildProduct(1L, "Widget", 10, new BigDecimal("50.00"));
        product.setStore(store);

        Cart existingCart = new Cart();
        existingCart.setCartItemId(1L);
        existingCart.setUser(customer);
        existingCart.setProduct(product);
        existingCart.setStore(store);
        existingCart.setQuantity(2L); // already has 2

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(existingCart));
        when(cartRepo.findByUserAndProduct(customer, product)).thenReturn(Optional.of(existingCart));
        when(cartRepo.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartDto result = cartService.addToCart(1L, 1L, 3L);

        // 2 existing + 3 new = 5
        assertEquals(5L, existingCart.getQuantity());
    }

    @Test
    void getUserCart_throwsForNonCustomer() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));

        assertThrows(RuntimeException.class,
                () -> cartService.getUserCart(1L));
    }

    @Test
    void getUserCart_returnsEmptyListWhenCartIsEmpty() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(Collections.emptyList());

        List<CartDto> result = cartService.getUserCart(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateCartQuantity_throwsWhenQuantityIsZero() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Cart cart = new Cart();
        cart.setCartItemId(1L);
        cart.setUser(customer);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.updateCartQuantity(1L, 1L, 0L));

        assertTrue(ex.getMessage().contains("Quantity must be greater than 0"));
    }

    @Test
    void updateCartQuantity_throwsWhenCartDoesNotBelongToUser() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        User otherUser = buildUser(99L, User.UserType.CUSTOMER);

        Cart cart = new Cart();
        cart.setCartItemId(1L);
        cart.setUser(otherUser); // belongs to someone else

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class,
                () -> cartService.updateCartQuantity(1L, 1L, 5L));
    }

    @Test
    void removeFromCart_deletesCartItem() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Cart cart = new Cart();
        cart.setCartItemId(1L);
        cart.setUser(customer);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));

        cartService.removeFromCart(1L, 1L);

        verify(cartRepo, times(1)).delete(cart);
    }

    @Test
    void clearCart_deletesAllUserCartItems() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));

        cartService.clearCart(1L);

        verify(cartRepo, times(1)).deleteByUser(customer);
    }

    @Test
    void getCartItemCount_returnsZeroForNonCustomer() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));

        Long count = cartService.getCartItemCount(1L);

        assertEquals(0L, count);
    }

    @Test
    void getCartItemCount_returnsCountForCustomer() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.countByUser_UserId(1L)).thenReturn(3L);

        Long count = cartService.getCartItemCount(1L);

        assertEquals(3L, count);
    }

    // Helper methods to build test data
       private User buildUser(Long id, User.UserType type) {
        User user = new User();
        user.setUserId(id);
        user.setUserType(type);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("user" + id);
        return user;
    }

    private Stores buildStore(Long id, String name) {
        Stores store = new Stores();
        store.setStoreId(id);
        store.setStoreName(name);
        return store;
    }

    private Products buildProduct(Long id, String name, int stock, BigDecimal price) {
        Products product = new Products();
        product.setProductId(id);
        product.setProductName(name);
        product.setStockQuantity(stock);
        product.setProductPrice(price);
        return product;
    }
}
