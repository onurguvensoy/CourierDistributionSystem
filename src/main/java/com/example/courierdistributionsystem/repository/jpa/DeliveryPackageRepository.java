package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPackageRepository extends JpaRepository<DeliveryPackage, Long> {
    List<DeliveryPackage> findByCustomer(Customer customer);
    List<DeliveryPackage> findByCourier(Courier courier);
    List<DeliveryPackage> findByStatus(DeliveryPackage.DeliveryStatus status);
    List<DeliveryPackage> findByCustomer_Username(String username);
    List<DeliveryPackage> findByCourier_Username(String username);
    List<DeliveryPackage> findByCourierAndStatusIn(Courier courier, List<DeliveryPackage.DeliveryStatus> statuses);
    List<DeliveryPackage> findByCustomerAndStatusIn(Customer customer, List<DeliveryPackage.DeliveryStatus> statuses);
    List<DeliveryPackage> findByCustomerAndStatus(Customer customer, DeliveryPackage.DeliveryStatus status);
    List<DeliveryPackage> findByStatusAndCourierIsNull(DeliveryPackage.DeliveryStatus status);
    List<DeliveryPackage> findByCourierAndStatus(Courier courier, DeliveryPackage.DeliveryStatus status);
    Optional<DeliveryPackage> findByTrackingNumber(String trackingNumber);
    List<DeliveryPackage> findByCourier_UsernameAndStatus(String username, DeliveryPackage.DeliveryStatus status);
} 