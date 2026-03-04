package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressesRepository extends JpaRepository<User_Addresses, Long> {

    // New methods for checkout
    List<User_Addresses> findByUser(User user);
    Optional<User_Addresses> findFirstByUserAndIsDefault(User user, Boolean isDefault);
    Optional<User_Addresses> findFirstByUser(User user);
}
