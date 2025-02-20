package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.CreatePackageDto;
import com.example.courierdistributionsystem.dto.DeliveryPackageDto;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;
import com.example.courierdistributionsystem.mapper.DeliveryPackageMapper;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.repository.jpa.DeliveryPackageRepository;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.service.IDeliveryPackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryPackageServiceImpl implements IDeliveryPackageService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryPackageServiceImpl.class);

    private final DeliveryPackageRepository deliveryPackageRepository;
    private final DeliveryPackageMapper deliveryPackageMapper;
    private final CourierRepository courierRepository;

    @Autowired
    public DeliveryPackageServiceImpl(DeliveryPackageRepository deliveryPackageRepository,
                                    DeliveryPackageMapper deliveryPackageMapper,
                                    CourierRepository courierRepository) {
        this.deliveryPackageRepository = deliveryPackageRepository;
        this.deliveryPackageMapper = deliveryPackageMapper;
        this.courierRepository = courierRepository;
    }

    @Override
    public DeliveryPackageDto createDeliveryPackage(CreatePackageDto packageDto) {
        logger.debug("Creating new delivery package");
        DeliveryPackage deliveryPackage = new DeliveryPackage();
        deliveryPackage.setDescription(packageDto.getDescription());
        deliveryPackage.setWeight(packageDto.getWeight());
        deliveryPackage.setDeliveryAddress(packageDto.getDeliveryAddress());
        deliveryPackage.setPickupAddress(packageDto.getPickupAddress());
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.PENDING);
        
        DeliveryPackage savedPackage = deliveryPackageRepository.save(deliveryPackage);
        return deliveryPackageMapper.toDto(savedPackage);
    }

    @Override
    public Optional<DeliveryPackageDto> getDeliveryPackageById(Long id) {
        logger.debug("Fetching delivery package with ID: {}", id);
        return deliveryPackageRepository.findById(id)
                .map(deliveryPackageMapper::toDto);
    }

    @Override
    public List<DeliveryPackageDto> getAllDeliveryPackages() {
        logger.debug("Fetching all delivery packages");
        return deliveryPackageRepository.findAll().stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryPackageDto updateDeliveryPackage(Long id, Map<String, String> updates) {
        logger.debug("Updating delivery package with ID: {}", id);
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery package not found with id: " + id));

        if (updates.containsKey("description")) {
            deliveryPackage.setDescription(updates.get("description"));
        }
        if (updates.containsKey("deliveryAddress")) {
            deliveryPackage.setDeliveryAddress(updates.get("deliveryAddress"));
        }
        if (updates.containsKey("status")) {
            deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.valueOf(updates.get("status")));
        }

        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        return deliveryPackageMapper.toDto(updatedPackage);
    }

    @Override
    public void deleteDeliveryPackage(Long id) {
        logger.debug("Deleting delivery package with ID: {}", id);
        if (!deliveryPackageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Delivery package not found with id: " + id);
        }
        deliveryPackageRepository.deleteById(id);
    }

    @Override
    public List<DeliveryPackageDto> getCustomerActiveDeliveryPackages(String username) {
        logger.debug("Fetching active delivery packages for customer: {}", username);
        return deliveryPackageRepository.findByCustomer_UsernameAndStatusNot(username, DeliveryPackage.DeliveryStatus.DELIVERED)
                .stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryPackageDto> getCustomerDeliveryHistory(String username) {
        logger.debug("Fetching delivery history for customer: {}", username);
        return deliveryPackageRepository.findByCustomer_Username(username)
                .stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryPackageDto trackDeliveryPackage(Long packageId, String username) {
        logger.debug("Tracking package {} for customer: {}", packageId, username);
        return deliveryPackageRepository.findByIdAndCustomer_Username(packageId, username)
                .map(deliveryPackageMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
    }

    @Override
    public void cancelDeliveryPackage(Long packageId, String username) {
        logger.debug("Cancelling package {} for customer: {}", packageId, username);
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findByIdAndCustomer_Username(packageId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        
        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel package that is not in PENDING state");
        }

        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.CANCELLED);
        deliveryPackageRepository.save(deliveryPackage);
    }

    @Override
    public List<DeliveryPackageDto> searchDeliveryPackages(String query) {
        logger.debug("Searching delivery packages with query: {}", query);
        return deliveryPackageRepository.findByDescriptionContainingIgnoreCase(query)
                .stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryPackageDto> getAvailableDeliveryPackages() {
        logger.debug("Fetching available delivery packages");
        return deliveryPackageRepository.findByStatus(DeliveryPackage.DeliveryStatus.PENDING)
                .stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryPackageDto> getCourierActiveDeliveryPackages(String username) {
        logger.debug("Fetching active delivery packages for courier: {}", username);
        return deliveryPackageRepository.findByCourier_UsernameAndStatusIn(username, 
                List.of(DeliveryPackage.DeliveryStatus.IN_PROGRESS))
                .stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryPackageDto> getCustomerDeliveryPackages(String username) {
        logger.debug("Fetching all delivery packages for customer: {}", username);
        return deliveryPackageRepository.findByCustomer_Username(username)
                .stream()
                .map(deliveryPackageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryPackageDto takeDeliveryPackage(Long packageId, String username) {
        logger.debug("Courier {} taking delivery package with ID: {}", username, packageId);
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery package not found with id: " + packageId));

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Package is not available for pickup");
        }

        deliveryPackage.setCourier(courierRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Courier not found")));
        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.IN_PROGRESS);
        DeliveryPackage updatedPackage = deliveryPackageRepository.save(deliveryPackage);
        return deliveryPackageMapper.toDto(updatedPackage);
    }

    @Override
    public DeliveryPackageDto dropDeliveryPackage(Long packageId, String username) {
        logger.debug("Dropping package {} by courier: {}", packageId, username);
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findByIdAndCourier_Username(packageId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found or not assigned to courier"));

        if (deliveryPackage.getStatus() != DeliveryPackage.DeliveryStatus.IN_PROGRESS) {
            throw new IllegalStateException("Package must be in IN_PROGRESS state to be dropped");
        }

        deliveryPackage.setStatus(DeliveryPackage.DeliveryStatus.DELIVERED);
        deliveryPackage = deliveryPackageRepository.save(deliveryPackage);
        return deliveryPackageMapper.toDto(deliveryPackage);
    }

    @Override
    public DeliveryPackageDto updateDeliveryStatus(Long packageId, String username, DeliveryPackage.DeliveryStatus status) {
        logger.debug("Updating status of package {} to {} by courier: {}", packageId, status, username);
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findByIdAndCourier_Username(packageId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found or not assigned to courier"));

        deliveryPackage.setStatus(status);
        deliveryPackage = deliveryPackageRepository.save(deliveryPackage);
        return deliveryPackageMapper.toDto(deliveryPackage);
    }
} 