package com.example.This_App_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.This_App_Backend.entity.Store_Owners;

public interface StoreOwnerRepository  extends JpaRepository<Store_Owners, Integer>{

}
