package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.This_App_Backend.Enuma.EscrowStatus;
import com.example.This_App_Backend.Enuma.PendingCheckoutStatus;
import com.example.This_App_Backend.dto.CheckoutDTO.CheckoutInitiateResponse;
import com.example.This_App_Backend.entity.Cart;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.Payments;
import com.example.This_App_Backend.entity.PendingCheckout;
import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.CartRepo;
import com.example.This_App_Backend.repository.OrderItemsRepository;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.PaymentsRepository;
import com.example.This_App_Backend.repository.PendingCheckoutRepository;
import com.example.This_App_Backend.repository.ProductsRepository;
import com.example.This_App_Backend.repository.UserAddressesRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.PayFastCheckoutService;
import com.example.This_App_Backend.service.PayfastService;

public class PayfastCheckoutServiceTest {

    @Mock private CartRepo cartRepo;
    @Mock private UserRepository userRepo;
    @Mock private UserAddressesRepository userAddressRepo;
    @Mock private PendingCheckoutRepository pendingCheckoutRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private OrderItemsRepository orderItemsRepo;
    @Mock private PaymentsRepository paymentsRepo;
    @Mock private ProductsRepository productsRepo;
    @Mock private PayfastService payfastService;

    private PayFastCheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        checkoutService = new PayFastCheckoutService();
        ReflectionTestUtils.setField(checkoutService, "cartRepo", cartRepo);
        ReflectionTestUtils.setField(checkoutService, "userRepo", userRepo);
        ReflectionTestUtils.setField(checkoutService, "userAddressRepo", userAddressRepo);
        ReflectionTestUtils.setField(checkoutService, "pendingCheckoutRepo", pendingCheckoutRepo);
        ReflectionTestUtils.setField(checkoutService, "orderRepo", orderRepo);
        ReflectionTestUtils.setField(checkoutService, "orderItemsRepo", orderItemsRepo);
        ReflectionTestUtils.setField(checkoutService, "paymentsRepo", paymentsRepo);
        ReflectionTestUtils.setField(checkoutService, "productsRepo", productsRepo);
        ReflectionTestUtils.setField(checkoutService, "payfastService", payfastService);
        ReflectionTestUtils.setField(checkoutService, "commissionPercent", 10);
        ReflectionTestUtils.setField(checkoutService, "shippingAmount", new BigDecimal("20.00"));
    }

    // ── initiateCheckout ──────────────────────────────────────────────────────

    @Test
    void initiateCheckout_throwsWhenUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));
    }

    @Test
    void initiateCheckout_throwsForNonCustomerUser() {
        User admin = new User();
        admin.setUserId(1L);
        admin.setUserType(User.UserType.ADMIN);

        when(userRepo.findById(1L)).thenReturn(Optional.of(admin));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));

        assertTrue(ex.getMessage().contains("Only customers can initiate checkout"));
    }

    @Test
    void initiateCheckout_throwsWhenCartIsEmpty() {
        User customer = buildCustomer();
        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));

        assertTrue(ex.getMessage().contains("Cart is empty"));
    }

    @Test
    void initiateCheckout_throwsWhenCartHasMultipleStores() {
        User customer = buildCustomer();

        Stores store1 = buildStore(1L, "Store One");
        Stores store2 = buildStore(2L, "Store Two");

        Cart item1 = buildCartItem(store1, 10, 1L, new BigDecimal("50.00"));
        Cart item2 = buildCartItem(store2, 10, 2L, new BigDecimal("30.00"));

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item1, item2));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));

        assertTrue(ex.getMessage().contains("multiple stores"));
    }

    @Test
    void initiateCheckout_throwsWhenInsufficientStock() {
        User customer = buildCustomer();
        Stores store = buildStore(1L, "Test Store");

        // Only 1 in stock but requesting 5
        Cart item = buildCartItem(store, 1, 1L, new BigDecimal("50.00"));
        item.setQuantity(5L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void initiateCheckout_throwsWhenNoDeliveryAddressExists() {
        User customer = buildCustomer();
        Stores store = buildStore(1L, "Test Store");
        Cart item = buildCartItem(store, 10, 1L, new BigDecimal("50.00"));

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));

        // No default address, no fallback address
        when(userAddressRepo.findFirstByUserAndIsDefault(customer, true))
                .thenReturn(Optional.empty());
        when(userAddressRepo.findFirstByUser(customer))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));

        assertTrue(ex.getMessage().contains("No delivery address found"));
    }

    @Test
    void initiateCheckout_throwsWhenAddressIsIncomplete() {
        User customer = buildCustomer();
        Stores store = buildStore(1L, "Test Store");
        Cart item = buildCartItem(store, 10, 1L, new BigDecimal("50.00"));

        // Address missing postal code
        User_Addresses incompleteAddress = new User_Addresses();
        incompleteAddress.setAddressId(1L);
        incompleteAddress.setCity("Johannesburg");
        incompleteAddress.setPostalCode(null); // incomplete

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));
        when(userAddressRepo.findFirstByUserAndIsDefault(customer, true))
                .thenReturn(Optional.of(incompleteAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, null));

        assertTrue(ex.getMessage().contains("address is incomplete"));
    }

    @Test
    void initiateCheckout_throwsWhenSpecifiedAddressDoesNotBelongToUser() {
        User customer = buildCustomer();
        Stores store = buildStore(1L, "Test Store");
        Cart item = buildCartItem(store, 10, 1L, new BigDecimal("50.00"));

        // Address belongs to a different user
        User otherUser = new User();
        otherUser.setUserId(99L);

        User_Addresses address = new User_Addresses();
        address.setAddressId(5L);
        address.setUser(otherUser); // belongs to someone else

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));
        when(userAddressRepo.findById(5L)).thenReturn(Optional.of(address));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.initiateCheckout(1L, 5L));

        assertTrue(ex.getMessage().contains("does not belong to this user"));
    }

    @Test
    void initiateCheckout_returnsPaymentUrlOnSuccess() throws Exception {
        User customer = buildCustomer();
        Stores store = buildStore(1L, "Test Store");
        Cart item = buildCartItem(store, 10, 1L, new BigDecimal("50.00"));
        User_Addresses address = buildCompleteAddress(customer);

        when(userRepo.findById(1L)).thenReturn(Optional.of(customer));
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));
        when(userAddressRepo.findFirstByUserAndIsDefault(customer, true))
                .thenReturn(Optional.of(address));
        when(pendingCheckoutRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(payfastService.buildPaymentUrl(any(), any(), any(), any(), any(), any()))
                .thenReturn("https://sandbox.payfast.co.za/eng/process?test=1");

        CheckoutInitiateResponse response = checkoutService.initiateCheckout(1L, null);

        assertNotNull(response);
        assertNotNull(response.getPendingCheckoutId());
        assertEquals("https://sandbox.payfast.co.za/eng/process?test=1", response.getPaymentUrl());
        // subtotal = 50.00 * 1 = 50.00, + shipping 20.00 = 70.00
        assertEquals(new BigDecimal("70.00"), response.getTotalAmount());
        assertEquals(new BigDecimal("20.00"), response.getShippingAmount());
        assertEquals("Test Store", response.getStoreName());

        verify(pendingCheckoutRepo, times(1)).save(any(PendingCheckout.class));
    }

    // ── processPayfastItn ─────────────────────────────────────────────────────

    @Test
    void processPayfastItn_doesNothingWhenPaymentIdMissing() {
        // No m_payment_id in params — should return silently
        checkoutService.processPayfastItn(new HashMap<>());

        verifyNoInteractions(pendingCheckoutRepo);
    }

    @Test
    void processPayfastItn_throwsWhenPendingCheckoutNotFound() {
        when(pendingCheckoutRepo.findByPendingCheckoutIdAndStatus(
                "abc-123", PendingCheckoutStatus.PAYMENT_PENDING))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.processPayfastItn(Map.of("m_payment_id", "abc-123")));

        assertTrue(ex.getMessage().toLowerCase().contains("not found")
                || ex.getMessage().toLowerCase().contains("already processed"));
    }

    @Test
    void processPayfastItn_marksFailedWhenItnVerificationFails() {
        PendingCheckout pending = buildPendingCheckout("abc-123", false);

        when(pendingCheckoutRepo.findByPendingCheckoutIdAndStatus(
                "abc-123", PendingCheckoutStatus.PAYMENT_PENDING))
                .thenReturn(Optional.of(pending));
        when(payfastService.verifyITN(any(), any())).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> checkoutService.processPayfastItn(Map.of("m_payment_id", "abc-123")));

        assertEquals(PendingCheckoutStatus.FAILED, pending.getStatus());
        verify(pendingCheckoutRepo).save(pending);
    }

    @Test
    void processPayfastItn_marksExpiredWhenSessionExpired() {
        PendingCheckout pending = buildPendingCheckout("abc-123", false);
        // Force expiry to be in the past
        pending.setExpiredAt(LocalDateTime.now().minusMinutes(30));

        when(pendingCheckoutRepo.findByPendingCheckoutIdAndStatus(
                "abc-123", PendingCheckoutStatus.PAYMENT_PENDING))
                .thenReturn(Optional.of(pending));
        when(payfastService.verifyITN(any(), any())).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> checkoutService.processPayfastItn(Map.of("m_payment_id", "abc-123")));

        assertEquals(PendingCheckoutStatus.EXPIRED, pending.getStatus());
    }

    @Test
    void processPayfastItn_throwsWhenCartEmptyAtPaymentTime() {
        User customer = buildCustomer();
        PendingCheckout pending = buildPendingCheckout("abc-123", false);
        pending.setUser(customer);

        when(pendingCheckoutRepo.findByPendingCheckoutIdAndStatus(
                "abc-123", PendingCheckoutStatus.PAYMENT_PENDING))
                .thenReturn(Optional.of(pending));
        when(payfastService.verifyITN(any(), any())).thenReturn(true);
        when(cartRepo.findByUser(customer)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.processPayfastItn(Map.of("m_payment_id", "abc-123")));

        assertTrue(ex.getMessage().contains("Cart is empty at time of payment"));
    }

    @Test
    void processPayfastItn_throwsWhenStockChangedAfterPayment() {
        User customer = buildCustomer();
        PendingCheckout pending = buildPendingCheckout("abc-123", false);
        pending.setUser(customer);

        Stores store = buildStore(1L, "Test Store");
        // Stock was 5 when checkout started, now only 0 left
        Cart item = buildCartItem(store, 0, 1L, new BigDecimal("50.00"));
        item.setQuantity(2L);

        when(pendingCheckoutRepo.findByPendingCheckoutIdAndStatus(
                "abc-123", PendingCheckoutStatus.PAYMENT_PENDING))
                .thenReturn(Optional.of(pending));
        when(payfastService.verifyITN(any(), any())).thenReturn(true);
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));
        when(userAddressRepo.findById(any())).thenReturn(Optional.of(buildCompleteAddress(customer)));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.processPayfastItn(Map.of("m_payment_id", "abc-123")));

        assertTrue(ex.getMessage().contains("Stock changed"));
    }

    @Test
    void processPayfastItn_createsOrderAndClearsCartOnSuccess() {
        User customer = buildCustomer();
        Stores store = buildStore(1L, "Test Store");

        // Set up store owner for payment record
        Store_Owners storeOwner = new Store_Owners();
        store.setStoreOwner(storeOwner);

        Cart item = buildCartItem(store, 10, 1L, new BigDecimal("50.00"));
        User_Addresses address = buildCompleteAddress(customer);

        PendingCheckout pending = buildPendingCheckout("abc-123", false);
        pending.setUser(customer);
        pending.setDeliveryAddressId(1L);
        pending.setShippingAmount(new BigDecimal("20.00"));

        CustomerOrders savedOrder = new CustomerOrders();
        savedOrder.setOrderId(100L);

        when(pendingCheckoutRepo.findByPendingCheckoutIdAndStatus(
                "abc-123", PendingCheckoutStatus.PAYMENT_PENDING))
                .thenReturn(Optional.of(pending));
        when(payfastService.verifyITN(any(), any())).thenReturn(true);
        when(cartRepo.findByUser(customer)).thenReturn(List.of(item));
        when(userAddressRepo.findById(1L)).thenReturn(Optional.of(address));
        when(orderRepo.save(any())).thenReturn(savedOrder);
        when(paymentsRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pendingCheckoutRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        checkoutService.processPayfastItn(Map.of(
                "m_payment_id", "abc-123",
                "pf_payment_id", "pf-999"
        ));

        // Order created
        verify(orderRepo, times(1)).save(any(CustomerOrders.class));

        // Payment recorded with escrow HELD
        verify(paymentsRepo, times(1)).save(argThat(p ->
                ((Payments) p).getEscrowStatus() == EscrowStatus.HELD));

        // Cart cleared
        verify(cartRepo, times(1)).deleteAll(List.of(item));

        // Pending checkout marked COMPLETED
        assertEquals(PendingCheckoutStatus.COMPLETED, pending.getStatus());
        assertEquals("pf-999", pending.getPayfastPaymentId());
    }

    // ── PayfastService unit tests ─────────────────────────────────────────────

    @Test
    void payfastService_buildPaymentUrl_usesSandboxUrl() throws Exception {
        PayfastService service = buildPayfastService(true);

        String url = service.buildPaymentUrl(
                "checkout-123", new BigDecimal("120.00"),
                "Test Order", "John", "Doe", "john@example.com");

        assertTrue(url.startsWith("https://sandbox.payfast.co.za"),
                "Expected sandbox URL but got: " + url);
        assertTrue(url.contains("merchant_id=test-merchant-id"));
        assertTrue(url.contains("amount=120.00"));
        assertTrue(url.contains("signature="));
    }

    @Test
    void payfastService_buildPaymentUrl_usesProductionUrl() throws Exception {
        PayfastService service = buildPayfastService(false);

        String url = service.buildPaymentUrl(
                "checkout-456", new BigDecimal("80.00"),
                "Order", "Jane", "Smith", "jane@example.com");

        assertTrue(url.startsWith("https://www.payfast.co.za"),
                "Expected production URL but got: " + url);
    }

    @Test
    void payfastService_verifyItn_returnsFalseWhenStatusNotComplete() {
        PayfastService service = buildPayfastService(true);

        Map<String, String> params = new HashMap<>();
        params.put("payment_status", "CANCELLED");
        params.put("amount_gross", "100.00");
        params.put("signature", "somesig");

        assertFalse(service.verifyITN(params, new BigDecimal("100.00")));
    }

    @Test
    void payfastService_verifyItn_returnsFalseWhenAmountMismatch() {
        PayfastService service = buildPayfastService(true);

        Map<String, String> params = new HashMap<>();
        params.put("payment_status", "COMPLETE");
        params.put("amount_gross", "50.00"); // paid 50 but expected 100
        params.put("signature", "somesig");

        assertFalse(service.verifyITN(params, new BigDecimal("100.00")));
    }

    @Test
    void payfastService_verifyItn_returnsFalseWhenSignatureTampered() {
        PayfastService service = buildPayfastService(true);

        Map<String, String> params = new HashMap<>();
        params.put("payment_status", "COMPLETE");
        params.put("amount_gross", "100.00");
        params.put("signature", "tampered-signature-xyz");

        assertFalse(service.verifyITN(params, new BigDecimal("100.00")));
    }

    @Test
    void payfastService_verifyItn_returnsFalseOnMalformedAmount() {
        PayfastService service = buildPayfastService(true);

        Map<String, String> params = new HashMap<>();
        params.put("payment_status", "COMPLETE");
        params.put("amount_gross", "not-a-number");
        params.put("signature", "somesig");

        // Should not throw — returns false gracefully
        assertFalse(service.verifyITN(params, new BigDecimal("100.00")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User buildCustomer() {
        User user = new User();
        user.setUserId(1L);
        user.setUserType(User.UserType.CUSTOMER);
        user.setEmail("customer@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }

    private Stores buildStore(Long id, String name) {
        Stores store = new Stores();
        store.setStoreId(id);
        store.setStoreName(name);
        return store;
    }

    private Cart buildCartItem(Stores store, int stock, Long productId, BigDecimal price) {
        Products product = new Products();
        product.setProductId(productId);
        product.setProductName("Product " + productId);
        product.setProductPrice(price);
        product.setStockQuantity(stock);

        Cart cart = new Cart();
        cart.setStore(store);
        cart.setProduct(product);
        cart.setQuantity(1L);
        return cart;
    }

    private User_Addresses buildCompleteAddress(User user) {
        User_Addresses address = new User_Addresses();
        address.setAddressId(1L);
        address.setUser(user);
        address.setStreetNumber("12");
        address.setStreetName("Main Road");
        address.setSuburb("Sandton");
        address.setCity("Johannesburg");
        address.setProvince("Gauteng");
        address.setPostalCode("2196");
        return address;
    }

    private PendingCheckout buildPendingCheckout(String id, boolean expired) {
        PendingCheckout pending = new PendingCheckout();
        pending.setPendingCheckoutId(id);
        pending.setTotalAmount(new BigDecimal("70.00"));
        pending.setShippingAmount(new BigDecimal("20.00"));
        pending.setStatus(PendingCheckoutStatus.PAYMENT_PENDING);
        pending.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        pending.setExpiredAt(expired
                ? LocalDateTime.now().minusMinutes(1)  // already expired
                : LocalDateTime.now().plusMinutes(10)); // still valid
        return pending;
    }

    private PayfastService buildPayfastService(boolean sandbox) {
        PayfastService service = new PayfastService();
        ReflectionTestUtils.setField(service, "merchantId", "test-merchant-id");
        ReflectionTestUtils.setField(service, "merchantKey", "test-merchant-key");
        ReflectionTestUtils.setField(service, "passphrase", "test-passphrase");
        ReflectionTestUtils.setField(service, "sandbox", sandbox);
        ReflectionTestUtils.setField(service, "returnUrl", "http://localhost/return");
        ReflectionTestUtils.setField(service, "cancelUrl", "http://localhost/cancel");
        ReflectionTestUtils.setField(service, "notifyUrl", "http://localhost/notify");
        return service;
    }
}
