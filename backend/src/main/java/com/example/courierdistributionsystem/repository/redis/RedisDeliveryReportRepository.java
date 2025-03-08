package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.DeliveryReport;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RedisDeliveryReportRepository extends CrudRepository<DeliveryReport, String> {
    List<DeliveryReport> findByCourierUsername(String username);
    List<DeliveryReport> findByCustomerUsername(String username);
    List<DeliveryReport> findByDeliveryPackageId(Long packageId);
    List<DeliveryReport> findByDeliveryTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<DeliveryReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
} 