package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeliveryPackageRepository extends JpaRepository<DeliveryPackage, Long> {
    Optional<DeliveryPackage> findByTrackingNumber(String trackingNumber);
} 