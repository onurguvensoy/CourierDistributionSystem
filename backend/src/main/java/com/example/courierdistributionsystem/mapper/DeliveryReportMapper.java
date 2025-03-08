package com.example.courierdistributionsystem.mapper;

import com.example.courierdistributionsystem.dto.DeliveryReportDto;
import com.example.courierdistributionsystem.model.DeliveryReport;
import org.springframework.stereotype.Component;

@Component
public class DeliveryReportMapper {

    public DeliveryReportDto toDto(DeliveryReport report) {
        if (report == null) {
            return null;
        }

        DeliveryReportDto dto = new DeliveryReportDto();
        dto.setId(report.getId());
        dto.setTrackingNumber(report.getDeliveryPackage() != null ? report.getDeliveryPackage().getTrackingNumber() : null);
        dto.setCourierUsername(report.getCourier() != null ? report.getCourier().getUsername() : null);
        dto.setCustomerUsername(report.getCustomer() != null ? report.getCustomer().getUsername() : null);
        dto.setDeliveryTime(report.getDeliveryTime());
        dto.setDeliveryNotes(report.getDeliveryNotes());
        dto.setCustomerSignature(report.getCustomerSignature());
        dto.setDeliveryProofPhoto(report.getDeliveryProofPhoto());
        dto.setAdditionalDetails(report.getAdditionalDetails());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }

    public DeliveryReport toEntity(DeliveryReportDto dto) {
        if (dto == null) {
            return null;
        }

        DeliveryReport report = new DeliveryReport();
        report.setId(dto.getId());
        report.setDeliveryTime(dto.getDeliveryTime());
        report.setDeliveryNotes(dto.getDeliveryNotes());
        report.setCustomerSignature(dto.getCustomerSignature());
        report.setDeliveryProofPhoto(dto.getDeliveryProofPhoto());
        report.setAdditionalDetails(dto.getAdditionalDetails());
        report.setCreatedAt(dto.getCreatedAt());
        return report;
    }
} 