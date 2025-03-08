package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.CourierDto;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.stereotype.Component;

@Component
public class CourierMapper {

    public CourierDto toDto(Courier courier) {
        if (courier == null) {
            return null;
        }

        CourierDto dto = new CourierDto();
        dto.setId(courier.getId());
        dto.setUsername(courier.getUsername());
        dto.setEmail(courier.getEmail());
        dto.setPhoneNumber(courier.getPhoneNumber());
        dto.setVehicleType(courier.getVehicleType());
        dto.setAvailable(courier.isAvailable());
        dto.setCurrentZone(courier.getCurrentZone());
        dto.setCurrentLatitude(courier.getCurrentLatitude());
        dto.setCurrentLongitude(courier.getCurrentLongitude());
        dto.setRole(courier.getRole());
        return dto;
    }

    public Courier toEntity(CourierDto dto) {
        if (dto == null) {
            return null;
        }

        Courier courier = new Courier();
        courier.setId(dto.getId());
        courier.setUsername(dto.getUsername());
        courier.setEmail(dto.getEmail());
        courier.setPhoneNumber(dto.getPhoneNumber());
        courier.setVehicleType(dto.getVehicleType());
        courier.setAvailable(dto.isAvailable());
        courier.setCurrentZone(dto.getCurrentZone());
        courier.setCurrentLatitude(dto.getCurrentLatitude());
        courier.setCurrentLongitude(dto.getCurrentLongitude());
        courier.setRole(dto.getRole());
        return courier;
    }
} 