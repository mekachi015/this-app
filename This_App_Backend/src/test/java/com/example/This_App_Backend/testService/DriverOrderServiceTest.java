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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
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
import com.example.This_App_Backend.service.DriverOrderService;
import com.example.This_App_Backend.service.OrderService;
import com.example.This_App_Backend.service.WalletService;

@ExtendWith(MockitoExtension.class)
public class DriverOrderServiceTest {
    @Mock
    private CartRepo cartRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ProductsRepository productRepo;
    @Mock
    private StoreRepository storeRepo;
    @Mock
    private OrderRepository orderRepo;
    @Mock
    private OrderItemsRepository orderItemsRepo;
    @Mock
    private UserAddressesRepository userAddressRepo;
    @Mock
    private StoreOwnerRepository storeOwnerRepo;
    @Mock
    private DriverRepository driverRepo;
    @Mock
    private WalletService walletService;

    private OrderService orderService;
    private DriverOrderService driverOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 1. Create and configure OrderService first
        orderService = new OrderService();
        ReflectionTestUtils.setField(orderService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(orderService, "userRepo", userRepo);
        ReflectionTestUtils.setField(orderService, "storeRepo", storeRepo);
        ReflectionTestUtils.setField(orderService, "storeOwnerRepo", storeOwnerRepo);

        // 2. Create DriverOrderService and inject all dependencies
        driverOrderService = new DriverOrderService();
        ReflectionTestUtils.setField(driverOrderService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(driverOrderService, "userRepo", userRepo);
        ReflectionTestUtils.setField(driverOrderService, "orderService", orderService);
        ReflectionTestUtils.setField(driverOrderService, "walletService", walletService);
    }

    @Test
    void getAvailableOrders_throwsWhenUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> driverOrderService.getAvailableOrder(1L));
    }

    @Test
    void getAvailableOrders_throwsWhenUserIsNotDriver() {
        User customer = buildUser(1L, User.UserType.CUSTOMER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.getAvailableOrder(1L));

        assertTrue(ex.getMessage().contains("not driver"));
    }

    @Test
    void getAvailableOrders_returnsReadyForDeliveryOrders() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.READY_FOR_DELIVERY);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findByOrderStatusAndIsAssignedDriverFalseOrderByOrderDateDesc(
                OrderStatus.READY_FOR_DELIVERY))
                .thenReturn(List.of(order));

        List<OrderDTO> result = driverOrderService.getAvailableOrder(1L);

        assertEquals(1, result.size());
    }

    @Test
    void claimOrder_throwsWhenOrderNotFound() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> driverOrderService.claimOrder(1L, 99L));
    }

    @Test
    void claimOrder_throwsWhenOrderNotReadyForDelivery() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.PENDING); // not ready

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.claimOrder(1L, 1L));

        assertTrue(ex.getMessage().contains("not available to be claimed"));
    }

    @Test
    void claimOrder_throwsWhenOrderAlreadyClaimed() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.READY_FOR_DELIVERY);
        order.setIsAssignedDriver(true); // already claimed

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.claimOrder(1L, 1L));

        assertTrue(ex.getMessage().contains("already been claimed"));
    }

    @Test
    void claimOrder_successfullyAssignsDriverToOrder() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.READY_FOR_DELIVERY);
        order.setIsAssignedDriver(false);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);

        OrderDTO result = driverOrderService.claimOrder(1L, 1L);

        assertNotNull(result);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getOrderStatus());
        assertEquals(driver, order.getAssignedDriver());
        assertTrue(order.getIsAssignedDriver());
    }

    @Test
    void updateOrderStatus_throwsWhenOrderHasNoDriver() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);
        order.setAssignedDriver(null); // no driver assigned

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.updateOrderStatus(1L, 1L, "DELIVERED"));

        assertTrue(ex.getMessage().contains("no assigned driver"));
    }

    @Test
    void updateOrderStatus_throwsWhenDriverDoesNotOwnOrder() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User otherDriver = buildUser(99L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);

        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);
        order.setAssignedDriver(otherDriver); // different driver owns it

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.updateOrderStatus(1L, 1L, "DELIVERED"));

        assertTrue(ex.getMessage().contains("not assigned to you"));
    }

    @Test
    void updateOrderStatus_throwsOnInvalidStatus() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);
        order.setAssignedDriver(driver);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.updateOrderStatus(1L, 1L, "INVALID"));

        assertTrue(ex.getMessage().contains("Invalid status"));
    }

    @Test
    void updateOrderStatus_throwsOnInvalidTransitionFromDelivered() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.DELIVERED); // terminal
        order.setAssignedDriver(driver);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.updateOrderStatus(1L, 1L, "FAILED"));

        assertTrue(ex.getMessage().contains("terminal state"));
    }

    @Test
    void updateOrderStatus_toDelivered_releasesEscrow() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);
        order.setAssignedDriver(driver);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);

        driverOrderService.updateOrderStatus(1L, 1L, "DELIVERED");

        // Most important: escrow is released when order is delivered
        verify(walletService, times(1)).releaseEscrowForOrder(1L);
        assertEquals(OrderStatus.DELIVERED, order.getOrderStatus());
    }

    @Test
    void updateOrderStatus_toFailed_doesNotReleaseEscrow() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);
        order.setAssignedDriver(driver);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);

        driverOrderService.updateOrderStatus(1L, 1L, "FAILED");

        // Escrow must NOT be released for failed deliveries
        verify(walletService, never()).releaseEscrowForOrder(any());
        assertEquals(OrderStatus.FAILED, order.getOrderStatus());
    }

    @Test
    void getMyOrders_returnsOnlyDriversOrders() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);
        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findByAssignedDriverOrderByOrderDateDesc(driver))
                .thenReturn(List.of(order));

        List<OrderDTO> result = driverOrderService.getMyOrder(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrderById_throwsWhenOrderNotAssignedToDriver() {
        User driver = buildUser(1L, User.UserType.DRIVER);
        User otherDriver = buildUser(99L, User.UserType.DRIVER);
        User customer = buildUser(2L, User.UserType.CUSTOMER);

        CustomerOrders order = buildOrder(1L, customer, OrderStatus.OUT_FOR_DELIVERY);
        order.setAssignedDriver(otherDriver);

        when(userRepo.findById(1L)).thenReturn(Optional.of(driver));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> driverOrderService.getOrderById(1L, 1L));

        assertTrue(ex.getMessage().contains("not assigned to you"));
    }

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

    private CustomerOrders buildOrder(Long id, User customer, OrderStatus status) {
        Stores store = buildStore(1L, "Test Store");

        CustomerOrders order = new CustomerOrders();
        order.setOrderId(id);
        order.setUser(customer);
        order.setStore(store);
        order.setOrderStatus(status);
        order.setTotalAmount(new BigDecimal("120.00"));
        order.setShippingAmount(new BigDecimal("20.00"));
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setIsAssignedDriver(false);
        order.setOrderItems(Collections.emptyList());
        return order;
    }
}
