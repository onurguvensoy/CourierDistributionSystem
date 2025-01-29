package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Add query methods
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
} 