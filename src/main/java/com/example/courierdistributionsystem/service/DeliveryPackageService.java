package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CreatePackageRequest;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.Rating;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.CustomerRepository;
import com.example.courierdistributionsystem.repository.CourierRepository;
import com.example.courierdistributionsystem.repository.DeliveryReportRepository;
import com.example.courierdistributionsystem.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DeliveryPackageService {

    @Autowired
    private DeliveryPackageRepository deliveryPackageRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private DeliveryReportRepository deliveryReportRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private UserService userService;

    public List<DeliveryPackage> getAllDeliveryPackages() {
        return deliveryPackageRepository.findAll();
    }

    public DeliveryPackage getDeliveryPackageById(Long id) {
        return deliveryPackageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Delivery package not found with id: " + id));
    }

    public DeliveryPackage createDeliveryPackage(Map<String, String> deliveryRequest) {
        String username = deliveryRequest.get("username");
        String pickupAddress = deliveryRequest.get("pickupAddress");
        String deliveryAddress = deliveryRequest.get("deliveryAddress");
        String description = deliveryRequest.get("description");
        String weightStr = deliveryRequest.get("weight");
        String specialInstructions = deliveryRequest.get("specialInstructions");

        // Validate required fields
        if (username == null || pickupAddress == null || deliveryAddress == null || 
            description == null || weightStr == null) {
            throw new IllegalArgumentException("Missing required fields");
        }


        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Parse and validate weight
        double weight;
        try {
            weight = Double.parseDouble(weightStr);
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be greater than 0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid weight format");
        }

        // Create delivery package
        DeliveryPackage newPackage = DeliveryPackage.builder()
            .customer(customer)
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .description(description)
            .specialInstructions(specialInstructions != null ? specialInstructions : "")
            .weight(weight)
            .status(DeliveryPackage.DeliveryStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        // Save package
        DeliveryPackage savedPackage = deliveryPackageRepository.save(newPackage);

        // Notify through WebSocket
        webSocketService.notifyNewDeliveryAvailable(savedPackage);

        return savedPackage;
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
        DeliveryPackage deliveryPackage = getDeliveryPackageById(id);
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!deliveryPackage.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Delivery package does not belong to this customer");
        }

        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("status", deliveryPackage.getStatus());
        trackingInfo.put("createdAt", deliveryPackage.getCreatedAt());
        trackingInfo.put("updatedAt", deliveryPackage.getUpdatedAt());

        if (deliveryPackage.getCourier() != null) {
            Courier courier = deliveryPackage.getCourier();
            trackingInfo.put("courierName", courier.getUsername());
            trackingInfo.put("courierPhone", courier.getPhoneNumber());
            trackingInfo.put("currentLocation", Map.of(
                "latitude", courier.getCurrentLatitude(),
                "longitude", courier.getCurrentLongitude(),
                "zone", courier.getCurrentZone()
            ));
        }

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
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        // Validate courier
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to update this package");
        }

        deliveryPackage.setStatus(status);
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);

        // Send WebSocket notification to customer
        webSocketService.sendPackageUpdate(deliveryPackage.getCustomer().getUsername(), updatedPackage);

        // If delivered, send rating prompt
        if (status == DeliveryPackage.DeliveryStatus.DELIVERED) {
            webSocketService.sendRatingPrompt(deliveryPackage.getCustomer().getUsername(), packageId);
        }

        return updatedPackage;
    }

    @Transactional
    public DeliveryPackage updateDeliveryLocation(Long packageId, String username, Double latitude, Double longitude, String location) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        // Validate courier
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to update this package");
        }

        deliveryPackage.setCurrentLatitude(latitude);
        deliveryPackage.setCurrentLongitude(longitude);
        deliveryPackage.setCurrentLocation(location);
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);

        // Send WebSocket notifications
        String customerUsername = deliveryPackage.getCustomer().getUsername();
        webSocketService.sendLocationUpdate(customerUsername, packageId, location, latitude, longitude);
        webSocketService.sendPackageUpdate(customerUsername, updatedPackage);

        return updatedPackage;
    }

    @Transactional
    public DeliveryPackage takeDeliveryPackage(Long packageId, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Package is not available for pickup");
        }

        User courier = userService.findByUsername(username);
        if (courier == null || courier.getRole() != User.UserRole.COURIER) {
            throw new IllegalArgumentException("Invalid courier");
        }

        deliveryPackage.setCourier((Courier) courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PICKED_UP);
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);

        // Send WebSocket notification to customer
        webSocketService.sendPackageUpdate(deliveryPackage.getCustomer().getUsername(), updatedPackage);

        return updatedPackage;
    }

    @Transactional
    public DeliveryPackage dropDeliveryPackage(Long packageId, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to drop this package");
        }

        deliveryPackage.setCourier(null);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);

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
            .recipientName(request.getRecipientName())
            .recipientPhone(request.getRecipientPhone())
            .status(DeliveryPackage.DeliveryStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        DeliveryPackage savedPackage = deliveryPackageRepository.save(newPackage);
        webSocketService.notifyNewDeliveryAvailable(savedPackage);
        
        return savedPackage;
    }
}
