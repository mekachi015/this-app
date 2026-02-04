package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.DeliveryAddressDTO.DeliveryAddressDTO;
import com.example.This_App_Backend.dto.DriversDTO.DriverDTO;
import com.example.This_App_Backend.dto.OrderDTO.OrderDTO;
import com.example.This_App_Backend.dto.OrderDTO.OrderItemsDTO;
import com.example.This_App_Backend.entity.*;
import com.example.This_App_Backend.repository.OrderRepository;
import com.example.This_App_Backend.repository.UserRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.StoreOwnerRepository;

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

    //Get all orders for the user
    public List<OrderDTO> getUserOrders (Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if(user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can view their orders");
        }

        List<CustomerOrders> orders = orderRepo.findByUserOrderByOrderDateDesc(user);
        return  orders.stream()
                .map(this:: convertToDTO)
                .collect(Collectors.toList());
    }

    //Get specific order by id
    public OrderDTO getOrderById(Long userId, Long orderId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (user.getUserType() != User.UserType.CUSTOMER){
            throw new RuntimeException("Only customers can view orders");
        }

        CustomerOrders order = orderRepo.findByOrderIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this user"));

        return convertToDTO(order);
    }

    //get order count for users
    public Long getOrderCount(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getUserType() != User.UserType.CUSTOMER){
            return 0L;
        }

        return orderRepo.countByUser(user);
    }

    //Convert entity to DTO
    private OrderDTO convertToDTO(CustomerOrders orders){
        OrderDTO dto = new OrderDTO();

        dto.setOrderId(orders.getOrderId());
        dto.setOrderStatus(orders.getOrderStatus());
        dto.setTotalAmount(orders.getTotalAmount());
        dto.getShippingAmount(orders.getShippingAmount());

        //Calculate subtotal
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

        //Store details
        if(orders.getStore() != null){
            dto.setStoreId(orders.getStore().getStoreId());
            dto.setStoreName(orders.getStore().getStoreName());
        }

        //Delivery address
        if(orders.getDeliveryAddress() != null){
            dto.setDeliveryAddress(convertAddressToDTO(orders.getDeliveryAddress()));
        }

        //driver details
        if(orders.getDriver() != null){
            dto.setDriver(convertToDriverDTO(orders.getDriver()));
        }

        //Order items
        List<OrderItemsDTO> itemsDTO = orders.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());

        dto.setItems(itemsDTO);
        dto.setItemCount(itemsDTO.size());

        return dto;
    }

    private OrderItemsDTO convertOrderItemToDTO(Order_Items item){
        OrderItemsDTO dto = new OrderItemsDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProduct().getProductId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setProductImage(item.getProduct().getImageUrl());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());

        //calculate the total item
        BigDecimal itemTotal = item.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        dto.setItemTotal(itemTotal);

        return dto;
    }

    private DeliveryAddressDTO convertAddressToDTO(User_Addresses addresses){
        DeliveryAddressDTO dto = new DeliveryAddressDTO();

        dto.setAddressId(addresses.getAddressId());
        dto.setAddressLine1(addresses.getAddressLine1());
        dto.setAddressLine2(addresses.getAddressLine2());
        dto.setCity(addresses.getCity());
        dto.setProvince(addresses.getState());
        dto.setPostalCode(addresses.getPostalCode());

        return dto;
    }

    private DriverDTO convertToDriverDTO(Drivers drivers){
        DriverDTO dto = new DriverDTO();

        dto.setDriverId(drivers.getDriverId());
        dto.setDriverName(drivers.getUser().getFirstName() + " " + drivers.getUser().getLastName());
        dto.setPhoneNumber(drivers.getUser().getPhoneNumber());
        dto.setVehicleInfo(drivers.getVehicleLicensePlate());

        return dto;
    }

     /**
     * Get all orders for a specific store
     */
    public List<OrderDTO> getStoreOrders(Long storeId){
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        List<CustomerOrders> orders = orderRepo.findByStoreOrderByOrderDateDesc(store);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get orders for a store filtered by status
     */
    public List<OrderDTO> getStoreOrdersByStatus(Long storeId, String orderStatus){
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        List<CustomerOrders> orders = orderRepo.findByStoreAndOrderStatusOrderByOrderDateDesc(store, orderStatus);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get specific order by ID for a store
     */
    public OrderDTO getStoreOrderById(Long storeId, Long orderId){
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        CustomerOrders order = orderRepo.findByOrderIdAndStore(orderId, store)
                .orElseThrow(() -> new RuntimeException("Order not found or does not belong to this store"));

        return convertToDTO(order);
    }
    
    /**
     * Get total order count for a store
     */
    public Long getStoreOrderCount(Long storeId){
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return orderRepo.countByStore(store);
    }

    /**
 * Get all orders for all stores owned by a specific owner
 */
public List<OrderDTO> getOrdersByOwnerId(Long ownerId) {
    Store_Owners storeOwner = storeOwnerRepo.findByOwnerId(ownerId)
            .orElseThrow(() -> new RuntimeException("Store owner not found"));

    List<CustomerOrders> orders = orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(storeOwner);
    return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
}

/**
 * Get all orders for all stores owned by the logged-in user
 */
public List<OrderDTO> getOrdersByUserId(Long userId) {
    User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Store_Owners storeOwner = storeOwnerRepo.findByUser(user)
            .orElseThrow(() -> new RuntimeException("User is not a store owner"));

    List<CustomerOrders> orders = orderRepo.findByStore_StoreOwnerOrderByOrderDateDesc(storeOwner);
    return orders.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
}

/**
 * Get order count for a store owner
 */
public Long getOrderCountByOwnerId(Long ownerId) {
    Store_Owners storeOwner = storeOwnerRepo.findByOwnerId (ownerId)
            .orElseThrow(() -> new RuntimeException("Store owner not found"));

    return orderRepo.countByStore_StoreOwner(storeOwner);
}

}
