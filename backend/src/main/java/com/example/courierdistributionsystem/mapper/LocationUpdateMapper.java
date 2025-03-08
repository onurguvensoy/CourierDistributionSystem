package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.LocationUpdateDto;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.LocationHistory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LocationUpdateMapper {

    public void updateCourierLocation(Courier courier, LocationUpdateDto dto) {
        if (courier == null || dto == null) {
            return;
        }

        courier.setCurrentLatitude(dto.getLatitude());
        courier.setCurrentLongitude(dto.getLongitude());
        if (dto.getZone() != null) {
            courier.setCurrentZone(dto.getZone());
        }
    }

    public LocationHistory toLocationHistory(Courier courier, LocationUpdateDto dto) {
        if (courier == null || dto == null) {
            return null;
        }

        LocationHistory history = new LocationHistory();
        history.setCourier(courier);
        history.setLatitude(dto.getLatitude());
        history.setLongitude(dto.getLongitude());
        history.setZone(dto.getZone());
        history.setTimestamp(LocalDateTime.now());
        
        return history;
    }

    public LocationUpdateDto toDto(Courier courier) {
        if (courier == null) {
            return null;
        }

        LocationUpdateDto dto = new LocationUpdateDto();
        dto.setLatitude(courier.getCurrentLatitude());
        dto.setLongitude(courier.getCurrentLongitude());
        dto.setZone(courier.getCurrentZone());
        
        return dto;
    }
} 