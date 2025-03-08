package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.repository.jpa.LocationHistoryRepository;
import com.example.courierdistributionsystem.repository.jpa.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.service.LocationHistoryService;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationHistoryServiceImpl implements LocationHistoryService {

    private final LocationHistoryRepository locationHistoryRepository;
    private final DeliveryPackageRepository deliveryPackageRepository;
    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public LocationHistory saveLocationUpdate(LocationUpdateDto locationUpdate, String courierUsername) {
        String trackingNumber = locationUpdate.getTrackingNumber();
        log.debug("Processing location update for package {} from courier {}", 
                 trackingNumber, courierUsername);
        
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> {
                log.error("Package not found with tracking number: {}", trackingNumber);
                return new ResourceNotFoundException(
                    "Package not found with tracking number: " + trackingNumber);
            });
            
        Courier courier = courierRepository.findByUsername(courierUsername)
            .orElseThrow(() -> {
                log.error("Courier not found with username: {}", courierUsername);
                return new ResourceNotFoundException(
                    "Courier not found with username: " + courierUsername);
            });


        if (!deliveryPackage.getCourier().getId().equals(courier.getId())) {
            String errorMsg = String.format("Courier %s is not assigned to package %s", 
                                          courierUsername, trackingNumber);
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        LocationHistory history = LocationHistory.builder()
            .deliveryPackage(deliveryPackage)
            .courier(courier)
            .latitude(locationUpdate.getLatitude())
            .longitude(locationUpdate.getLongitude())
            .zone(locationUpdate.getZone())
            .courierUsername(courierUsername)
            .timestamp(locationUpdate.getTimestamp() != null ? locationUpdate.getTimestamp() : LocalDateTime.now())
            .build();

        log.debug("Saving location history for package {}: {}", trackingNumber, history);
        return locationHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationHistory> getLocationHistory(String trackingNumber) {
        log.debug("Retrieving location history for package {}", trackingNumber);
        return locationHistoryRepository.findByTrackingNumberOrderByTimestampDesc(trackingNumber);
    }
} 