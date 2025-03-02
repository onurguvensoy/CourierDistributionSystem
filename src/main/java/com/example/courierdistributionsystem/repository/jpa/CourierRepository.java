package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    @Query("SELECT c FROM Courier c WHERE c.available = true AND " +
           "ST_Distance_Sphere(point(c.currentLongitude, c.currentLatitude), " +
           "point(:longitude, :latitude)) <= :radiusMeters")
    List<Courier> findAvailableCouriersNearby(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeters") Double radiusMeters);
    
    @Query("SELECT c FROM Courier c WHERE c.available = true AND c.currentZone = :zone")
    List<Courier> findAvailableCouriersByZone(@Param("zone") String zone);
} 