package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CourierDto;
import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICourierService {
    List<CourierDto> getAllAvailableCouriers();
    Optional<CourierDto> getCourierByUsername(String username);
    CourierDto updateCourierLocation(String username, LocationUpdateDto location);
    CourierDto updateCourierAvailability(String username, boolean available);
    List<CourierDto> getCouriersByZone(String zone);
    Map<String, Object> getCourierStats(String username);
    CourierDto updateCourierProfile(String username, Map<String, String> updates);
    void deleteCourier(String username);
    List<CourierDto> getAllCouriers();
    Optional<CourierDto> getCourierById(Long id);
    CourierDto createCourier(CourierDto courierDto);
    void updateCourierStatus(String username, String status);
    Map<String, Object> getCourierDeliveryHistory(String username);
    Map<String, Object> getCourierPerformanceMetrics(String username);
    void assignDeliveryToCourier(Long deliveryId, String username);
    void unassignDeliveryFromCourier(Long deliveryId, String username);
} 