package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    List<LocationHistory> findByCourier(User courier);
    List<LocationHistory> findByDeliveryPackage(Package deliveryPackage);
    List<LocationHistory> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<LocationHistory> findByCourierAndTimestampBetween(User courier, LocalDateTime start, LocalDateTime end);
    List<LocationHistory> findByDeliveryPackageAndTimestampBetween(Package deliveryPackage, LocalDateTime start, LocalDateTime end);
} 