package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.DeliveryHistoryDto;
import com.example.courierdistributionsystem.model.DeliveryHistory;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.repository.jpa.DeliveryHistoryRepository;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.repository.jpa.DeliveryPackageRepository;
import com.example.courierdistributionsystem.service.IDeliveryHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveryHistoryServiceImpl implements IDeliveryHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryHistoryServiceImpl.class);

    private final DeliveryHistoryRepository deliveryHistoryRepository;
    private final CourierRepository courierRepository;
    private final DeliveryPackageRepository deliveryPackageRepository;

    @Autowired
    public DeliveryHistoryServiceImpl(
            DeliveryHistoryRepository deliveryHistoryRepository,
            CourierRepository courierRepository,
            DeliveryPackageRepository deliveryPackageRepository) {
        this.deliveryHistoryRepository = deliveryHistoryRepository;
        this.courierRepository = courierRepository;
        this.deliveryPackageRepository = deliveryPackageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryHistoryDto> getCourierDeliveryHistory(String username) {
        logger.debug("Fetching delivery history for courier: {}", username);
        List<DeliveryHistory> histories = deliveryHistoryRepository.findByCourierUsername(username);
        return histories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryHistoryDto> getCourierDeliveryHistoryByDateRange(
            String username, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching delivery history for courier: {} between {} and {}", 
                username, startDate, endDate);
        List<DeliveryHistory> histories = deliveryHistoryRepository
                .findByCourierUsernameAndDateRange(username, startDate, endDate);
        return histories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryHistoryDto createDeliveryHistory(String username, Long packageId) {
        logger.debug("Creating delivery history for courier: {} and package: {}", username, packageId);
        
        Courier courier = courierRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        
        DeliveryPackage deliveryPackage = deliveryPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        DeliveryHistory history = DeliveryHistory.builder()
                .courier(courier)
                .deliveryPackage(deliveryPackage)
                .status(deliveryPackage.getStatus())
                .pickupLocation(deliveryPackage.getPickupAddress())
                .deliveryLocation(deliveryPackage.getDeliveryAddress())
                .build();

        return convertToDto(deliveryHistoryRepository.save(history));
    }

    @Override
    @Transactional
    public void updateDeliveryHistory(Long historyId, DeliveryHistoryDto updates) {
        logger.debug("Updating delivery history: {}", historyId);
        
        DeliveryHistory history = deliveryHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("Delivery history not found"));

        if (updates.getStatus() != null) {
            history.setStatus(updates.getStatus());
            switch (updates.getStatus()) {
                case IN_PROGRESS -> history.setPickedUpAt(LocalDateTime.now());
                case DELIVERED -> history.setCompletedAt(LocalDateTime.now());
                case CANCELLED -> history.setCancelledAt(LocalDateTime.now());
            }
        }

        if (updates.getDeliveryNotes() != null) {
            history.setDeliveryNotes(updates.getDeliveryNotes());
        }
        if (updates.getCustomerFeedback() != null) {
            history.setCustomerFeedback(updates.getCustomerFeedback());
        }
        if (updates.getDeliveryRating() != null) {
            history.setDeliveryRating(updates.getDeliveryRating());
        }

        deliveryHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void deleteDeliveryHistory(Long historyId) {
        logger.debug("Deleting delivery history: {}", historyId);
        deliveryHistoryRepository.deleteById(historyId);
    }

    private DeliveryHistoryDto convertToDto(DeliveryHistory history) {
        Map<String, Object> packageDetails = new HashMap<>();
        packageDetails.put("trackingNumber", history.getDeliveryPackage().getTrackingNumber());
        packageDetails.put("weight", history.getDeliveryPackage().getWeight());
        packageDetails.put("description", history.getDeliveryPackage().getDescription());
        packageDetails.put("specialInstructions", history.getDeliveryPackage().getSpecialInstructions());

        Map<String, Object> customerDetails = new HashMap<>();
        if (history.getDeliveryPackage().getCustomer() != null) {
            customerDetails.put("name", history.getDeliveryPackage().getCustomer().getUsername());
            customerDetails.put("email", history.getDeliveryPackage().getCustomer().getEmail());
        }

        return DeliveryHistoryDto.builder()
                .id(history.getId())
                .courierId(history.getCourier().getId())
                .courierName(history.getCourier().getUsername())
                .packageId(history.getDeliveryPackage().getId())
                .trackingNumber(history.getDeliveryPackage().getTrackingNumber())
                .createdAt(history.getCreatedAt())
                .pickedUpAt(history.getPickedUpAt())
                .completedAt(history.getCompletedAt())
                .cancelledAt(history.getCancelledAt())
                .status(history.getStatus())
                .pickupLocation(history.getPickupLocation())
                .deliveryLocation(history.getDeliveryLocation())
                .deliveryNotes(history.getDeliveryNotes())
                .customerFeedback(history.getCustomerFeedback())
                .deliveryRating(history.getDeliveryRating())
                .packageDetails(packageDetails)
                .customerDetails(customerDetails)
                .build();
    }
} 