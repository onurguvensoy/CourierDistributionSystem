package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.CustomerDashboardDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomerDashboardMapper {
    
    private final DeliveryPackageMapper deliveryPackageMapper;

    public CustomerDashboardMapper(DeliveryPackageMapper deliveryPackageMapper) {
        this.deliveryPackageMapper = deliveryPackageMapper;
    }

    public CustomerDashboardDto toDto(Customer customer, List<DeliveryPackage> activeDeliveries, 
                                    List<DeliveryPackage> deliveryHistory, Map<String, Object> stats) {
        if (customer == null) {
            return null;
        }

        CustomerDashboardDto dto = new CustomerDashboardDto();
        dto.setCustomerId(customer.getId());
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setPhoneNumber(customer.getPhoneNumber());
        
        // Map active deliveries
        List<DeliveryPackageDto> activeDeliveriesDto = activeDeliveries.stream()
            .map(deliveryPackageMapper::toDto)
            .collect(Collectors.toList());
        dto.setActiveDeliveries(activeDeliveriesDto);

        // Map delivery history
        List<DeliveryPackageDto> deliveryHistoryDto = deliveryHistory.stream()
            .map(deliveryPackageMapper::toDto)
            .collect(Collectors.toList());
        dto.setDeliveryHistory(deliveryHistoryDto);

        // Set statistics
        dto.setStats(stats);

        return dto;
    }
} 