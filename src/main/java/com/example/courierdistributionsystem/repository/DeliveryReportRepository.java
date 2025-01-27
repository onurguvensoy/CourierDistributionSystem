package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryReportRepository extends JpaRepository<DeliveryReport, Long> {
    List<DeliveryReport> findByCourier(User courier);
    List<DeliveryReport> findByDeliveryPackage(DeliveryPackage deliveryPackage);
    List<DeliveryReport> findByDeliveryTimeBetween(LocalDateTime start, LocalDateTime end);
    List<DeliveryReport> findByCourierAndDeliveryTimeBetween(User courier, LocalDateTime start, LocalDateTime end);
} 