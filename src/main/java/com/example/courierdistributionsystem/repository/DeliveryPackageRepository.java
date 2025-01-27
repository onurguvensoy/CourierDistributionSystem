package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryPackageRepository extends JpaRepository<DeliveryPackage, Long> {
    List<DeliveryPackage> findByCustomer(User customer);
    List<DeliveryPackage> findByCourier(User courier);
    List<DeliveryPackage> findByStatus(DeliveryPackage.DeliveryStatus status);
    List<DeliveryPackage> findByCourierAndStatusIn(User courier, List<DeliveryPackage.DeliveryStatus> statuses);
} 