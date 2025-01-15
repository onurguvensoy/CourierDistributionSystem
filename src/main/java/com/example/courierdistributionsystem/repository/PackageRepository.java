package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    Optional<Package> findByTrackingNumber(String trackingNumber);
    List<Package> findByCourier(User courier);
    List<Package> findByCustomer(User customer);
    List<Package> findByCurrentStatus(Package.PackageStatus status);
    List<Package> findByCourierAndCurrentStatus(User courier, Package.PackageStatus status);
    List<Package> findByIsActiveTrue();
} 