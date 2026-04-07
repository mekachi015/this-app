package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutRequestDTO;
import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutResponseDTO;
import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutSummaryDTO;
import com.example.This_App_Backend.dto.CheckoutDTO.StoreOrderSummary;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.CartRepo;
import com.example.This_App_Backend.repository.OrderItemsRepository;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.UserAddressesRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.CheckoutServices;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {
     @Mock private CartRepo cartRepo;
    @Mock private UserRepository userRepo;
    @Mock private ProductsRepository productsRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private OrderItemsRepository orderItemsRepo;
    @Mock private UserAddressesRepository userAddressesRepo;

    @InjectMocks
    private CheckoutServices checkoutServices;

    private User testCustomer;
    private User testDriver;
    private Stores testStore1;
    private Stores testStore2;
    private Products testProduct1;
    private Products testProduct2;
    private Cart testCart1;
    private Cart testCart2;
    private User_Addresses defaultAddress;
    private User_Addresses selectedAddress;
    private CheckoutRequestDTO checkoutRequest;

    @BeforeEach
    void setUp() {
        testCustomer = new User();
        testCustomer.setUserId(1L);
        testCustomer.setUserType(User.UserType.CUSTOMER);
        testCustomer.setUsername("customer");

        testDriver = new User();
        testDriver.setUserId(2L);
        testDriver.setUserType(User.UserType.DRIVER);

        testStore1 = new Stores();
        testStore1.setStoreId(10L);
        testStore1.setStoreName("Store A");
        testStore1.setStoreAddress("123 Store St");

        testStore2 = new Stores();
        testStore2.setStoreId(20L);
        testStore2.setStoreName("Store B");
        testStore2.setStoreAddress("456 Market Ave");

        testProduct1 = new Products();
        testProduct1.setProductId(100L);
        testProduct1.setProductName("Product 1");
        testProduct1.setProductPrice(new BigDecimal("19.99"));
        testProduct1.setStockQuantity(10);
        testProduct1.setImageUrl("img1.jpg");

        testProduct2 = new Products();
        testProduct2.setProductId(200L);
        testProduct2.setProductName("Product 2");
        testProduct2.setProductPrice(new BigDecimal("29.99"));
        testProduct2.setStockQuantity(5);
        testProduct2.setImageUrl("img2.jpg");

        testCart1 = new Cart();
        testCart1.setCartItemId(1000L);
        testCart1.setUser(testCustomer);
        testCart1.setStore(testStore1);
        testCart1.setProduct(testProduct1);
        testCart1.setQuantity(2L);

        testCart2 = new Cart();
        testCart2.setCartItemId(2000L);
        testCart2.setUser(testCustomer);
        testCart2.setStore(testStore2);
        testCart2.setProduct(testProduct2);
        testCart2.setQuantity(1L);

        defaultAddress = new User_Addresses();
        defaultAddress.setAddressId(500L);
        defaultAddress.setUser(testCustomer);
        defaultAddress.setStreetNumber("10");
        defaultAddress.setStreetName("Main St");
        defaultAddress.setSuburb("Downtown");
        defaultAddress.setCity("Metropolis");
        defaultAddress.setProvince("State");
        defaultAddress.setPostalCode("12345");
        defaultAddress.setDefault(true);

        selectedAddress = new User_Addresses();
        selectedAddress.setAddressId(501L);
        selectedAddress.setUser(testCustomer);
        selectedAddress.setStreetNumber("20");
        selectedAddress.setStreetName("Second Ave");
        selectedAddress.setSuburb("Uptown");
        selectedAddress.setCity("Gotham");
        selectedAddress.setProvince("Gotham Province");
        selectedAddress.setPostalCode("67890");
        selectedAddress.setDefault(false);

        checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setDeliveryAddressId(501L);
    }

    // ---------- getCheckoutSummary ----------
    @Test
    void getCheckoutSummary_success() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1, testCart2));

        CheckoutSummaryDTO summary = checkoutServices.getCheckoutSummary(1L);

        assertNotNull(summary);
        assertEquals(1L, summary.getUserId());
        assertEquals(2, summary.getTotalStores());
        assertEquals(3, summary.getTotalItems()); // 2 + 1
        assertEquals(new BigDecimal("19.99").multiply(BigDecimal.valueOf(2))
                .add(new BigDecimal("29.99")), summary.getGrandTotal());

        List<StoreOrderSummary> storeOrders = summary.getStoreOrders();
        assertEquals(2, storeOrders.size());
    }

    @Test
    void getCheckoutSummary_userNotFound_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.getCheckoutSummary(1L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getCheckoutSummary_notCustomer_throwsException() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(testDriver));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.getCheckoutSummary(2L));
        assertEquals("Only customers can checkout items", ex.getMessage());
    }

    @Test
    void getCheckoutSummary_cartEmpty_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(Collections.emptyList());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.getCheckoutSummary(1L));
        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void getCheckoutSummary_insufficientStock_throwsException() {
        testProduct1.setStockQuantity(1); // only 1, but cart has 2
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.getCheckoutSummary(1L));
        assertTrue(ex.getMessage().contains("Insufficient stock for Product 1"));
    }

    // ---------- processCheckout ----------
    @Test
    void processCheckout_successWithSpecificAddress() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1, testCart2));
        when(userAddressesRepo.findById(501L)).thenReturn(Optional.of(selectedAddress));
        when(productsRepo.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerOrders savedOrder1 = createOrder(1000L, testStore1, new BigDecimal("39.98"));
        CustomerOrders savedOrder2 = createOrder(1001L, testStore2, new BigDecimal("29.99"));
        when(orderRepo.save(any(CustomerOrders.class)))
                .thenReturn(savedOrder1)
                .thenReturn(savedOrder2);

        CheckoutResponseDTO response = checkoutServices.proccessCheckout(1L, checkoutRequest);

        assertTrue(response.getSuccess());
        assertEquals("Checkout successful! 2 order(s) created from 2 store(s)", response.getMessage());
        assertEquals(2, response.getTotalOrders());
        assertEquals(new BigDecimal("69.97"), response.getGrandTotal());
        assertEquals(2, response.getOrders().size());

        // Verify stock updates
        assertEquals(8, testProduct1.getStockQuantity()); // 10 - 2
        assertEquals(4, testProduct2.getStockQuantity()); // 5 - 1
        verify(productsRepo, times(2)).save(any(Products.class));

        // Verify cart cleared
        verify(cartRepo, times(1)).deleteAll(List.of(testCart1, testCart2));
    }

    @Test
    void processCheckout_usesDefaultAddressWhenNoIdProvided() {
        checkoutRequest.setDeliveryAddressId(null);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findFirstByUserAndIsDefault(testCustomer, true))
                .thenReturn(Optional.of(defaultAddress));
        when(productsRepo.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepo.save(any(CustomerOrders.class))).thenReturn(createOrder(2000L, testStore1, new BigDecimal("39.98")));

        CheckoutResponseDTO response = checkoutServices.proccessCheckout(1L, checkoutRequest);

        assertTrue(response.getSuccess());
        verify(userAddressesRepo, never()).findById(any());
        verify(userAddressesRepo).findFirstByUserAndIsDefault(testCustomer, true);
    }

    @Test
    void processCheckout_fallsBackToAnyAddressWhenNoDefault() {
        checkoutRequest.setDeliveryAddressId(null);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findFirstByUserAndIsDefault(testCustomer, true))
                .thenReturn(Optional.empty());
        when(userAddressesRepo.findFirstByUser(testCustomer))
                .thenReturn(Optional.of(selectedAddress));
        when(productsRepo.save(any(Products.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepo.save(any(CustomerOrders.class))).thenReturn(createOrder(3000L, testStore1, new BigDecimal("39.98")));

        CheckoutResponseDTO response = checkoutServices.proccessCheckout(1L, checkoutRequest);

        assertTrue(response.getSuccess());
        verify(userAddressesRepo).findFirstByUser(testCustomer);
    }

    @Test
    void processCheckout_noAddressAvailable_throwsException() {
        checkoutRequest.setDeliveryAddressId(null);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findFirstByUserAndIsDefault(testCustomer, true))
                .thenReturn(Optional.empty());
        when(userAddressesRepo.findFirstByUser(testCustomer))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertTrue(ex.getMessage().contains("No delivery address found"));
    }

    @Test
    void processCheckout_addressNotFound_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findById(501L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertEquals("Delivery address not found", ex.getMessage());
    }

    @Test
    void processCheckout_addressNotOwnedByUser_throwsException() {
        User otherUser = new User();
        otherUser.setUserId(99L);
        selectedAddress.setUser(otherUser);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findById(501L)).thenReturn(Optional.of(selectedAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertEquals("Delivery address does not belong to this user", ex.getMessage());
    }

    @Test
    void processCheckout_incompleteAddress_throwsException() {
        selectedAddress.setStreetNumber(null);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findById(501L)).thenReturn(Optional.of(selectedAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertEquals("Delivery address is incomplete. Please update your address.", ex.getMessage());
    }

    @Test
    void processCheckout_userNotFound_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void processCheckout_notCustomer_throwsException() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(testDriver));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(2L, checkoutRequest));
        assertEquals("Only customers can checkout", ex.getMessage());
    }

    @Test
    void processCheckout_cartEmpty_throwsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(Collections.emptyList());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertEquals("Cart is empty. Nothing to checkout", ex.getMessage());
    }

    @Test
    void processCheckout_stockInsufficientDuringCheckout_throwsException() {
        testProduct1.setStockQuantity(1); // only 1 but cart has 2
        when(userRepo.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepo.findByUser(testCustomer)).thenReturn(List.of(testCart1));
        when(userAddressesRepo.findById(501L)).thenReturn(Optional.of(selectedAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutServices.proccessCheckout(1L, checkoutRequest));
        assertTrue(ex.getMessage().contains("Insufficient stock for Product 1 from store Store A"));
        verify(productsRepo, never()).save(any());
        verify(orderRepo, never()).save(any());
    }

    // Helper to create a saved order mock
    private CustomerOrders createOrder(Long orderId, Stores store, BigDecimal total) {
        CustomerOrders order = new CustomerOrders();
        order.setOrderId(orderId);
        order.setStore(store);
        order.setTotalAmount(total);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderItems(new ArrayList<>());
        return order;
    }

}
