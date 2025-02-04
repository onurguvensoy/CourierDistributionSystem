package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CustomerDashboardDTO;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private DeliveryPackageRepository packageRepository;

    public CustomerDashboardDTO getCustomerDashboard(User user) {
        if (!(user instanceof Customer)) {
            throw new IllegalArgumentException("User must be a customer");
        }
        Customer customer = (Customer) user;
        
        List<DeliveryPackage> allPackages = packageRepository.findByCustomer(customer);
        
        List<DeliveryPackage> activePackages = allPackages.stream()
            .filter(pkg -> !pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.DELIVERED) 
                && !pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.CANCELLED))
            .collect(Collectors.toList());

        List<DeliveryPackage> completedPackages = allPackages.stream()
            .filter(pkg -> pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.DELIVERED) 
                || pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.CANCELLED))
            .collect(Collectors.toList());

        long deliveredCount = allPackages.stream()
            .filter(pkg -> pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.DELIVERED))
            .count();

        long cancelledCount = allPackages.stream()
            .filter(pkg -> pkg.getStatus().equals(DeliveryPackage.DeliveryStatus.CANCELLED))
            .count();

        CustomerDashboardDTO.DashboardStats stats = CustomerDashboardDTO.DashboardStats.builder()
            .totalPackages(allPackages.size())
            .activeShipments(activePackages.size())
            .deliveredPackages(deliveredCount)
            .cancelledPackages(cancelledCount)
            .build();

        return CustomerDashboardDTO.builder()
            .activePackages(activePackages)
            .completedPackages(completedPackages)
            .stats(stats)
            .build();
    }
} 