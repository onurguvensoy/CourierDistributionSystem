package com.example.courierdistributionsystem.dto;

import com.example.courierdistributionsystem.model.DeliveryPackage;
import lombok.Data;
import lombok.Builder;

import java.util.List;

@Data
@Builder
public class CustomerDashboardDTO {
    private List<DeliveryPackage> activePackages;
    private List<DeliveryPackage> completedPackages;
    private DashboardStats stats;

    @Data
    @Builder
    public static class DashboardStats {
        private long totalPackages;
        private long activeShipments;
        private long deliveredPackages;
        private long cancelledPackages;
    }
} 