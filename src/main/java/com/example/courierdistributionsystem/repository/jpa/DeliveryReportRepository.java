package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeliveryReportRepository extends JpaRepository<DeliveryReport, Long> {
    List<DeliveryReport> findByCourier(Courier courier);
    List<DeliveryReport> findByDeliveryPackage(DeliveryPackage deliveryPackage);
    List<DeliveryReport> findByCourier_Username(String username);
} 