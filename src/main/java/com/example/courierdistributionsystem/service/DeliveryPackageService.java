package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.CreatePackageRequest;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.repository.jpa.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.jpa.CustomerRepository;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.repository.jpa.DeliveryReportRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class DeliveryPackageService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryPackageService.class);
    private static final String PACKAGES_CACHE = "packages";
    private static final String REPORTS_CACHE = "reports";
    private static final String LOCATIONS_CACHE = "locations";

    private final DeliveryPackageRepository deliveryPackageRepository;
    private final CustomerRepository customerRepository;
    private final CourierRepository courierRepository;
    private final DeliveryReportRepository deliveryReportRepository;
 


    public DeliveryPackageService(
            DeliveryPackageRepository deliveryPackageRepository,
            CustomerRepository customerRepository,
            CourierRepository courierRepository,
            DeliveryReportRepository deliveryReportRepository
            ) {
        this.deliveryPackageRepository = deliveryPackageRepository;
        this.customerRepository = customerRepository;
        this.courierRepository = courierRepository;
        this.deliveryReportRepository = deliveryReportRepository;
    }

    @Cacheable(value = PACKAGES_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public DeliveryPackage getDeliveryPackageById(Long id) {
        logger.debug("Fetching delivery package with id: {}", id);
        return deliveryPackageRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Delivery package not found with id: {}", id);
                return new IllegalArgumentException("Delivery package not found with id: " + id);
            });
    }

    @Cacheable(value = PACKAGES_CACHE, key = "'all'")
    public List<DeliveryPackage> getAllDeliveryPackages() {
        logger.debug("Fetching all delivery packages");
        return deliveryPackageRepository.findAll();
    }

    @Cacheable(value = PACKAGES_CACHE, key = "'all'")
    public Page<DeliveryPackage> getDeliveryPackagesPage(Pageable pageable) {
        return deliveryPackageRepository.findAll(pageable);
    }

    @Cacheable(value = PACKAGES_CACHE, key = "'customer_' + #username")
    public List<DeliveryPackage> getDeliveryPackagesByCustomerUsername(String username) {
        return deliveryPackageRepository.findByCustomer_Username(username);
    }

    @Cacheable(value = PACKAGES_CACHE, key = "'courier_' + #username")
    public List<DeliveryPackage> getDeliveryPackagesByCourierUsername(String username) {
        return deliveryPackageRepository.findByCourier_Username(username);
    }

    @CacheEvict(value = PACKAGES_CACHE, allEntries = true)
    @Transactional
    public DeliveryPackage createDeliveryPackage(DeliveryPackage deliveryPackage) {
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        deliveryPackage.setCreatedAt(LocalDateTime.now());
        deliveryPackage.setUpdatedAt(LocalDateTime.now());
        return deliveryPackageRepository.save(deliveryPackage);
    }

    @CacheEvict(value = PACKAGES_CACHE, key = "#id")
    @Transactional
    public void deleteDeliveryPackage(Long id) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(id);
        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Cannot delete a package that is not in PENDING status");
        }
        deliveryPackageRepository.deleteById(id);
    }

    @Caching(evict = {
        @CacheEvict(value = PACKAGES_CACHE, key = "#packageId"),
        @CacheEvict(value = LOCATIONS_CACHE, key = "#packageId")
    })
    @Transactional
    public DeliveryPackage updateLocation(Long packageId, String username, String location, double latitude, double longitude) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        validateCourierForPackage(deliveryPackage, username);

        deliveryPackage.setCurrentLocation(location);
        deliveryPackage.setLatitude(latitude);
        deliveryPackage.setLongitude(longitude);
        deliveryPackage.setUpdatedAt(LocalDateTime.now());

        return deliveryPackageRepository.save(deliveryPackage);
    }

    @CachePut(value = PACKAGES_CACHE, key = "#id")
    @Transactional
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

        return deliveryPackageRepository.save(existingDelivery);
    }

    @Caching(evict = {
        @CacheEvict(value = PACKAGES_CACHE, key = "#id"),
        @CacheEvict(value = PACKAGES_CACHE, key = "'customer_' + #username"),
        @CacheEvict(value = PACKAGES_CACHE, key = "'all'")
    })
    @Transactional
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

       deliveryPackageRepository.save(deliveryPackage);
    
    }

    @Cacheable(value = PACKAGES_CACHE, key = "'customer_' + #username")
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

    @Cacheable(value = PACKAGES_CACHE, key = "'available'")
    @Transactional(readOnly = true)
    public List<DeliveryPackage> getAvailableDeliveryPackages() {
        return deliveryPackageRepository.findByStatus(DeliveryPackage.DeliveryStatus.PENDING);
    }

    @Cacheable(value = PACKAGES_CACHE, key = "'courier_active_' + #username")
    @Transactional(readOnly = true)
    public List<DeliveryPackage> getCourierActiveDeliveryPackages(String username) {
        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        return deliveryPackageRepository.findByCourierAndStatusIn(courier, 
            List.of(DeliveryPackage.DeliveryStatus.ASSIGNED, 
                   DeliveryPackage.DeliveryStatus.PICKED_UP, 
                   DeliveryPackage.DeliveryStatus.IN_TRANSIT));
    }

    @Caching(evict = {
        @CacheEvict(value = PACKAGES_CACHE, key = "#packageId"),
        @CacheEvict(value = PACKAGES_CACHE, key = "'available'"),
        @CacheEvict(value = PACKAGES_CACHE, key = "'courier_active_' + #username")
    })
    @Transactional
    public DeliveryPackage updateDeliveryStatus(Long packageId, String username, DeliveryPackage.DeliveryStatus status) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        validateCourierForPackage(deliveryPackage, username);

        deliveryPackage.setStatus(status);
        
        if (status == DeliveryPackage.DeliveryStatus.DELIVERED) {
            deliveryPackage.setDeliveredAt(LocalDateTime.now());
            Courier courier = deliveryPackage.getCourier();
            courier.setAvailable(true);
            courierRepository.save(courier);
        }

        return deliveryPackageRepository.save(deliveryPackage);
    }

    private void validateCourierForPackage(DeliveryPackage deliveryPackage, String username) {
        if (!deliveryPackage.getCourier().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to update this package");
        }
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

        return updatedPackage;
    }

    @CacheEvict(value = PACKAGES_CACHE, allEntries = true)
    @Transactional
    public DeliveryPackage takeDeliveryPackage(Long packageId, String username) {
        Courier courier = courierRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Courier not found"));

        if (!courier.isAvailable()) {
            throw new IllegalStateException("Courier is not available for deliveries");
        }

        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Package is not available for pickup");
        }

        deliveryPackage.setCourier(courier);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.ASSIGNED);
        courier.setAvailable(false);
        courierRepository.save(courier);

        return deliveryPackageRepository.save(deliveryPackage);
    }

    @CacheEvict(value = PACKAGES_CACHE, allEntries = true)
    @Transactional
    public DeliveryPackage dropDeliveryPackage(Long packageId, String username) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        validateCourierForPackage(deliveryPackage, username);

        Courier courier = deliveryPackage.getCourier();
        deliveryPackage.setCourier(null);
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        courier.setAvailable(true);
        courierRepository.save(courier);

        return deliveryPackageRepository.save(deliveryPackage);
    }

    // Delivery Report Methods
    @Cacheable(value = REPORTS_CACHE, key = "'all'")
    public List<DeliveryReport> getAllDeliveryReports() {
        return deliveryReportRepository.findAll();
    }

    @Cacheable(value = REPORTS_CACHE, key = "'courier_' + #username")
    public List<DeliveryReport> getCourierDeliveryReports(String username) {
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        return deliveryReportRepository.findByCourier(courier);
    }

    @CachePut(value = REPORTS_CACHE, key = "#result.id")
    @CacheEvict(value = REPORTS_CACHE, key = "'all'")
    @Transactional
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

    @Cacheable(value = REPORTS_CACHE, key = "#id")
    public DeliveryReport getDeliveryReportById(Long id) {
        return deliveryReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery report not found"));
    }

    @Cacheable(value = REPORTS_CACHE, key = "'package_' + #packageId")
    public List<DeliveryReport> getDeliveryReportsByPackage(Long packageId) {
        DeliveryPackage deliveryPackage = getDeliveryPackageById(packageId);
        return deliveryReportRepository.findByDeliveryPackage(deliveryPackage);
    }

    @CachePut(value = REPORTS_CACHE, key = "#id")
    @Transactional
    public DeliveryReport updateDeliveryReport(Long id, DeliveryReport reportDetails) {
        DeliveryReport report = getDeliveryReportById(id);
        report.setDeliveryTime(reportDetails.getDeliveryTime());
        report.setDeliveryNotes(reportDetails.getDeliveryNotes());
        report.setCustomerConfirmation(reportDetails.isCustomerConfirmation());
        report.setDeliveryPhotoUrl(reportDetails.getDeliveryPhotoUrl());
        report.setSignatureUrl(reportDetails.getSignatureUrl());
        report.setDistanceTraveled(reportDetails.getDistanceTraveled());
        return deliveryReportRepository.save(report);
    }

    @CacheEvict(value = REPORTS_CACHE, allEntries = true)
    @Transactional
    public void deleteDeliveryReport(Long id) {
        DeliveryReport report = getDeliveryReportById(id);
        deliveryReportRepository.delete(report);
    }

    /**
     * Get active delivery packages for a customer (PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT)
     */
    @Cacheable(value = PACKAGES_CACHE, key = "'customer_active_' + #username")
    public List<DeliveryPackage> getCustomerActiveDeliveryPackages(String username) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        return deliveryPackageRepository.findByCustomerAndStatusIn(customer, 
            List.of(DeliveryPackage.DeliveryStatus.PENDING,
                   DeliveryPackage.DeliveryStatus.ASSIGNED,
                   DeliveryPackage.DeliveryStatus.PICKED_UP,
                   DeliveryPackage.DeliveryStatus.IN_TRANSIT));
    }

   
    @Cacheable(value = PACKAGES_CACHE, key = "'customer_history_' + #username")
    public List<DeliveryPackage> getCustomerDeliveryHistory(String username) {
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        return deliveryPackageRepository.findByCustomerAndStatus(customer, 
            DeliveryPackage.DeliveryStatus.DELIVERED);
    }

    @Transactional
    public DeliveryPackage createPackage(CreatePackageRequest request, Customer customer) {
        if (request.getWeight() <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0");
        }

        // Fetch the customer within the transaction to ensure we have a managed entity
        Customer managedCustomer = customerRepository.findById(customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        DeliveryPackage newPackage = DeliveryPackage.builder()
                .customer(managedCustomer)
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .weight(request.getWeight())
                .description(request.getDescription())
                .specialInstructions(request.getSpecialInstructions())
                .status(DeliveryPackage.DeliveryStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .trackingNumber(generateTrackingNumber())
                .build();

        // Save the package
        DeliveryPackage savedPackage = deliveryPackageRepository.save(newPackage);

        // Initialize the customer's packages collection if needed and add the new package
        if (managedCustomer.getPackages() == null) {
            managedCustomer.getPackages().clear();
        }
        managedCustomer.getPackages().add(savedPackage);
        

        return savedPackage;
    }

    private String generateTrackingNumber() {
        return String.format("CDS-%d-%04d", 
            System.currentTimeMillis() % 1000000000, 
            (int)(Math.random() * 10000));
    }

    @Transactional(readOnly = true)
    public List<DeliveryPackage> getCompletedDeliveriesByCourier(Courier courier) {
        return deliveryPackageRepository.findByCourierAndStatus(courier, DeliveryPackage.DeliveryStatus.DELIVERED);
    }
}
