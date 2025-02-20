package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByUsername(String username);
    List<Courier> findByAvailable(boolean available);
    List<Courier> findByVehicleType(String vehicleType);
    List<Courier> findByAvailableTrue();
    Optional<Courier> findByEmail(String email);
    List<Courier> findByCurrentZone(String zone);
} 