package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.This_App_Backend.Enuma.EscrowStatus;
import com.example.This_App_Backend.Enuma.WalletTransactionType;
import com.example.This_App_Backend.dto.WalletDTO.WalletBalanceDTO;
import com.example.This_App_Backend.dto.WalletDTO.WalletTransactionDTO;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Payments;
import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.Wallet;
import com.example.This_App_Backend.entity.WalletTransactions;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.PaymentsRepository;
import com.example.This_App_Backend.repository.WalletRepository;
import com.example.This_App_Backend.repository.WalletTransactionRepository;
import com.example.This_App_Backend.service.WalletService;

public class WalletServiceTest {
 @Mock private WalletRepository walletRepo;
    @Mock private WalletTransactionRepository walletTransactionRepo;
    @Mock private PaymentsRepository paymentRepo;
    @Mock private OrderRepository orderRepo;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        walletService = new WalletService();
        ReflectionTestUtils.setField(walletService, "walletRepo", walletRepo);
        ReflectionTestUtils.setField(walletService, "walletTransactionRepo", walletTransactionRepo);
        ReflectionTestUtils.setField(walletService, "paymentRepo", paymentRepo);
        ReflectionTestUtils.setField(walletService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(walletService, "commissionPercent", 10);
        ReflectionTestUtils.setField(walletService, "deliveryCutPercent", 20);
    }

    // ── releaseEscrowForOrder ─────────────────────────────────────────────────

