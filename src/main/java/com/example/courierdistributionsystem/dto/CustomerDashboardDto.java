package com.example.courierdistributionsystem.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDashboardDto {
    private Long customerId;
    private String username;
    private String email;
    private String phoneNumber;
    private List<DeliveryPackageDto> activeDeliveries;
    private List<DeliveryPackageDto> deliveryHistory;
    private Map<String, Object> stats;
} 