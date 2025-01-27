package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByUserId(Long userId);
} 