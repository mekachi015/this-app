package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.This_App_Backend.dto.StoresDTO.ProductsDTO;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.ProductsRedefined;
import com.example.This_App_Backend.service.StoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProductsRedefinedTest {

    @Mock
    private ProductsRepository productsRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private StoreService storeService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductsRedefined productsRedefined;

    private User createUser(Long id, User.UserType type) {
        User user = new User();
        user.setUserId(id);
        user.setUsername("user" + id);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail("user" + id + "@example.com");
        user.setPhoneNumber("0123456789");
        user.setPassword("password");
        user.setUserType(type);
        return user;
    }

    private Stores createStore(Long id) {
        Stores store = new Stores();
        store.setStoreId(id);
        store.setStoreName("Store " + id);
        store.setStoreDescription("Description");
        store.setStoreAddress("Address");
        store.setStoreEmail("store" + id + "@example.com");
        store.setStorePhoneNumber("0123456789");
        store.setStoreBusinessHours("9am-5pm");
        Store_Owners owner = new Store_Owners();
        owner.setOwnerId(id + 10);
        owner.setUser(createUser(id + 100, User.UserType.ADMIN));
        store.setStoreOwner(owner);
        return store;
    }

    private ProductsDTO createProductsDTO(Long storeId) {
        ProductsDTO dto = new ProductsDTO();
        dto.setProductName("Test Product");
        dto.setProductDescription("A sample product");
        dto.setProductPrice(BigDecimal.valueOf(19.99));
        dto.setCategory("Electronics");
        dto.setStockQuantity(10);
        dto.setStoreId(storeId);
        return dto;
    }

    private Products createProduct(Long id, Stores store, User createdBy) {
        Products product = new Products();
        product.setProductId(id);
        product.setStore(store);
        product.setProductName("Existing Product");
        product.setProductDescription("Existing Description");
        product.setProductPrice(BigDecimal.valueOf(9.99));
        product.setCategory("Toys");
        product.setStockQuantity(5);
        product.setImageUrl("http://example.com/image.png");
        product.setCreatedBy(createdBy);
        return product;
    }

    @Test
    void createProduct_whenUserIsAdmin_shouldSaveProduct() {
        Stores store = createStore(1L);
        User admin = createUser(2L, User.UserType.ADMIN);
        Products savedProduct = createProduct(1L, store, admin);
        ProductsDTO dto = createProductsDTO(1L);

        when(storeService.getStoreById(1L)).thenReturn(store);
        when(userRepository.findByUserId(2L)).thenReturn(Optional.of(admin));
        when(productsRepository.save(any(Products.class))).thenReturn(savedProduct);

        Products result = productsRedefined.createProduct(1L, dto, 2L, null);

        assertEquals(1L, result.getProductId());
        assertEquals("Existing Product", result.getProductName());
        verify(productsRepository).save(any(Products.class));
    }

    @Test
    void createProduct_whenUserIsNotAdmin_shouldThrowRuntimeException() {
        Stores store = createStore(1L);
        User customer = createUser(3L, User.UserType.CUSTOMER);
        ProductsDTO dto = createProductsDTO(1L);

        when(storeService.getStoreById(1L)).thenReturn(store);
        when(userRepository.findByUserId(3L)).thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productsRedefined.createProduct(1L, dto, 3L, null));

        assertEquals("User does not have permission to create products", exception.getMessage());
    }

    @Test
    void updateProduct_whenUserIsAdminAndLogoProvided_shouldSaveUpdatedProduct() throws IOException {
        User admin = createUser(4L, User.UserType.ADMIN);
        Stores store = createStore(2L);
        Products existing = createProduct(2L, store, admin);
        ProductsDTO dto = createProductsDTO(2L);
        dto.setProductName("Updated Product");
        MockMultipartFile file = new MockMultipartFile("logoFile", "logo.png", "image/png", "data".getBytes());

        when(productsRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(4L)).thenReturn(Optional.of(admin));
        when(productsRepository.save(existing)).thenReturn(existing);
        when(fileStorageService.storeProductImage(file, 2L, 2L)).thenReturn("http://cdn.example.com/product.png");

        Products result = productsRedefined.updateProduct(2L, dto, 4L, file);

        assertEquals("Updated Product", result.getProductName());
        assertEquals("http://cdn.example.com/product.png", result.getImageUrl());
        verify(productsRepository, times(2)).save(existing);
    }

    @Test
    void updateProduct_whenUserIsNotAdmin_shouldThrowRuntimeException() {
        User customer = createUser(5L, User.UserType.CUSTOMER);
        Stores store = createStore(3L);
        Products existing = createProduct(3L, store, createUser(6L, User.UserType.ADMIN));
        ProductsDTO dto = createProductsDTO(3L);

        when(productsRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(5L)).thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productsRedefined.updateProduct(3L, dto, 5L, null));

        assertEquals("User does not have permission to update this product", exception.getMessage());
    }

    @Test
    void deleteProduct_whenUserIsAdmin_shouldDeleteProduct() {
        User admin = createUser(7L, User.UserType.ADMIN);
        Stores store = createStore(4L);
        Products existing = createProduct(4L, store, admin);

        when(productsRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(7L)).thenReturn(Optional.of(admin));
        doNothing().when(productsRepository).delete(existing);

        productsRedefined.deleteProduct(4L, 7L);

        verify(productsRepository).delete(existing);
    }

    @Test
    void deleteProduct_whenProductNotFound_shouldThrowIllegalArgumentException() {
        when(productsRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productsRedefined.deleteProduct(99L, 7L));

        assertTrue(exception.getMessage().contains("Product not found"));
    }

    @Test
    void getAllProductsByStore_whenStoreExists_returnsDtoList() {
        Stores store = createStore(5L);
        User admin = createUser(8L, User.UserType.ADMIN);
        Products productA = createProduct(5L, store, admin);
        Products productB = createProduct(6L, store, admin);

        when(storeRepository.findById(5L)).thenReturn(Optional.of(store));
        when(productsRepository.findByStore(store)).thenReturn(List.of(productA, productB));

        List<ProductsDTO> result = productsRedefined.getAllProductsByStore(5L);

        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).getProductId());
    }

    @Test
    void searchProducts_whenSearchTermMatches_returnsFilteredDtoList() {
        Stores store = createStore(6L);
        User admin = createUser(9L, User.UserType.ADMIN);
        Products productA = createProduct(7L, store, admin);
        productA.setProductName("Phone");
        Products productB = createProduct(8L, store, admin);
        productB.setProductDescription("Luxury watch");

        when(productsRepository.findAll()).thenReturn(List.of(productA, productB));

        List<ProductsDTO> result = productsRedefined.searchProducts("watch");

        assertEquals(1, result.size());
        assertEquals("Luxury watch", result.get(0).getProductDescription());
    }

    @Test
    void getProductsCountByStore_shouldReturnCount() {
        Stores store = createStore(7L);

        when(storeRepository.findById(7L)).thenReturn(Optional.of(store));
        when(productsRepository.countByStore(store)).thenReturn(3L);

        long count = productsRedefined.getProductsCountByStore(7L);

        assertEquals(3L, count);
    }

    @Test
    void getStoreCountByStoreOwner_whenUserIsAdmin_returnsStoreCount() {
        User admin = createUser(10L, User.UserType.ADMIN);

        when(userRepository.findByUserId(10L)).thenReturn(Optional.of(admin));
        when(storeRepository.countByStoreOwner_User_UserId(10L)).thenReturn(2L);

        long count = productsRedefined.getStoreCountByStoreOwner(10L);

        assertEquals(2L, count);
    }

    @Test
    void getStoreCountByStoreOwner_whenUserIsNotAdmin_shouldThrowIllegalArgumentException() {
        User customer = createUser(11L, User.UserType.CUSTOMER);

        when(userRepository.findByUserId(11L)).thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productsRedefined.getStoreCountByStoreOwner(11L));

        assertEquals("USER_NOT_ADMIN", exception.getMessage());
    }
}
