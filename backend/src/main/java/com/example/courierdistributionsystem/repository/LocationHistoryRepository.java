package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.LocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
    
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.deliveryPackage.id = :packageId ORDER BY lh.timestamp DESC")
    List<LocationHistory> findByPackageIdOrderByTimestampDesc(@Param("packageId") Long packageId);
    
    @Query("SELECT lh FROM LocationHistory lh WHERE lh.deliveryPackage.trackingNumber = :trackingNumber ORDER BY lh.timestamp DESC")
    List<LocationHistory> findByTrackingNumberOrderByTimestampDesc(@Param("trackingNumber") String trackingNumber);
} 