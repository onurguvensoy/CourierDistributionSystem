package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RedisCustomerRepository extends CrudRepository<Customer, String> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhoneNumber(String phoneNumber);
} 