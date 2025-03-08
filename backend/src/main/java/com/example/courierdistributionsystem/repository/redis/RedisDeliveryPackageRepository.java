package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RedisDeliveryPackageRepository extends CrudRepository<DeliveryPackage, String> {
    Optional<DeliveryPackage> findByTrackingNumber(String trackingNumber);
    List<DeliveryPackage> findByStatus(DeliveryPackage.DeliveryStatus status);
    List<DeliveryPackage> findByCourierUsername(String courierUsername);
    List<DeliveryPackage> findByCustomerUsername(String customerUsername);
    void deleteByTrackingNumber(String trackingNumber);
} 