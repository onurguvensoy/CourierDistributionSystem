package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    List<LocationHistory> findByCourier(Courier courier);
    List<LocationHistory> findByCourierAndTimestampBetween(Courier courier, LocalDateTime start, LocalDateTime end);
    List<LocationHistory> findByCourierOrderByTimestampDesc(Courier courier);
    List<LocationHistory> findByDeliveryPackage(DeliveryPackage deliveryPackage);
} 