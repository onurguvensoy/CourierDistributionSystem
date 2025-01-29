package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourierService {

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private WebSocketService webSocketService;

    public Courier getCourierById(Long id) {
        return courierRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));
    }

    public List<Courier> getAvailableCouriers() {
        return courierRepository.findByAvailable(true);
    }

    public Courier updateCourierProfile(Long id, Map<String, String> courierRequest) {
        Courier courier = getCourierById(id);

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

    public Courier createCourierProfile(Map<String, String> courierRequest) {
        String username = courierRequest.get("username");
        String email = courierRequest.get("email");
        String password = courierRequest.get("password");
        String phoneNumber = courierRequest.get("phoneNumber");
        String vehicleType = courierRequest.get("vehicleType");

        if (phoneNumber == null || vehicleType == null) {
            throw new IllegalArgumentException("Phone number and vehicle type are required");
        }

        if (courierRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (courierRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Courier courier = Courier.builder()
            .username(username)
            .email(email)
            .password(password)
            .role(User.UserRole.COURIER)
            .phoneNumber(phoneNumber)
            .vehicleType(vehicleType)
            .available(true)
            .maxPackageCapacity(5)
            .currentPackageCount(0)
            .averageRating(0.0)
            .build();

        return courierRepository.save(courier);
    }

    public void updateCourierLocation(Long id, Map<String, String> locationRequest) {
        Courier courier = getCourierById(id);
        
        if (locationRequest.containsKey("latitude")) {
            courier.setCurrentLatitude(Double.parseDouble(locationRequest.get("latitude")));
        }
        if (locationRequest.containsKey("longitude")) {
            courier.setCurrentLongitude(Double.parseDouble(locationRequest.get("longitude")));
        }
        if (locationRequest.containsKey("zone")) {
            courier.setCurrentZone(locationRequest.get("zone"));
        }

        courierRepository.save(courier);
    }
} 