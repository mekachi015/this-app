package com.example.This_App_Backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.This_App_Backend.entity.Products;
import com.example.This_App_Backend.entity.Stores;

public interface ProductsRepository extends JpaRepository <Products, Long>{
    List<Products> findByStore(Stores store);

}
