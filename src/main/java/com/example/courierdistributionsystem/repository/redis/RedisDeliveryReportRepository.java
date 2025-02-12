package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.DeliveryReport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisDeliveryReportRepository extends CrudRepository<DeliveryReport, String> {
} 