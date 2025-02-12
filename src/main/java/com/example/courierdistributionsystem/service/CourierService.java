package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.exception.CourierException;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.socket.WebSocketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;


@Service
@Validated
public class CourierService {
    private static final Logger logger = LoggerFactory.getLogger(CourierService.class);

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private WebSocketService webSocketService;


    public Courier getCourierById(Long id) {
        logger.debug("Fetching courier with ID: {}", id);
        return courierRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Courier not found with ID: {}", id);
                return new CourierException("Courier not found with ID: " + id);
            });
    }

    public List<Courier> getAvailableCouriers() {
        logger.debug("Fetching all available couriers");
        List<Courier> couriers = courierRepository.findByAvailableTrue();
        logger.info("Found {} available couriers", couriers.size());
        return couriers;
    }

    @Transactional
    public Courier updateCourierProfile(Long id, Map<String, String> courierRequest) {
        logger.debug("Updating courier profile for ID: {} with data: {}", id, courierRequest);
        
        Courier courier = getCourierById(id);
        validateCourierUpdateRequest(courierRequest);

        try {
            updateCourierFields(courier, courierRequest);
            Courier updatedCourier = courierRepository.save(courier);
            logger.info("Successfully updated courier profile for ID: {}", id);
            webSocketService.notifyCourierAssignment(updatedCourier.getId(), Map.of(
                "type", "COURIER_UPDATE",
                "courier", updatedCourier
            ));
            return updatedCourier;
        } catch (Exception e) {
            logger.error("Failed to update courier profile: {}", e.getMessage(), e);
            throw new CourierException("Failed to update courier profile: " + e.getMessage());
        }
    }

    @Transactional
    public void updateCourierLocation(Long id, Map<String, String> locationRequest) {
        logger.debug("Updating courier location for ID: {} with data: {}", id, locationRequest);
        
        validateLocationRequest(locationRequest);
        Courier courier = getCourierById(id);

        try {
            updateLocationFields(courier, locationRequest);
            courierRepository.save(courier);
            logger.info("Successfully updated location for courier ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to update courier location: {}", e.getMessage(), e);
            throw new CourierException("Failed to update courier location: " + e.getMessage());
        }
    }

    private void validateCourierUpdateRequest(Map<String, String> request) {
        logger.debug("Validating courier update request: {}", request);
        
        if (request == null || request.isEmpty()) {
            logger.warn("Empty update request received");
            throw new CourierException("Update request cannot be empty");
        }

        if (request.containsKey("phoneNumber") && !isValidPhoneNumber(request.get("phoneNumber"))) {
            logger.warn("Invalid phone number format: {}", request.get("phoneNumber"));
            throw new CourierException("Invalid phone number format");
        }

        if (request.containsKey("vehicleType") && !isValidVehicleType(request.get("vehicleType"))) {
            logger.warn("Invalid vehicle type: {}", request.get("vehicleType"));
            throw new CourierException("Invalid vehicle type. Must be one of: CAR, MOTORCYCLE, BICYCLE");
        }
    }

    private void validateLocationRequest(Map<String, String> request) {
        logger.debug("Validating location update request: {}", request);
        
        if (request == null || request.isEmpty()) {
            logger.warn("Empty location request received");
            throw new CourierException("Location request cannot be empty");
        }

        if (!request.containsKey("latitude") || !request.containsKey("longitude")) {
            logger.warn("Missing latitude or longitude in request");
            throw new CourierException("Both latitude and longitude are required");
        }

        try {
            Double latitude = Double.parseDouble(request.get("latitude"));
            Double longitude = Double.parseDouble(request.get("longitude"));
            
            if (latitude < -90 || latitude > 90) {
                logger.warn("Invalid latitude value: {}", latitude);
                throw new CourierException("Latitude must be between -90 and 90");
            }
            
            if (longitude < -180 || longitude > 180) {
                logger.warn("Invalid longitude value: {}", longitude);
                throw new CourierException("Longitude must be between -180 and 180");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid coordinate format in request");
            throw new CourierException("Invalid coordinate format");
        }
    }

    private void updateCourierFields(Courier courier, Map<String, String> request) {
        if (request.containsKey("phoneNumber")) {
            courier.setPhoneNumber(request.get("phoneNumber"));
        }
        if (request.containsKey("vehicleType")) {
            courier.setVehicleType(request.get("vehicleType"));
        }
        if (request.containsKey("available")) {
            courier.setAvailable(Boolean.parseBoolean(request.get("available")));
        }
        if (request.containsKey("currentZone")) {
            courier.setCurrentZone(request.get("currentZone"));
        }
    }

    private void updateLocationFields(Courier courier, Map<String, String> request) {
        courier.setCurrentLatitude(Double.parseDouble(request.get("latitude")));
        courier.setCurrentLongitude(Double.parseDouble(request.get("longitude")));
        if (request.containsKey("zone")) {
            courier.setCurrentZone(request.get("zone"));
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    private boolean isValidVehicleType(String vehicleType) {
        return vehicleType != null && vehicleType.matches("^(CAR|MOTORCYCLE|BICYCLE)$");
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
            .build();

        return courierRepository.save(courier);
    }
} 