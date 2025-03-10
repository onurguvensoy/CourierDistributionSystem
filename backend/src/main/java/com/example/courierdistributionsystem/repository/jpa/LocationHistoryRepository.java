package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    List<LocationHistory> findByCourier(Courier courier);
    List<LocationHistory> findByCourierAndTimestampBetween(Courier courier, LocalDateTime start, LocalDateTime end);
    List<LocationHistory> findByCourierOrderByTimestampDesc(Courier courier);
    List<LocationHistory> findByDeliveryPackage(DeliveryPackage deliveryPackage);
    
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.deliveryPackage.id = :packageId ORDER BY lh.timestamp DESC")
    List<LocationHistory> findByPackageIdOrderByTimestampDesc(@Param("packageId") Long packageId);
    
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.deliveryPackage.trackingNumber = :trackingNumber ORDER BY lh.timestamp DESC")
    List<LocationHistory> findByTrackingNumberOrderByTimestampDesc(@Param("trackingNumber") String trackingNumber);
} 