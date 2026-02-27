package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.DeliveryAddressDTO.DeliveryAddressDTO;
import com.example.This_App_Backend.dto.DriversDTO.DriverDTO;
import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.dto.OrderDTO.OrderItemsDTO;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.StoreOwnerRepository;
import com.example.This_App_Backend.Enuma.OrderStatus;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private StoreOwnerRepository storeOwnerRepo;

    // -------------------------------------------------------------------------
    // Customer methods
    // -------------------------------------------------------------------------

    public List<OrderDTO> getUserOrders(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can view their orders");
        }

        return orderRepo.findByUserOrderByOrderDateDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long userId, Long orderId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            throw new RuntimeException("Only customers can view orders");
        }

        CustomerOrders order = orderRepo.findByOrderIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this user"));

        return convertToDTO(order);
    }

    public Long getOrderCount(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserType() != User.UserType.CUSTOMER) {
            return 0L;
        }

        return orderRepo.countByUser(user);
    }

    // -------------------------------------------------------------------------
    // Store methods
    // -------------------------------------------------------------------------

    public List<OrderDTO> getStoreOrders(Long storeId) {
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return orderRepo.findByStoreOrderByOrderDateDesc(store)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getStoreOrdersByStatus(Long storeId, String orderStatus) {
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        OrderStatus status;
        try {
            status = OrderStatus.valueOf(orderStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("INVALID_STATUS");
        }

        return orderRepo.findByStoreAndOrderStatusOrderByOrderDateDesc(store, String.valueOf(status))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getStoreOrderById(Long storeId, Long orderId) {
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        CustomerOrders order = orderRepo.findByOrderIdAndStore(orderId, store)
                .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this store"));

        return convertToDTO(order);
    }

    public Long getStoreOrderCount(Long storeId) {
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return orderRepo.countByStore(store);
    }

    // -------------------------------------------------------------------------
    // Store owner methods
    // -------------------------------------------------------------------------

    public List<OrderDTO> getOrdersByOwnerId(Long ownerId) {
        Store_Owners storeOwner = storeOwnerRepo.findByOwnerId(ownerId)
                .orElseThrow(() -> new RuntimeException("Store owner not found"));

        return orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(storeOwner)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Store_Owners storeOwner = storeOwnerRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User is not a store owner"));

        return orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(storeOwner)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

//    public Long getOrderCountByOwnerId(Long userId) {
//        User user = userRepo.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("User not found not found"));
//
//        if (user.getUserType() != User.UserType.ADMIN) {
//            throw new RuntimeException("Only admins can view their orders");
//        }
//
//        return orderRepo.countByStore_StoreOwner(user);
//    }

    public Long getOrderCountByUserId(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found not found"));

        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("Only admins can view their orders");
        }

        return orderRepo.countByStore_StoreOwner_User_UserId(userId);
    }


    public List<OrderDTO> getAllOrdersByUserId(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getUserType() != User.UserType.ADMIN) {
            throw new RuntimeException("Only admins can view their orders");
        }

        Store_Owners storeOwners =  storeOwnerRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User is not a store owner"));

        return orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(storeOwners)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Conversion — package-private so DriverOrderService can reuse it
    // -------------------------------------------------------------------------

    OrderDTO convertToDTO(CustomerOrders orders) {
        OrderDTO dto = new OrderDTO();

        dto.setOrderId(orders.getOrderId());
        dto.setOrderStatus(orders.orderStatus);
        dto.setTotalAmount(orders.getTotalAmount());
        dto.setShippingAmount(orders.getShippingAmount()); // was incorrectly dto.getShippingAmount()

        // Calculate subtotal
        BigDecimal subtotal = orders.getTotalAmount().subtract(
                orders.getShippingAmount() != null ? orders.getShippingAmount() : BigDecimal.ZERO
        );
        dto.setSubTotal(subtotal);

        dto.setOrderDate(orders.getOrderDate());
        dto.setEstimatedDeliveryDate(orders.getEstimatedDeliveryDate());
        dto.setActualDeliveryDate(orders.getActualDeliveryDate());
        dto.setIsAssignedDriver(orders.getIsAssignedDriver());
        dto.setCreatedAt(orders.getCreatedAt());
        dto.setUpdatedAt(orders.getUpdatedAt());

        // Store details
        if (orders.getStore() != null) {
            dto.setStoreId(orders.getStore().getStoreId());
            dto.setStoreName(orders.getStore().getStoreName());
            dto.setStoreAddress(orders.getStore().getStoreAddress());
        }

        // Delivery address
        if (orders.getDeliveryAddress() != null) {
            dto.setDeliveryAddress(convertAddressToDTO(orders.getDeliveryAddress()));
        }

        // Driver details — now mapped directly from User with DRIVER type
        if (orders.getAssignedDriver() != null) {
            dto.setDriver(convertToDriverDTO(orders.getAssignedDriver()));
        }

        // Order items
        List<OrderItemsDTO> itemsDTO = orders.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());

        dto.setItems(itemsDTO);
        dto.setItemCount(itemsDTO.size());

        return dto;
    }

    private OrderItemsDTO convertOrderItemToDTO(Order_Items item) {
        OrderItemsDTO dto = new OrderItemsDTO();

        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());

        BigDecimal itemTotal = item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        dto.setItemTotal(itemTotal);

        return dto;
    }

    private DeliveryAddressDTO convertAddressToDTO(User_Addresses addresses) {
        DeliveryAddressDTO dto = new DeliveryAddressDTO();

        dto.setAddressId(addresses.getAddressId());
        dto.setStreetNumber(addresses.getStreetNumber());
        dto.setStreetName(addresses.getStreetName());
        dto.setSuburb(addresses.getSuburb());
        dto.setCity(addresses.getCity());
        dto.setProvince(addresses.getProvince());
        dto.setPostalCode(addresses.getPostalCode());

        return dto;
    }

    // Takes a User (with DRIVER userType) instead of the old Drivers entity
    private DriverDTO convertToDriverDTO(User driver) {
        DriverDTO dto = new DriverDTO();

        dto.setDriverId(driver.getUserId());
        dto.setDriverName(driver.getFirstName() + " " + driver.getLastName());
        dto.setPhoneNumber(driver.getPhoneNumber());
        // vehicleInfo removed — no longer available without the Drivers entity
        // add it back later if you store vehicle info directly on User

        return dto;
    }
}