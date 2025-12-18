package com.example.This_App_Backend.repository;

import com.example.This_App_Backend.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, Long> {



}
