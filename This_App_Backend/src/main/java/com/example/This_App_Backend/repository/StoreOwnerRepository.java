package com.example.This_App_Backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.User;

public interface StoreOwnerRepository  extends JpaRepository<Store_Owners, Integer>{
    Optional<Store_Owners> findByUser(User user);
}
