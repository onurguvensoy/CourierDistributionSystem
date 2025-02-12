package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisDeliveryPackageRepository extends CrudRepository<DeliveryPackage, String> {
} 