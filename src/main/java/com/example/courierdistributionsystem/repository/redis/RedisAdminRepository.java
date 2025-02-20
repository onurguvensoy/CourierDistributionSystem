package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.Admin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RedisAdminRepository extends CrudRepository<Admin, String> {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
} 