package com.example.courierdistributionsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class MapService {
    
    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public Map<String, Object> createMapConfig(Double latitude, Double longitude, String address) {
        Map<String, Object> mapConfig = new HashMap<>();
        mapConfig.put("apiKey", googleMapsApiKey);
        
        // Default to a central location if no coordinates provided
        mapConfig.put("latitude", latitude != null ? latitude : 0.0);
        mapConfig.put("longitude", longitude != null ? longitude : 0.0);
        mapConfig.put("address", address);
        mapConfig.put("zoom", 15);
        
        return mapConfig;
    }

    public Map<String, Object> createDeliveryRouteConfig(String pickupAddress, String deliveryAddress, 
                                                        Double currentLat, Double currentLng) {
        Map<String, Object> mapConfig = new HashMap<>();
        mapConfig.put("apiKey", googleMapsApiKey);
        mapConfig.put("pickupAddress", pickupAddress);
        mapConfig.put("deliveryAddress", deliveryAddress);
        
        if (currentLat != null && currentLng != null) {
            mapConfig.put("currentLatitude", currentLat);
            mapConfig.put("currentLongitude", currentLng);
        }
        
        mapConfig.put("zoom", 12);
        return mapConfig;
    }
} 