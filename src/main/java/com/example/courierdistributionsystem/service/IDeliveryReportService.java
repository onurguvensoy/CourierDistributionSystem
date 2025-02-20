package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.DeliveryReportDto;
import com.example.courierdistributionsystem.model.DeliveryReport;
import java.util.List;
import java.util.Optional;

public interface IDeliveryReportService {
    List<DeliveryReport> getAllReports();
    Optional<DeliveryReport> getReportById(Long id);
    DeliveryReport generateReport(Long packageId, String username);
    List<DeliveryReport> getReportsByCourier(String username);
    List<DeliveryReport> getReportsByPackage(Long packageId);
    void deleteReport(Long id);
    DeliveryReport updateReport(Long reportId, DeliveryReportDto reportDto);
    List<DeliveryReport> getReportsByDateRange(String startDate, String endDate);
    List<DeliveryReport> getReportsByStatus(String status);
    List<DeliveryReport> getDeliveryReportsByPackageId(Long packageId);
    List<DeliveryReport> getDeliveryReportsByCourierId(Long courierId);
    List<DeliveryReport> getDeliveryReportsByCustomerId(Long customerId);
    DeliveryReport finalizeDeliveryReport(Long reportId);
    void deleteDeliveryReport(Long reportId);
} 