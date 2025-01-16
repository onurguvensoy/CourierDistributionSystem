package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByCustomer(User customer);
    List<Package> findByCourier(User courier);
    List<Package> findByStatus(Package.PackageStatus status);
    List<Package> findByCourierAndStatusIn(User courier, List<Package.PackageStatus> statuses);
} 