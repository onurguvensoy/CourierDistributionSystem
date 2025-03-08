package com.example.courierdistributionsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateDto {
    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;
    
    @NotNull(message = "Latitude is required")
    @JsonProperty("lat")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @JsonProperty("lng")
    private Double longitude;
    
    private String zone;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
} 