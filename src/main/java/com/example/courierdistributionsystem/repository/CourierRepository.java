package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    // Add query methods
    Optional<Courier> findByUsername(String username);
    Optional<Courier> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Courier> findByPhoneNumber(String phoneNumber);
    List<Courier> findByAvailableTrue();
    List<Courier> findByAvailable(boolean available);
    List<Courier> findByCurrentZone(String zone);
} 