package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.CourierRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourierService {

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    public Courier getCourierByUserId(Long userId) {
        return courierRepository.findByUserId(userId);
    }

    public List<Courier> getAvailableCouriers() {
        return courierRepository.findByAvailable(true);
    }

    public Courier updateCourierProfile(Long userId, Map<String, String> courierRequest) {
        Courier courier = getCourierByUserId(userId);
        if (courier == null) {
            throw new IllegalArgumentException("Courier not found");
        }

        if (courierRequest.containsKey("phoneNumber")) {
            courier.setPhoneNumber(courierRequest.get("phoneNumber"));
        }
        if (courierRequest.containsKey("vehicleType")) {
            courier.setVehicleType(courierRequest.get("vehicleType"));
        }
        if (courierRequest.containsKey("isAvailable")) {
            courier.setAvailable(Boolean.parseBoolean(courierRequest.get("isAvailable")));
        }
        if (courierRequest.containsKey("currentZone")) {
            courier.setCurrentZone(courierRequest.get("currentZone"));
        }

        Courier updatedCourier = courierRepository.save(courier);
        webSocketService.notifyCourierAssignment(updatedCourier.getId(), Map.of(
            "type", "COURIER_UPDATE",
            "courier", updatedCourier
        ));
        return updatedCourier;
    }

    public Courier createCourierProfile(Long userId, Map<String, String> courierRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        if (courierRepository.findByUserId(userId) != null) {
            throw new IllegalArgumentException("Courier profile already exists");
        }

        String phoneNumber = courierRequest.get("phoneNumber");
        String vehicleType = courierRequest.get("vehicleType");

        if (phoneNumber == null || vehicleType == null) {
            throw new IllegalArgumentException("Phone number and vehicle type are required");
        }

        Courier courier = Courier.builder()
            .user(user)
            .phoneNumber(phoneNumber)
            .vehicleType(vehicleType)
            .available(true)
            .maxPackageCapacity(5)
            .currentPackageCount(0)
            .averageRating(0.0)
            .build();

        return courierRepository.save(courier);
    }

    public void updateCourierLocation(Long userId, Map<String, String> locationRequest) {
        Courier courier = getCourierByUserId(userId);
        if (courier == null) {
            throw new IllegalArgumentException("Courier not found");
        }

        String latitude = locationRequest.get("latitude");
        String longitude = locationRequest.get("longitude");
        String zone = locationRequest.get("zone");

        if (latitude != null && longitude != null) {
            courier.setCurrentLatitude(Double.parseDouble(latitude));
            courier.setCurrentLongitude(Double.parseDouble(longitude));
        }

        if (zone != null) {
            courier.setCurrentZone(zone);
        }

        courierRepository.save(courier);
        webSocketService.notifyCourierAssignment(courier.getId(), Map.of(
            "type", "LOCATION_UPDATE",
            "courier", courier
        ));
    }
} 