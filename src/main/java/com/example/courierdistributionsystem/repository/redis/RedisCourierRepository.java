package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RedisCourierRepository extends CrudRepository<Courier, String> {
    Optional<Courier> findByUsername(String username);
    Optional<Courier> findByEmail(String email);
    List<Courier> findByAvailable(boolean available);
    List<Courier> findByVehicleType(String vehicleType);
    List<Courier> findByCurrentZone(String zone);
    List<Courier> findByAvailableTrue();
} 