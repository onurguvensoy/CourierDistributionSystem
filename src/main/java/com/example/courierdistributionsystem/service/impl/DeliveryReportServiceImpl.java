package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.DeliveryReportDto;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.repository.jpa.DeliveryReportRepository;
import com.example.courierdistributionsystem.repository.jpa.DeliveryPackageRepository;
import com.example.courierdistributionsystem.service.IDeliveryReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class DeliveryReportServiceImpl implements IDeliveryReportService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportServiceImpl.class);

    @Autowired
    private DeliveryReportRepository deliveryReportRepository;

    @Autowired
    private DeliveryPackageRepository packageRepository;


    @Override
    public List<DeliveryReport> getAllReports() {
        logger.info("Fetching all delivery reports");
        return deliveryReportRepository.findAll();
    }

    @Override
    public Optional<DeliveryReport> getReportById(Long id) {
        logger.info("Fetching delivery report with ID: {}", id);
        return deliveryReportRepository.findById(id);
    }

    @Override
    @Transactional
    public DeliveryReport generateReport(Long packageId, String username) {
        logger.info("Generating report for package ID: {} by user: {}", packageId, username);
        
        DeliveryPackage deliveryPackage = packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + packageId));

        DeliveryReport report = new DeliveryReport();
        report.setDeliveryPackage(deliveryPackage);
        report.setCourier(deliveryPackage.getCourier());
        report.setCustomer(deliveryPackage.getCustomer());
        report.setDeliveryTime(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());

        return deliveryReportRepository.save(report);
    }

    @Override
    public List<DeliveryReport> getReportsByCourier(String username) {
        logger.info("Fetching delivery reports for courier: {}", username);
        return deliveryReportRepository.findByCourier_Username(username);
    }

    @Override
    public List<DeliveryReport> getReportsByPackage(Long packageId) {
        logger.info("Fetching delivery reports for package ID: {}", packageId);
        return deliveryReportRepository.findByDeliveryPackage(packageRepository.findById(packageId)
            .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + packageId)));
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        logger.info("Deleting delivery report with ID: {}", id);
        if (!deliveryReportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report not found with id: " + id);
        }
        deliveryReportRepository.deleteById(id);
    }

    @Override
    @Transactional
    public DeliveryReport updateReport(Long reportId, DeliveryReportDto reportDto) {
        logger.info("Updating delivery report with ID: {}", reportId);
        
        DeliveryReport report = deliveryReportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.setDeliveryTime(reportDto.getDeliveryTime());
        report.setDeliveryNotes(reportDto.getDeliveryNotes());
        report.setCustomerSignature(reportDto.getCustomerSignature());
        report.setDeliveryProofPhoto(reportDto.getDeliveryProofPhoto());
        report.setAdditionalDetails(reportDto.getAdditionalDetails());

        return deliveryReportRepository.save(report);
    }

    @Override
    public List<DeliveryReport> getReportsByDateRange(String startDate, String endDate) {
        logger.info("Fetching delivery reports between {} and {}", startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        return deliveryReportRepository.findByDeliveryTimeBetween(start, end);
    }

    @Override
    public List<DeliveryReport> getReportsByStatus(String status) {
        logger.info("Fetching delivery reports with status: {}", status);
        return deliveryReportRepository.findByDeliveryPackage_Status(DeliveryPackage.DeliveryStatus.valueOf(status));
    }

    @Override
    public List<DeliveryReport> getDeliveryReportsByPackageId(Long packageId) {
        return getReportsByPackage(packageId);
    }

    @Override
    public List<DeliveryReport> getDeliveryReportsByCourierId(Long courierId) {
        logger.info("Fetching delivery reports for courier ID: {}", courierId);
        return deliveryReportRepository.findByCourier_Id(courierId);
    }

    @Override
    public List<DeliveryReport> getDeliveryReportsByCustomerId(Long customerId) {
        logger.info("Fetching delivery reports for customer ID: {}", customerId);
        return deliveryReportRepository.findByCustomer_Id(customerId);
    }

    @Override
    @Transactional
    public DeliveryReport finalizeDeliveryReport(Long reportId) {
        logger.info("Finalizing delivery report with ID: {}", reportId);
        
        DeliveryReport report = deliveryReportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.setCompletionTime(LocalDateTime.now());
        return deliveryReportRepository.save(report);
    }

    @Override
    @Transactional
    public void deleteDeliveryReport(Long reportId) {
        deleteReport(reportId);
    }
} 