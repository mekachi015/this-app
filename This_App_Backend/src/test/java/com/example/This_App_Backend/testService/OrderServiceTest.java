package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.StoreOwnerRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.OrderService;
import com.example.This_App_Backend.Enuma.OrderStatus;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private StoreRepository storeRepo;

    @Mock
    private StoreOwnerRepository storeOwnerRepo;

    @InjectMocks
    private OrderService orderService;

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

    private Stores buildStore(Long storeId, Store_Owners owner) {
        Stores store = new Stores();
        store.setStoreId(storeId);
        store.setStoreName("Store " + storeId);
        store.setStoreAddress("123 Main St");
        store.setStoreEmail("store" + storeId + "@example.com");
        store.setStorePhoneNumber("0123456789");
        store.setStoreBusinessHours("9am-5pm");
        store.setStoreDescription("Description");
        store.setStoreLogo("http://example.com/logo.png");
        store.setStoreOwner(owner);
        return store;
    }

    private Store_Owners buildStoreOwner(Long ownerId, User user) {
        Store_Owners owner = new Store_Owners();
        owner.setOwnerId(ownerId);
        owner.setUser(user);
        return owner;
    }

    private CustomerOrders buildOrder(Long orderId, User user, Stores store, OrderStatus status) {
        CustomerOrders order = new CustomerOrders();
        order.setOrderId(orderId);
        order.setUser(user);
        order.setStore(store);
        order.setOrderStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setShippingAmount(BigDecimal.valueOf(10.00));
        order.setOrderDate(LocalDateTime.now());
        order.setEstimatedDeliveryDate(LocalDate.now().plusDays(2));
        order.setActualDeliveryDate(null);
        order.setIsAssignedDriver(false);
        order.setCheckoutSessionId("cs_test_123");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    // Customer methods tests
    @Test
    void getUserOrders_whenCustomer_shouldReturnOrderList() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Stores store = buildStore(10L, null);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepo.findByUserOrderByOrderDateDesc(customer)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getUserOrders(1L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());
    }

    @Test
    void getUserOrders_whenNotCustomer_shouldThrowException() {
        User admin = buildUser(1L, User.UserType.ADMIN);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getUserOrders(1L));

        assertEquals("Only customers can view their orders", exception.getMessage());
    }

    @Test
    void getOrderById_whenValid_shouldReturnOrder() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        Stores store = buildStore(10L, null);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepo.findByOrderIdAndUser(100L, customer)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getOrderById(1L, 100L);

        assertEquals(100L, result.getOrderId());
    }

    @Test
    void getOrderCount_whenCustomer_shouldReturnCount() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepo.countByUser(customer)).thenReturn(5L);

        Long count = orderService.getOrderCount(1L);

        assertEquals(5L, count);
    }

    // Store methods tests
    @Test
    void getStoreOrders_shouldReturnOrderList() {
        Stores store = buildStore(10L, null);
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));
        when(orderRepo.findByStoreOrderByOrderDateDesc(store)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getStoreOrders(10L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getOrderId());
    }

    @Test
    void getStoreOrdersByStatus_whenValidStatus_shouldReturnOrders() {
        Stores store = buildStore(10L, null);
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));
        when(orderRepo.findByStoreAndOrderStatusOrderByOrderDateDesc(store, "PENDING"))
                .thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getStoreOrdersByStatus(10L, "pending");

        assertEquals(1, result.size());
    }

    @Test
    void getStoreOrdersByStatus_whenInvalidStatus_shouldThrowException() {
        Stores store = buildStore(10L, null);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.getStoreOrdersByStatus(10L, "invalid"));

        assertEquals("INVALID_STATUS", exception.getMessage());
    }

    @Test
    void getStoreOrderCount_shouldReturnCount() {
        Stores store = buildStore(10L, null);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));
        when(orderRepo.countByStore(store)).thenReturn(3L);

        Long count = orderService.getStoreOrderCount(10L);

        assertEquals(3L, count);
    }

    // Store owner methods tests
    @Test
    void getOrdersByOwnerId_shouldReturnOrderList() {
        User ownerUser = buildUser(2L, User.UserType.ADMIN);
        Store_Owners owner = buildStoreOwner(20L, ownerUser);
        Stores store = buildStore(10L, owner);
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(storeOwnerRepo.findByOwnerId(20L)).thenReturn(Optional.of(owner));
        when(orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(owner)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByOwnerId(20L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByUserId_whenStoreOwner_shouldReturnOrders() {
        User ownerUser = buildUser(2L, User.UserType.ADMIN);
        Store_Owners owner = buildStoreOwner(20L, ownerUser);
        Stores store = buildStore(10L, owner);
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(userRepo.findById(2L)).thenReturn(Optional.of(ownerUser));
        when(storeOwnerRepo.findByUser(ownerUser)).thenReturn(Optional.of(owner));
        when(orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(owner)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByUserId(2L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrderCountByUserId_whenAdmin_shouldReturnCount() {
        User admin = buildUser(2L, User.UserType.ADMIN);

        when(userRepo.findById(2L)).thenReturn(Optional.of(admin));
        when(orderRepo.countByStore_StoreOwner_User_UserId(2L)).thenReturn(7L);

        Long count = orderService.getOrderCountByUserId(2L);

        assertEquals(7L, count);
    }

    @Test
    void getOrderCountByUserId_whenNotAdmin_shouldThrowException() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrderCountByUserId(1L));

        assertEquals("Only admins can view their orders", exception.getMessage());
    }

    // Update order status tests
    @Test
    void updateOrderStatus_whenValidTransition_shouldUpdateStatus() {
        User ownerUser = buildUser(2L, User.UserType.ADMIN);
        Store_Owners owner = buildStoreOwner(20L, ownerUser);
        Stores store = buildStore(10L, owner);
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(CustomerOrders.class))).thenReturn(order);

        OrderDTO result = orderService.updateOrderStatus(10L, 100L, "ready_for_delivery", "user2");

        assertEquals(OrderStatus.READY_FOR_DELIVERY, result.getOrderStatus());
    }

    @Test
    void updateOrderStatus_whenInvalidStatus_shouldThrowException() {
        User ownerUser = buildUser(2L, User.UserType.ADMIN);
        Store_Owners owner = buildStoreOwner(20L, ownerUser);
        Stores store = buildStore(10L, owner);
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(100L, customer, store, OrderStatus.PENDING);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));
        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(10L, 100L, "invalid", "user2"));

        assertEquals("INVALID_STATUS", exception.getMessage());
    }

    @Test
    void updateOrderStatus_whenUnauthorized_shouldThrowException() {
        User ownerUser = buildUser(2L, User.UserType.ADMIN);
        Store_Owners owner = buildStoreOwner(20L, ownerUser);
        Stores store = buildStore(10L, owner);

        when(storeRepo.findById(10L)).thenReturn(Optional.of(store));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(10L, 100L, "ready_for_delivery", "wronguser"));

        assertEquals("UNAUTHORIZED", exception.getMessage());
    }
}