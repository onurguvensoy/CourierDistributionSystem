package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.CourierDto;
import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.LocationHistory;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.repository.jpa.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.jpa.LocationHistoryRepository;
import com.example.courierdistributionsystem.service.ICourierService;
import com.example.courierdistributionsystem.mapper.CourierMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourierServiceImpl implements ICourierService {
    private static final Logger logger = LoggerFactory.getLogger(CourierServiceImpl.class);

    private final CourierRepository courierRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final DeliveryPackageRepository deliveryPackageRepository;
    private final CourierMapper courierMapper;

    @Autowired
    public CourierServiceImpl(
            CourierRepository courierRepository,
            LocationHistoryRepository locationHistoryRepository,
            DeliveryPackageRepository deliveryPackageRepository,
            CourierMapper courierMapper) {
        this.courierRepository = courierRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.deliveryPackageRepository = deliveryPackageRepository;
        this.courierMapper = courierMapper;
    }

    @Override
    @Cacheable(value = "couriers", key = "'available'")
    public List<CourierDto> getAllAvailableCouriers() {
        logger.debug("Fetching all available couriers");
        return courierRepository.findByAvailableTrue().stream()
                .map(courierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "couriers", key = "#username")
    public Optional<CourierDto> getCourierByUsername(String username) {
        logger.debug("Fetching courier by username: {}", username);
        return courierRepository.findByUsername(username)
                .map(courierMapper::toDto);
    }

    @Override
    @CacheEvict(value = "couriers", allEntries = true)
    @Transactional
    public CourierDto updateCourierLocation(String username, LocationUpdateDto location) {
        logger.debug("Updating location for courier: {}", username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        courier.setCurrentLatitude(location.getLatitude());
        courier.setCurrentLongitude(location.getLongitude());
        courier.setCurrentZone(location.getZone());

        // Find current active delivery package
        DeliveryPackage activeDelivery = deliveryPackageRepository.findByCourierAndStatus(
            courier, DeliveryPackage.DeliveryStatus.IN_PROGRESS)
            .stream()
            .findFirst()
            .orElse(null);

        LocationHistory locationHistory = LocationHistory.builder()
                .courier(courier)
                .deliveryPackage(activeDelivery)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .zone(location.getZone())
                .timestamp(LocalDateTime.now())
                .build();

        locationHistoryRepository.save(locationHistory);
        
        // Update package location if there's an active delivery
        if (activeDelivery != null) {
            activeDelivery.updateLocation(location.getLatitude(), location.getLongitude(), location.getZone());
            deliveryPackageRepository.save(activeDelivery);
        }

        return courierMapper.toDto(courierRepository.save(courier));
    }

    @Override
    @CacheEvict(value = "couriers", allEntries = true)
    @Transactional
    public CourierDto updateCourierAvailability(String username, boolean available) {
        logger.debug("Updating availability for courier: {} to {}", username, available);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        courier.setAvailable(available);
        return courierMapper.toDto(courierRepository.save(courier));
    }

    @Override
    @Cacheable(value = "couriers", key = "'zone_' + #zone")
    public List<CourierDto> getCouriersByZone(String zone) {
        logger.debug("Fetching couriers in zone: {}", zone);
        return courierRepository.findByCurrentZone(zone).stream()
                .map(courierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getCourierStats(String username) {
        logger.debug("Fetching stats for courier: {}", username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        Map<String, Object> stats = new HashMap<>();
        List<DeliveryPackage> deliveries = courier.getDeliveries();

        stats.put("totalDeliveries", deliveries.size());
        stats.put("completedDeliveries", deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                .count());
        stats.put("activeDeliveries", deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryPackage.DeliveryStatus.IN_PROGRESS)
                .count());

        return stats;
    }

    @Override
    @CacheEvict(value = "couriers", allEntries = true)
    @Transactional
    public CourierDto updateCourierProfile(String username, Map<String, String> updates) {
        logger.debug("Updating profile for courier: {}", username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        if (updates.containsKey("phoneNumber")) {
            courier.setPhoneNumber(updates.get("phoneNumber"));
        }
        if (updates.containsKey("vehicleType")) {
            courier.setVehicleType(updates.get("vehicleType"));
        }

        return courierMapper.toDto(courierRepository.save(courier));
    }

    @Override
    @CacheEvict(value = "couriers", allEntries = true)
    @Transactional
    public void deleteCourier(String username) {
        logger.debug("Deleting courier: {}", username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        courierRepository.delete(courier);
    }

    @Override
    @Cacheable(value = "couriers", key = "'all'")
    public List<CourierDto> getAllCouriers() {
        logger.debug("Fetching all couriers");
        return courierRepository.findAll().stream()
                .map(courierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "couriers", key = "'id_' + #id")
    public Optional<CourierDto> getCourierById(Long id) {
        logger.debug("Fetching courier by ID: {}", id);
        return courierRepository.findById(id)
                .map(courierMapper::toDto);
    }

    @Override
    @CacheEvict(value = "couriers", allEntries = true)
    @Transactional
    public CourierDto createCourier(CourierDto courierDto) {
        logger.debug("Creating new courier: {}", courierDto.getUsername());
        Courier courier = courierMapper.toEntity(courierDto);
        return courierMapper.toDto(courierRepository.save(courier));
    }

    @Override
    @CacheEvict(value = "couriers", allEntries = true)
    @Transactional
    public void updateCourierStatus(String username, String status) {
        logger.debug("Updating status for courier: {} to {}", username, status);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        courier.setAvailable("AVAILABLE".equalsIgnoreCase(status));
        courierRepository.save(courier);
    }

    @Override
    public Map<String, Object> getCourierDeliveryHistory(String username) {
        logger.debug("Fetching delivery history for courier: {}", username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        Map<String, Object> history = new HashMap<>();
        List<DeliveryPackage> deliveries = courier.getDeliveries();

        history.put("totalDeliveries", deliveries.size());
        history.put("deliveries", deliveries.stream()
                .map(this::convertDeliveryToMap)
                .collect(Collectors.toList()));

        return history;
    }

    @Override
    public Map<String, Object> getCourierPerformanceMetrics(String username) {
        logger.debug("Calculating performance metrics for courier: {}", username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        Map<String, Object> metrics = new HashMap<>();
        List<DeliveryPackage> deliveries = courier.getDeliveries();

        long completedDeliveries = deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                .count();

        metrics.put("completionRate", calculateCompletionRate(deliveries.size(), completedDeliveries));
        metrics.put("averageDeliveryTime", calculateAverageDeliveryTime(deliveries));
        metrics.put("totalDistance", calculateTotalDistance(courier));

        return metrics;
    }

    @Override
    @Transactional
    public void assignDeliveryToCourier(Long deliveryId, String username) {
        logger.debug("Assigning delivery {} to courier: {}", deliveryId, username);
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        DeliveryPackage deliveryPackage = deliveryPackageRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery package not found"));

        if (deliveryPackage.getCourier() != null) {
            throw new RuntimeException("Delivery package already assigned to a courier");
        }

        deliveryPackage.setCourier(courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.IN_PROGRESS);
        deliveryPackageRepository.save(deliveryPackage);
    }

    @Override
    @Transactional
    public void unassignDeliveryFromCourier(Long deliveryId, String username) {
        logger.debug("Unassigning delivery {} from courier: {}", deliveryId, username);
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery package not found"));

        if (!username.equals(deliveryPackage.getCourier().getUsername())) {
            throw new RuntimeException("Delivery not assigned to this courier");
        }

        deliveryPackage.setCourier(null);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        deliveryPackageRepository.save(deliveryPackage);
    }

    private Map<String, Object> convertDeliveryToMap(DeliveryPackage delivery) {
        Map<String, Object> deliveryMap = new HashMap<>();
        deliveryMap.put("id", delivery.getPackage_id());
        deliveryMap.put("status", delivery.getStatus());
        deliveryMap.put("createdAt", delivery.getCreatedAt());
        deliveryMap.put("updatedAt", delivery.getUpdatedAt());
        deliveryMap.put("pickupAddress", delivery.getPickupAddress());
        deliveryMap.put("deliveryAddress", delivery.getDeliveryAddress());
        return deliveryMap;
    }

    private double calculateCompletionRate(int total, long completed) {
        return total == 0 ? 0 : (completed * 100.0) / total;
    }

    private double calculateAverageDeliveryTime(List<DeliveryPackage> deliveries) {
        return deliveries.stream()
                .filter(d -> d.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                .mapToLong(d -> {
                    LocalDateTime start = d.getPickedUpAt();
                    LocalDateTime end = d.getDeliveredAt();
                    return start != null && end != null ? 
                            java.time.Duration.between(start, end).toMinutes() : 0;
                })
                .average()
                .orElse(0.0);
    }

    private double calculateTotalDistance(Courier courier) {
        return locationHistoryRepository.findByCourier(courier).stream()
                .mapToDouble(this::calculateDistanceBetweenPoints)
                .sum();
    }

    private double calculateDistanceBetweenPoints(LocationHistory location) {
        // Implement distance calculation logic here
        // This is a simplified version, you might want to implement actual distance calculation
        return 0.0;
    }
} 