package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CreatePackageRequest;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.Rating;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import com.example.courierdistributionsystem.repository.DeliveryReportRepository;
import com.example.courierdistributionsystem.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DeliveryPackageService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryPackageService.class);

    private final DeliveryPackageRepository deliveryPackageRepository;
    private final CustomerRepository customerRepository;
    private final CourierRepository courierRepository;
    private final DeliveryReportRepository deliveryReportRepository;
    private final RatingRepository ratingRepository;
    private final WebSocketService webSocketService;
    private final UserService userService;

    public DeliveryPackageService(
            DeliveryPackageRepository deliveryPackageRepository,
            CustomerRepository customerRepository,
            CourierRepository courierRepository,
            DeliveryReportRepository deliveryReportRepository,
            RatingRepository ratingRepository,
            WebSocketService webSocketService,
            UserService userService) {
        this.deliveryPackageRepository = deliveryPackageRepository;
        this.customerRepository = customerRepository;
        this.courierRepository = courierRepository;
        this.deliveryReportRepository = deliveryReportRepository;
        this.ratingRepository = ratingRepository;
        this.webSocketService = webSocketService;
        this.userService = userService;
    }

    public List<DeliveryPackage> getAllDeliveryPackages() {
        logger.debug("Fetching all delivery packages");
        return deliveryPackageRepository.findAll();
    }

    public DeliveryPackage getDeliveryPackageById(Long id) {
        logger.debug("Fetching delivery package with id: {}", id);
        return deliveryPackageRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Delivery package not found with id: {}", id);
                return new IllegalArgumentException("Delivery package not found with id: " + id);
            });
    }

    @Transactional
    public DeliveryPackage createDeliveryPackage(Map<String, String> deliveryRequest) {
        logger.info("Creating new delivery package");
        
        // Extract and validate required fields
        String username = validateRequiredField(deliveryRequest, "username", "Username");
        String pickupAddress = validateRequiredField(deliveryRequest, "pickupAddress", "Pickup address");
        String deliveryAddress = validateRequiredField(deliveryRequest, "deliveryAddress", "Delivery address");
        String description = validateRequiredField(deliveryRequest, "description", "Description");
        String weightStr = validateRequiredField(deliveryRequest, "weight", "Weight");
        String specialInstructions = deliveryRequest.getOrDefault("specialInstructions", "");

        // Find customer
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("Customer not found with username: {}", username);
                return new IllegalArgumentException("Customer not found");
            });

        // Parse and validate weight
        double weight = parseWeight(weightStr);

        // Create delivery package
        DeliveryPackage newPackage = DeliveryPackage.builder()
            .customer(customer)
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .description(description)
            .specialInstructions(specialInstructions)
            .weight(weight)
            .status(DeliveryPackage.DeliveryStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // Save package
        DeliveryPackage savedPackage = deliveryPackageRepository.save(newPackage);
        logger.info("Successfully created delivery package with id: {}", savedPackage.getPackage_id());

        // Notify through WebSocket
        webSocketService.notifyNewDeliveryAvailable(savedPackage);

        return savedPackage;
    }

    private String validateRequiredField(Map<String, String> request, String field, String fieldName) {
        String value = request.get(field);
        if (value == null || value.trim().isEmpty()) {
            logger.error("Missing required field: {}", fieldName);
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.trim();
    }

    private double parseWeight(String weightStr) {
        try {
            double weight = Double.parseDouble(weightStr);
            if (weight <= 0) {
                logger.error("Invalid weight value: {}. Weight must be greater than 0", weightStr);
                throw new IllegalArgumentException("Weight must be greater than 0");
            }
            return weight;
        } catch (NumberFormatException e) {
            logger.error("Invalid weight format: {}", weightStr);
            throw new IllegalArgumentException("Invalid weight format");
        }
    }

    public DeliveryPackage updateDeliveryPackage(Long id, Map<String, String> deliveryRequest) {
        DeliveryPackage existingDelivery = getDeliveryPackageById(id);

        if (existingDelivery.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalArgumentException("Can only update pending delivery packages");
        }

        if (deliveryRequest.containsKey("deliveryAddress")) {
            existingDelivery.setDeliveryAddress(deliveryRequest.get("deliveryAddress"));
        }
        if (deliveryRequest.containsKey("pickupAddress")) {
            existingDelivery.setPickupAddress(deliveryRequest.get("pickupAddress"));
        }
        if (deliveryRequest.containsKey("description")) {
            existingDelivery.setDescription(deliveryRequest.get("description"));
        }
        if (deliveryRequest.containsKey("weight")) {
            existingDelivery.setWeight(Double.parseDouble(deliveryRequest.get("weight")));
        }
        if (deliveryRequest.containsKey("specialInstructions")) {
            existingDelivery.setSpecialInstructions(deliveryRequest.get("specialInstructions"));
        }

        DeliveryPackage updatedDelivery = deliveryPackageRepository.save(existingDelivery);
        webSocketService.notifyDeliveryStatusUpdate(updatedDelivery);
        return updatedDelivery;
    }

    public void cancelDeliveryPackage(Long id, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(id);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Delivery package does not belong to this customer");
        }

        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel delivered packages");
        }

        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.CANCELLED);
        deliveryPackage.setCancelledAt(LocalDateTime.now());

        if (deliveryPackage.getCourier() != null) {
            Courier courier = deliveryPackage.getCourier();
            courier.setAvailable(true);
            courierRepository.save(courier);
        }

        DeliveryPackage cancelledDelivery = deliveryPackageRepository.save(deliveryPackage);
        webSocketService.notifyDeliveryStatusUpdate(cancelledDelivery);
    }

    public List<DeliveryPackage> getCustomerDeliveryPackages(String username) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        return deliveryPackageRepository.findByCustomer(customer);
    }

    public Map<String, Object> trackDeliveryPackage(Long id, String username) {
        logger.info("Tracking package {} for user {}", id, username);
        
        DeliveryPackage deliveryPackage = getDeliveryPackageById(id);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("Customer not found with username: {}", username);
                return new IllegalArgumentException("Customer not found");
            });

        if (!deliveryPackage.getCustomer().equals(customer)) {
            logger.warn("Unauthorized tracking attempt for package {} by user {}", id, username);
            throw new IllegalArgumentException("Unauthorized to track this package");
        }

        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("status", deliveryPackage.getStatus());
        trackingInfo.put("createdAt", deliveryPackage.getCreatedAt());
        trackingInfo.put("updatedAt", deliveryPackage.getUpdatedAt());

        if (deliveryPackage.getCourier() != null) {
            Courier courier = deliveryPackage.getCourier();
            trackingInfo.put("courierName", courier.getUsername());
            trackingInfo.put("courierPhone", courier.getPhoneNumber());
            if (courier.getCurrentLatitude() != null && courier.getCurrentLongitude() != null) {
                trackingInfo.put("currentLocation", Map.of(
                    "latitude", courier.getCurrentLatitude(),
                    "longitude", courier.getCurrentLongitude(),
                    "zone", courier.getCurrentZone()
                ));
            }
        }

        logger.debug("Retrieved tracking info for package {}", id);
        return trackingInfo;
    }

    public List<DeliveryPackage> getAvailableDeliveryPackages() {
        return deliveryPackageRepository.findByStatus(DeliveryPackage.DeliveryStatus.PENDING);
    }

    public List<DeliveryPackage> getCourierActiveDeliveryPackages(String username) {
        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        return deliveryPackageRepository.findByCourierAndStatusIn(courier, 
            List.of(DeliveryPackage.DeliveryStatus.ASSIGNED, 
                   DeliveryPackage.DeliveryStatus.PICKED_UP, 
                   DeliveryPackage.DeliveryStatus.IN_TRANSIT));
    }

    @Transactional
    public DeliveryPackage updateDeliveryStatus(Long packageId, String username, DeliveryPackage.DeliveryStatus status) {
        logger.info("Updating delivery status for package {} to {} by courier {}", packageId, status, username);
        
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        // Validate courier
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            logger.warn("Unauthorized status update attempt for package {} by courier {}", packageId, username);
            throw new IllegalArgumentException("Unauthorized to update this package");
        }

        // Validate status transition
        validateStatusTransition(deliveryPackage.getStatus(), status);

        deliveryPackage.setStatus(status);
        updateStatusTimestamp(deliveryPackage, status);
        
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        logger.info("Successfully updated package {} status to {}", packageId, status);

        // Send WebSocket notification to customer
        webSocketService.sendPackageUpdate(deliveryPackage.getCustomer().getUsername(), updatedPackage);

        // If delivered, send rating prompt
        if (status == DeliveryPackage.DeliveryStatus.DELIVERED) {
            webSocketService.sendRatingPrompt(deliveryPackage.getCustomer().getUsername(), packageId);
        }

        return updatedPackage;
    }

    private void validateStatusTransition(DeliveryPackage.DeliveryStatus currentStatus, DeliveryPackage.DeliveryStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == DeliveryPackage.DeliveryStatus.ASSIGNED;
            case ASSIGNED -> newStatus == DeliveryPackage.DeliveryStatus.PICKED_UP;
            case PICKED_UP -> newStatus == DeliveryPackage.DeliveryStatus.IN_TRANSIT;
            case IN_TRANSIT -> newStatus == DeliveryPackage.DeliveryStatus.DELIVERED;
            default -> false;
        };

        if (!isValid) {
            logger.warn("Invalid status transition from {} to {}", currentStatus, newStatus);
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void updateStatusTimestamp(DeliveryPackage deliveryPackage, DeliveryPackage.DeliveryStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case ASSIGNED -> deliveryPackage.setAssignedAt(now);
            case PICKED_UP -> deliveryPackage.setPickedUpAt(now);
            case DELIVERED -> deliveryPackage.setDeliveredAt(now);
            case CANCELLED -> deliveryPackage.setCancelledAt(now);
        }
        deliveryPackage.setUpdatedAt(now);
    }

    @Transactional
    public DeliveryPackage updateDeliveryLocation(Long packageId, String username, Double latitude, Double longitude, String location) {
        logger.info("Updating location for package {} by courier {}", packageId, username);
        
        if (latitude == null || longitude == null) {
            logger.error("Missing latitude or longitude for location update");
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        // Validate courier
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            logger.warn("Unauthorized location update attempt for package {} by courier {}", packageId, username);
            throw new IllegalArgumentException("Unauthorized to update this package");
        }

        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED ||
            deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.CANCELLED) {
            logger.warn("Cannot update location for package {} with status {}", packageId, deliveryPackage.getStatus());
            throw new IllegalStateException("Cannot update location for " + deliveryPackage.getStatus() + " package");
        }

        deliveryPackage.setCurrentLatitude(latitude);
        deliveryPackage.setCurrentLongitude(longitude);
        deliveryPackage.setCurrentLocation(location);
        deliveryPackage.setUpdatedAt(LocalDateTime.now());
        
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        logger.info("Successfully updated location for package {}", packageId);

        // Send WebSocket notifications
        String customerUsername = deliveryPackage.getCustomer().getUsername();
        webSocketService.sendLocationUpdate(customerUsername, packageId, location, latitude, longitude);
        webSocketService.sendPackageUpdate(customerUsername, updatedPackage);

        return updatedPackage;
    }

    @Transactional
    public DeliveryPackage takeDeliveryPackage(Long packageId, String username) {
        logger.info("Attempting to assign package {} to courier {}", packageId, username);
        
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            logger.warn("Package {} is not available for assignment. Current status: {}", packageId, deliveryPackage.getStatus());
            throw new IllegalStateException("Package is not available for assignment");
        }

        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("Courier not found with username: {}", username);
                return new IllegalArgumentException("Courier not found");
            });

        if (!courier.isAvailable()) {
            logger.warn("Courier {} is not available for new assignments", username);
            throw new IllegalStateException("Courier is not available for new assignments");
        }

        deliveryPackage.setCourier(courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.ASSIGNED);
        deliveryPackage.setAssignedAt(LocalDateTime.now());
        
        courier.setAvailable(false);
        courierRepository.save(courier);

        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        logger.info("Successfully assigned package {} to courier {}", packageId, username);

        // Send WebSocket notification to customer
        webSocketService.sendPackageUpdate(deliveryPackage.getCustomer().getUsername(), updatedPackage);

        return updatedPackage;
    }

    @Transactional
    public DeliveryPackage dropDeliveryPackage(Long packageId, String username) {
        logger.info("Attempting to drop package {} by courier {}", packageId, username);
        
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            logger.warn("Unauthorized drop attempt for package {} by courier {}", packageId, username);
            throw new IllegalArgumentException("Unauthorized to drop this package");
        }

        if (deliveryPackage.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED) {
            logger.warn("Cannot drop delivered package {}", packageId);
            throw new IllegalStateException("Cannot drop a delivered package");
        }

        Courier courier = deliveryPackage.getCourier();
        courier.setAvailable(true);
        courierRepository.save(courier);

        deliveryPackage.setCourier(null);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        deliveryPackage.setUpdatedAt(LocalDateTime.now());
        
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        logger.info("Successfully dropped package {}", packageId);

        // Send WebSocket notification to customer
        webSocketService.sendPackageUpdate(deliveryPackage.getCustomer().getUsername(), updatedPackage);

        return updatedPackage;
    }

    // Delivery Report Methods
    public List<DeliveryReport> getAllDeliveryReports() {
        return deliveryReportRepository.findAll();
    }

    public List<DeliveryReport> getCourierDeliveryReports(String username) {
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        return deliveryReportRepository.findByCourier(courier);
    }

    public DeliveryReport createDeliveryReport(Long packageId, DeliveryReport report, String username) {
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);

        report.setDeliveryPackage(deliveryPackage);
        report.setCourier(courier);
        report.setCompletionTime(LocalDateTime.now());
        
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.DELIVERED);
        deliveryPackageRepository.save(deliveryPackage);

        return deliveryReportRepository.save(report);
    }

    public DeliveryReport getDeliveryReportById(Long id) {
        return deliveryReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery report not found"));
    }

    public List<DeliveryReport> getDeliveryReportsByPackage(Long packageId) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        return deliveryReportRepository.findByDeliveryPackage(deliveryPackage);
    }

    public DeliveryReport updateDeliveryReport(Long id, DeliveryReport reportDetails) {
        DeliveryReport report = getDeliveryReportById(id);

        report.setDeliveryTime(reportDetails.getDeliveryTime());
        report.setDeliveryNotes(reportDetails.getDeliveryNotes());
        report.setCustomerConfirmation(reportDetails.isCustomerConfirmation());
        report.setDeliveryRating(reportDetails.getDeliveryRating());
        report.setDeliveryPhotoUrl(reportDetails.getDeliveryPhotoUrl());
        report.setSignatureUrl(reportDetails.getSignatureUrl());
        report.setDistanceTraveled(reportDetails.getDistanceTraveled());

        return deliveryReportRepository.save(report);
    }

    public void deleteDeliveryReport(Long id) {
        DeliveryReport report = getDeliveryReportById(id);
        deliveryReportRepository.delete(report);
    }

    /**
     * Get active delivery packages for a customer (PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT)
     */
    public List<DeliveryPackage> getCustomerActiveDeliveryPackages(String username) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        return deliveryPackageRepository.findByCustomerAndStatusIn(customer, 
            List.of(DeliveryPackage.DeliveryStatus.PENDING,
                   DeliveryPackage.DeliveryStatus.ASSIGNED,
                   DeliveryPackage.DeliveryStatus.PICKED_UP,
                   DeliveryPackage.DeliveryStatus.IN_TRANSIT));
    }

    /**
     * Get completed delivery packages for a customer (DELIVERED)
     */
    public List<DeliveryPackage> getCustomerDeliveryHistory(String username) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        return deliveryPackageRepository.findByCustomerAndStatus(customer, 
            DeliveryPackage.DeliveryStatus.DELIVERED);
    }

    /**
     * Rate a completed delivery
     */
    @Transactional
    public Rating rateDelivery(Long packageId, String username, Map<String, Object> ratingRequest) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Validate customer ownership
        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Delivery package does not belong to this customer");
        }

        // Validate delivery status
        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.DELIVERED) {
            throw new IllegalArgumentException("Can only rate delivered packages");
        }

        // Check if already rated
        if (ratingRepository.existsByDeliveryPackageAndCustomer(deliveryPackage, customer)) {
            throw new IllegalArgumentException("Delivery has already been rated");
        }

        // Validate rating value
        Double ratingValue = ((Number) ratingRequest.get("rating")).doubleValue();
        if (ratingValue < 1 || ratingValue > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Create rating
        Rating rating = Rating.builder()
            .customer(customer)
            .courier(deliveryPackage.getCourier())
            .deliveryPackage(deliveryPackage)
            .courierRating(ratingValue)
            .deliveryRating(ratingValue)
            .comment((String) ratingRequest.get("comment"))
            .anonymous((Boolean) ratingRequest.getOrDefault("anonymous", false))
            .build();

        Rating savedRating = ratingRepository.save(rating);

        // Update courier's average rating
        Courier courier = deliveryPackage.getCourier();
        Double averageRating = ratingRepository.getAverageCourierRating(courier);
        courier.setAverageRating(averageRating != null ? averageRating : 0.0);
        courierRepository.save(courier);

        return savedRating;
    }

    /**
     * Get rating for a specific delivery
     */
    public Rating getDeliveryRating(Long packageId, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Delivery package does not belong to this customer");
        }

        return ratingRepository.findByDeliveryPackageAndCustomer(deliveryPackage, customer)
            .orElse(null);
    }

    @Transactional
    public DeliveryPackage createPackage(CreatePackageRequest request, Customer customer) {
        if (request.getWeight() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }

        DeliveryPackage newPackage = DeliveryPackage.builder()
            .customer(customer)
            .pickupAddress(request.getPickupAddress())
            .deliveryAddress(request.getDeliveryAddress())
            .weight(request.getWeight())
            .description(request.getDescription())
            .specialInstructions(request.getSpecialInstructions())
            .status(DeliveryPackage.DeliveryStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        DeliveryPackage savedPackage = deliveryPackageRepository.save(newPackage);
        webSocketService.notifyNewDeliveryAvailable(savedPackage);
        
        return savedPackage;
    }
}
