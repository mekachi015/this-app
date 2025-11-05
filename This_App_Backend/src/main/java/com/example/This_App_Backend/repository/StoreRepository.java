package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.Store_Owners;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.This_App_Backend.entity.Stores;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository <Stores, Long>{

  //  List<Stores> findByStoreOwner(Long ownerId);

    List<Stores> findByStoreOwner(Store_Owners storeOwner);

//    // Option 2 (explicit native query if your mapping is different)
//    @Query("SELECT s FROM Stores s WHERE s.storeOwner.ownerId = :ownerId")
//    List<Stores> findStoresByOwnerId(@Param("ownerId") Long ownerId);
}
