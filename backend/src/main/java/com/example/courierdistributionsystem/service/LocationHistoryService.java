package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.model.LocationHistory;
import java.util.List;

public interface LocationHistoryService {
    LocationHistory saveLocationUpdate(LocationUpdateDto locationUpdate, String courierUsername);
    List<LocationHistory> getLocationHistory(String trackingNumber);
} 