package com.example.This_App_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.This_App_Backend.entity.Stores;

public interface StoreRepository extends JpaRepository <Stores, String>{

}