    @Test
    void releaseEscrow_throwsWhenOrderNotFound() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> walletService.releaseEscrowForOrder(99L));

        assertTrue(ex.getMessage().contains("Order not found with Id: 99"));
    }

    @Test
    void releaseEscrow_throwsWhenNoHeldPaymentExists() {
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, true);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> walletService.releaseEscrowForOrder(1L));

        assertTrue(ex.getMessage().contains("No held payment found"),
                "Expected double-release guard message but got: " + ex.getMessage());
    }

    @Test
    void releaseEscrow_calculatesAdminEarningsCorrectly() {
        // orderTotal = 100.00, commission = 10%
        // platformCut = 10.00, adminEarning = 90.00
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, true);
        Payments payment = buildHeldPayment(order);

        mockWalletSave(order.getStore().getStoreOwner().getUser());
        mockWalletSave(order.getAssignedDriver());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));

        walletService.releaseEscrowForOrder(1L);

        // Capture all wallet transactions saved
        ArgumentCaptor<WalletTransactions> txCaptor =
                ArgumentCaptor.forClass(WalletTransactions.class);
        verify(walletTransactionRepo, atLeast(1)).save(txCaptor.capture());

        WalletTransactions adminTx = txCaptor.getAllValues().stream()
                .filter(tx -> tx.getDescription().contains("store earnings"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Admin transaction not found"));

        assertEquals(new BigDecimal("90.00"), adminTx.getAmount(),
                "Admin should receive 90.00 (100.00 - 10% commission)");
        assertEquals(WalletTransactionType.CREDIT, adminTx.getType());
    }

    @Test
    void releaseEscrow_calculatesDriverEarningsCorrectly() {
        // shippingFee = 20.00, deliveryCut = 20%
        // platformDeliveryCut = 4.00, driverEarning = 16.00
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, true);
        Payments payment = buildHeldPayment(order);

        mockWalletSave(order.getStore().getStoreOwner().getUser());
        mockWalletSave(order.getAssignedDriver());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));

        walletService.releaseEscrowForOrder(1L);

        ArgumentCaptor<WalletTransactions> txCaptor =
                ArgumentCaptor.forClass(WalletTransactions.class);
        verify(walletTransactionRepo, atLeast(1)).save(txCaptor.capture());

        WalletTransactions driverTx = txCaptor.getAllValues().stream()
                .filter(tx -> tx.getDescription().contains("delivery earnings"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Driver transaction not found"));

        assertEquals(new BigDecimal("16.00"), driverTx.getAmount(),
                "Driver should receive 16.00 (20.00 - 20% delivery cut)");
        assertEquals(WalletTransactionType.CREDIT, driverTx.getType());
    }

    @Test
    void releaseEscrow_treatsNullShippingAmountAsZero() {
        // order has no shipping fee set — driver should get 0.00
        CustomerOrders order = buildOrder(1L, 100.00, 0.00, true);
        order.setShippingAmount(null); // explicitly null

        Payments payment = buildHeldPayment(order);

        mockWalletSave(order.getStore().getStoreOwner().getUser());
        mockWalletSave(order.getAssignedDriver());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));

        // Should not throw
        assertDoesNotThrow(() -> walletService.releaseEscrowForOrder(1L));

        ArgumentCaptor<WalletTransactions> txCaptor =
                ArgumentCaptor.forClass(WalletTransactions.class);
        verify(walletTransactionRepo, atLeast(1)).save(txCaptor.capture());

        WalletTransactions driverTx = txCaptor.getAllValues().stream()
                .filter(tx -> tx.getDescription().contains("delivery earnings"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Driver transaction not found"));

        assertEquals(BigDecimal.ZERO.setScale(2), driverTx.getAmount(),
                "Driver should get 0.00 when there is no shipping fee");
    }

    @Test
    void releaseEscrow_skipsDriverCreditWhenNoDriverAssigned() {
        // isAssignedDriver = null means no driver
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, false);
        order.setIsAssignedDriver(null);

        Payments payment = buildHeldPayment(order);

        mockWalletSave(order.getStore().getStoreOwner().getUser());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));

        walletService.releaseEscrowForOrder(1L);

        // Only 1 transaction saved — admin only, no driver
        verify(walletTransactionRepo, times(1)).save(any(WalletTransactions.class));
    }

    @Test
    void releaseEscrow_marksPaymentAsReleased() {
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, true);
        Payments payment = buildHeldPayment(order);

        mockWalletSave(order.getStore().getStoreOwner().getUser());
        mockWalletSave(order.getAssignedDriver());

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));

        walletService.releaseEscrowForOrder(1L);

        assertEquals(EscrowStatus.RELEASED, payment.getEscrowStatus(),
                "Payment escrow status should be RELEASED after payout");
        verify(paymentRepo).save(payment);
    }

    @Test
    void releaseEscrow_createsNewWalletWhenUserHasNone() {
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, false);
        order.setIsAssignedDriver(null);
        Payments payment = buildHeldPayment(order);

        User storeOwner = order.getStore().getStoreOwner().getUser();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));

        // No existing wallet — should be created
        when(walletRepo.findByUser_UserId(storeOwner.getUserId()))
                .thenReturn(Optional.empty());
        when(walletRepo.save(any(Wallet.class))).thenAnswer(i -> {
            Wallet w = i.getArgument(0);
            w.setWalletId(1L);
            return w;
        });

        walletService.releaseEscrowForOrder(1L);

        // Wallet was created (save called at least once for creation)
        verify(walletRepo, atLeast(1)).save(any(Wallet.class));
    }

    @Test
    void releaseEscrow_addsToExistingWalletBalance() {
        CustomerOrders order = buildOrder(1L, 100.00, 20.00, false);
        order.setIsAssignedDriver(null);
        Payments payment = buildHeldPayment(order);

        User storeOwner = order.getStore().getStoreOwner().getUser();

        // Existing wallet with 50.00 balance
        Wallet existingWallet = new Wallet();
        existingWallet.setWalletId(1L);
        existingWallet.setUser(storeOwner);
        existingWallet.setBalance(new BigDecimal("50.00"));

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepo.findByOrder_OrderIdAndEscrowStatus(1L, EscrowStatus.HELD))
                .thenReturn(Optional.of(payment));
        when(walletRepo.findByUser_UserId(storeOwner.getUserId()))
                .thenReturn(Optional.of(existingWallet));
        when(walletRepo.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        walletService.releaseEscrowForOrder(1L);

        // 50.00 existing + 90.00 admin earnings = 140.00
        assertEquals(new BigDecimal("140.00"), existingWallet.getBalance(),
                "Balance should be previous 50.00 + admin earnings 90.00");
    }

    // ── getBalance ────────────────────────────────────────────────────────────

    @Test
    void getBalance_throwsWhenWalletNotFound() {
        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> walletService.getBalance(1L));
    }

    @Test
    void getBalance_returnsCorrectDTOWhenWalletExists() {
        User user = buildUser(1L, "Alice", "Smith", User.UserType.ADMIN);
        Wallet wallet = buildWallet(1L, user, new BigDecimal("250.00"));

        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.of(wallet));

        WalletBalanceDTO dto = walletService.getBalance(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getWalletId());
        assertEquals(new BigDecimal("250.00"), dto.getBalance());
        assertEquals("Alice Smith", dto.getOwnerName());
        assertEquals("ADMIN", dto.getOwnerType());
    }

    @Test
    void getBalance_concatenatesFirstAndLastName() {
        User user = buildUser(1L, "Bob", "Driver", User.UserType.DRIVER);
        Wallet wallet = buildWallet(2L, user, BigDecimal.ZERO);

        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.of(wallet));

        WalletBalanceDTO dto = walletService.getBalance(1L);

        assertEquals("Bob Driver", dto.getOwnerName());
        assertEquals("DRIVER", dto.getOwnerType());
    }

    // ── getTransactions ───────────────────────────────────────────────────────

    @Test
    void getTransactions_throwsWhenWalletNotFound() {
        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> walletService.getTransactions(1L));
    }

    @Test
    void getTransactions_returnsEmptyListWhenNoTransactions() {
        User user = buildUser(1L, "Alice", "Smith", User.UserType.ADMIN);
        Wallet wallet = buildWallet(1L, user, BigDecimal.ZERO);

        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepo.findByWallet_WalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<WalletTransactionDTO> result = walletService.getTransactions(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTransactions_returnsMappedTransactionDTOs() {
        User user = buildUser(1L, "Alice", "Smith", User.UserType.ADMIN);
        Wallet wallet = buildWallet(1L, user, new BigDecimal("90.00"));

        CustomerOrders order = new CustomerOrders();
        order.setOrderId(42L);

        WalletTransactions tx = new WalletTransactions();
        tx.setTransactionId(10L);
        tx.setWallet(wallet);
        tx.setOrder(order);
        tx.setAmount(new BigDecimal("90.00"));
        tx.setType(WalletTransactionType.CREDIT);
        tx.setDescription("Order #42 delivered - store earnings");
        tx.setCreatedAt(LocalDateTime.now());

        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepo.findByWallet_WalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(tx));

        List<WalletTransactionDTO> result = walletService.getTransactions(1L);

        assertEquals(1, result.size());
        WalletTransactionDTO dto = result.get(0);
        assertEquals(10L, dto.getTransactionId());
        assertEquals(42L, dto.getOrderId());
        assertEquals(new BigDecimal("90.00"), dto.getAmount());
        assertEquals("CREDIT", dto.getType());
        assertEquals("Order #42 delivered - store earnings", dto.getDescription());
    }

    @Test
    void getTransactions_handlesTransactionWithNullOrder() {
        User user = buildUser(1L, "Alice", "Smith", User.UserType.ADMIN);
        Wallet wallet = buildWallet(1L, user, BigDecimal.ZERO);

        // Transaction with no linked order (edge case)
        WalletTransactions tx = new WalletTransactions();
        tx.setTransactionId(5L);
        tx.setWallet(wallet);
        tx.setOrder(null);
        tx.setAmount(new BigDecimal("50.00"));
        tx.setType(WalletTransactionType.CREDIT);
        tx.setDescription("Manual credit");
        tx.setCreatedAt(LocalDateTime.now());

        when(walletRepo.findByUser_UserId(1L)).thenReturn(Optional.of(wallet));
        when(walletTransactionRepo.findByWallet_WalletIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(tx));

        List<WalletTransactionDTO> result = walletService.getTransactions(1L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getOrderId(),
                "OrderId should be null when transaction has no linked order");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CustomerOrders buildOrder(Long id, double total,
                                      double shipping, boolean withDriver) {
        User storeOwnerUser = buildUser(10L, "Store", "Owner", User.UserType.ADMIN);

        Store_Owners storeOwner = new Store_Owners();
        storeOwner.setUser(storeOwnerUser);

        Stores store = new Stores();
        store.setStoreId(1L);
        store.setStoreName("Test Store");
        store.setStoreOwner(storeOwner);

        CustomerOrders order = new CustomerOrders();
        order.setOrderId(id);
        order.setStore(store);
        order.setTotalAmount(BigDecimal.valueOf(total));
        order.setShippingAmount(BigDecimal.valueOf(shipping));

        if (withDriver) {
            User driver = buildUser(20L, "Fast", "Driver", User.UserType.DRIVER);
            order.setIsAssignedDriver(true);
            order.setAssignedDriver(driver);
        } else {
            order.setIsAssignedDriver(false);
        }

        return order;
    }

    private Payments buildHeldPayment(CustomerOrders order) {
        Payments payment = new Payments();
        payment.setOrder(order);
        payment.setEscrowStatus(EscrowStatus.HELD);
        payment.setAmount(order.getTotalAmount());
        return payment;
    }

    private User buildUser(Long id, String firstName, String lastName, User.UserType type) {
        User user = new User();
        user.setUserId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUserType(type);
        return user;
    }

    private Wallet buildWallet(Long id, User user, BigDecimal balance) {
        Wallet wallet = new Wallet();
        wallet.setWalletId(id);
        wallet.setUser(user);
        wallet.setBalance(balance);
        wallet.setUpdateAt(LocalDateTime.now());
        return wallet;
    }

    // Mocks wallet repo to return empty then save a new wallet for a given user
    private void mockWalletSave(User user) {
        when(walletRepo.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());
        when(walletRepo.save(any(Wallet.class))).thenAnswer(i -> {
            Wallet w = i.getArgument(0);
            if (w.getWalletId() == null) w.setWalletId(user.getUserId());
            return w;
        });
    }
}
