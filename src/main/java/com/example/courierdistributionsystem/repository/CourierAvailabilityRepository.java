package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.CourierAvailability;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierAvailabilityRepository extends JpaRepository<CourierAvailability, Long> {
    Optional<CourierAvailability> findByCourier(User courier);
    List<CourierAvailability> findByAvailableTrue();
    List<CourierAvailability> findByCurrentZone(String zone);
    
    @Query("SELECT ca FROM CourierAvailability ca WHERE ca.available = true AND ca.currentPackageCount < ca.maxPackageCapacity")
    List<CourierAvailability> findAvailableCouriersWithCapacity();
} 