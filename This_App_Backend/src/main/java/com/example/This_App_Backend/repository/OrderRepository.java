package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.Enuma.OrderStatus;
import com.example.This_App_Backend.entity.CustomerOrders;
import com.example.This_App_Backend.entity.User;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.Store_Owners;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrders,Long> {

    //find all orders for a specific user
    List<CustomerOrders> findByUserOrderByOrderDateDesc(User user);

    //Find by order id and user id
    Optional<CustomerOrders> findByOrderIdAndUser(Long orderId, User user);

    //Find order by status for user
    List<CustomerOrders> findByUserAndOrderStatusOrderByOrderDateDesc(User user, String orderStatus);

    //Count orders for a user
    Long countByUser(User user);

    //New methods for orders related to stores
    List<CustomerOrders> findByStoreOrderByOrderDateDesc(Stores store);
    List<CustomerOrders> findByStoreAndOrderStatusOrderByOrderDateDesc(Stores store, String orderStatus);
    Long countByStore(Stores store);
    Optional<CustomerOrders> findByOrderIdAndStore(Long orderId, Stores store);

    // Find all orders for stores owned by a specific store owner
    List<CustomerOrders> findByStore_StoreOwnerOrderByOrderDateDesc(Store_Owners storeOwner);

    // Count orders for a specific store owner
    Long countByStore_StoreOwner(Store_Owners storeOwners);

    Long countByStore_StoreOwner_User_UserId(Long userId);
    //driver operations
    List<CustomerOrders> findByOrderStatusAndIsAssignedDriverFalseOrderByOrderDateDesc(OrderStatus orderStatus);
    List<CustomerOrders> findByAssignedDriverOrderByOrderDateDesc(User driver);
}


