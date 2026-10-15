package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.This_App_Backend.dto.wishlistDTO.WishlistDto;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.Wishlist;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.repository.WishlistRepository;
import com.example.This_App_Backend.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ProductsRepository productRepo;

    @Mock
    private StoreRepository storeRepo;

    @InjectMocks
    private WishlistService wishlistService;

    private User buildUser(Long userId, User.UserType type) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("user" + userId);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("user" + userId + "@example.com");
        user.setPhoneNumber("0123456789");
        user.setPassword("password");
        user.setUserType(type);
        return user;
    }

    private Stores buildStore(Long storeId) {
        Stores store = new Stores();
        store.setStoreId(storeId);
        store.setStoreName("Store " + storeId);
        store.setStoreAddress("123 Main St");
        store.setStoreEmail("store" + storeId + "@example.com");
        store.setStorePhoneNumber("0123456789");
        store.setStoreBusinessHours("9am-5pm");
        store.setStoreDescription("Description");
        store.setStoreLogo("http://example.com/logo.png");
        return store;
    }

    private Products buildProduct(Long productId, Stores store) {
        Products product = new Products();
        product.setProductId(productId);
        product.setStore(store);
        product.setProductName("Product " + productId);
        product.setProductDescription("Description");
        product.setProductPrice(java.math.BigDecimal.valueOf(9.99));
        product.setCategory("Category");
        product.setStockQuantity(5);
        product.setImageUrl("http://example.com/product.png");
        return product;
    }

    private Wishlist buildWishlist(Long wishlistId, User user, Products product, Stores store) {
        Wishlist wishlist = new Wishlist();
        wishlist.setWishlistId(wishlistId);
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setStore(store);
        wishlist.setCreatedAt(LocalDateTime.now());
        wishlist.setUpdatedAt(LocalDateTime.now());
        return wishlist;
    }

    @Test
    void addProductToWishlist_whenCustomerAndProductNotInWishlist_shouldReturnDto() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Stores store = buildStore(10L);
        Products product = buildProduct(5L, store);
        Wishlist saved = buildWishlist(100L, customer, product, store);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(5L)).thenReturn(Optional.of(product));
        when(wishlistRepo.findByUserAndProduct(customer, product)).thenReturn(Optional.empty());
        when(wishlistRepo.save(any(Wishlist.class))).thenReturn(saved);

        WishlistDto dto = wishlistService.addProductToWishlist(1L, 5L);

        assertEquals(100L, dto.getWishlistId());
        assertEquals(1L, dto.getUserId());
        assertEquals(5L, dto.getProductId());
        assertEquals("Product 5", dto.getProductName());
        verify(wishlistRepo).save(any(Wishlist.class));
    }

    @Test
    void addProductToWishlist_whenProductAlreadyInWishlist_shouldThrowRuntimeException() {
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        Stores store = buildStore(11L);
        Products product = buildProduct(6L, store);
        Wishlist existing = buildWishlist(101L, customer, product, store);

        when(userRepo.findById(2L)).thenReturn(Optional.of(customer));
        when(productRepo.findById(6L)).thenReturn(Optional.of(product));
        when(wishlistRepo.findByUserAndProduct(customer, product)).thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.addProductToWishlist(2L, 6L));

        assertEquals("Product already in wishlist", exception.getMessage());
    }

    @Test
    void addStoreToWishlist_whenCustomerAndStoreNotInWishlist_shouldReturnDto() {
        User customer = buildUser(3L, User.UserType.CUSTOMER);
        Stores store = buildStore(12L);
        Wishlist saved = buildWishlist(102L, customer, null, store);

        when(userRepo.findById(3L)).thenReturn(Optional.of(customer));
        when(storeRepo.findById(12L)).thenReturn(Optional.of(store));
        when(wishlistRepo.findByUser_UserIdAndStore_StoreId(3L, 12L)).thenReturn(Optional.empty());
        when(wishlistRepo.save(any(Wishlist.class))).thenReturn(saved);

        WishlistDto dto = wishlistService.addStoreToWishlist(3L, 12L);

        assertEquals(102L, dto.getWishlistId());
        assertEquals(12L, dto.getStoreId());
        assertEquals("Store 12", dto.getStoreName());
        verify(wishlistRepo).save(any(Wishlist.class));
    }

    @Test
    void addStoreToWishlist_whenStoreAlreadyInWishlist_shouldThrowRuntimeException() {
        User customer = buildUser(4L, User.UserType.CUSTOMER);
        Stores store = buildStore(13L);
        Wishlist existing = buildWishlist(103L, customer, null, store);

        when(userRepo.findById(4L)).thenReturn(Optional.of(customer));
        when(storeRepo.findById(13L)).thenReturn(Optional.of(store));
        when(wishlistRepo.findByUser_UserIdAndStore_StoreId(4L, 13L)).thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.addStoreToWishlist(4L, 13L));

        assertEquals("Store already in wishlist", exception.getMessage());
    }

    @Test
    void getUserWishlist_whenCustomer_shouldReturnWishlistItems() {
        User customer = buildUser(5L, User.UserType.CUSTOMER);
        Stores store = buildStore(14L);
        Products product = buildProduct(7L, store);
        Wishlist item = buildWishlist(104L, customer, product, store);

        when(userRepo.findById(5L)).thenReturn(Optional.of(customer));
        when(wishlistRepo.findByUser(customer)).thenReturn(List.of(item));

        List<WishlistDto> result = wishlistService.getUserWishlist(5L);

        assertEquals(1, result.size());
        assertEquals(104L, result.get(0).getWishlistId());
    }

    @Test
    void removeFromWishlist_whenOwnerMatches_shouldDeleteWishlistItem() {
        User customer = buildUser(6L, User.UserType.CUSTOMER);
        Stores store = buildStore(15L);
        Products product = buildProduct(8L, store);
        Wishlist item = buildWishlist(105L, customer, product, store);

        when(userRepo.findById(6L)).thenReturn(Optional.of(customer));
        when(wishlistRepo.findById(105L)).thenReturn(Optional.of(item));
        doNothing().when(wishlistRepo).delete(item);

        wishlistService.removeFromWishlist(6L, 105L);

        verify(wishlistRepo).delete(item);
    }

    @Test
    void clearWishlist_whenCustomer_shouldDeleteByUser() {
        User customer = buildUser(7L, User.UserType.CUSTOMER);

        when(userRepo.findById(7L)).thenReturn(Optional.of(customer));
        doNothing().when(wishlistRepo).deleteByUser(customer);

        wishlistService.clearWishlist(7L);

        verify(wishlistRepo).deleteByUser(customer);
    }

    @Test
    void getWishlistItemCount_whenCustomer_shouldReturnCount() {
        User customer = buildUser(8L, User.UserType.CUSTOMER);

        when(userRepo.findById(8L)).thenReturn(Optional.of(customer));
        when(wishlistRepo.countByUser_UserId(8L)).thenReturn(4L);

        Long count = wishlistService.getWishlistItemCount(8L);

        assertEquals(4L, count);
    }

    @Test
    void isProductInWishlist_shouldReturnTrueWhenExists() {
        when(wishlistRepo.findByUser_UserIdAndStore_StoreId(9L, 99L))
                .thenReturn(Optional.of(new Wishlist()));

        assertTrue(wishlistService.isProductInWishlist(9L, 99L));
    }

    @Test
    void isStoreInWishlist_whenStoreExists_shouldReturnTrue() {
        User customer = buildUser(10L, User.UserType.CUSTOMER);
        Stores store = buildStore(16L);

        when(userRepo.findById(10L)).thenReturn(Optional.of(customer));
        when(storeRepo.findById(16L)).thenReturn(Optional.of(store));
        when(wishlistRepo.findByUser_UserIdAndStore_StoreId(10L, 16L))
                .thenReturn(Optional.of(new Wishlist()));

        assertTrue(wishlistService.isStoreInWishlist(10L, 16L));
    }

    @Test
    void isStoreInWishlist_whenStoreNotFound_shouldThrowRuntimeException() {
        User customer = buildUser(11L, User.UserType.CUSTOMER);

        when(userRepo.findById(11L)).thenReturn(Optional.of(customer));
        when(storeRepo.findById(17L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> wishlistService.isStoreInWishlist(11L, 17L));

        assertTrue(exception.getMessage().contains("store if with id: not found"));
    }
}
