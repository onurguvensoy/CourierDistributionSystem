package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryReportRepository extends JpaRepository<DeliveryReport, Long> {
    List<DeliveryReport> findByCourier(Courier courier);
    List<DeliveryReport> findByDeliveryPackage(DeliveryPackage deliveryPackage);
    List<DeliveryReport> findByCourierAndDeliveryPackage(Courier courier, DeliveryPackage deliveryPackage);
    List<DeliveryReport> findByDeliveryTimeBetween(LocalDateTime start, LocalDateTime end);
    List<DeliveryReport> findByCourierAndDeliveryTimeBetween(Courier courier, LocalDateTime start, LocalDateTime end);
    Optional<DeliveryReport> findFirstByDeliveryPackageOrderByDeliveryTimeDesc(DeliveryPackage deliveryPackage);
    List<DeliveryReport> findByCustomerConfirmationTrue();
    List<DeliveryReport> findByDeliveryRatingGreaterThanEqual(Integer rating);
    List<DeliveryReport> findByCourier_Username(String username);
} 