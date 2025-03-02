package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.DeliveryHistoryDto;
import java.time.LocalDateTime;
import java.util.List;

public interface IDeliveryHistoryService {
    List<DeliveryHistoryDto> getCourierDeliveryHistory(String username);
    List<DeliveryHistoryDto> getCourierDeliveryHistoryByDateRange(String username, LocalDateTime startDate, LocalDateTime endDate);
    DeliveryHistoryDto createDeliveryHistory(String username, Long packageId);
    void updateDeliveryHistory(Long historyId, DeliveryHistoryDto updates);
    void deleteDeliveryHistory(Long historyId);
} 