package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Courier findByUserId(Long userId);
    List<Courier> findByAvailable(boolean available);
} 