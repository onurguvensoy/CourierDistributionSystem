package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Rating;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private DeliveryPackageService deliveryPackageService;


    @PostMapping("/delivery/{packageId}")
    public ResponseEntity<?> rateDelivery(
            @PathVariable Long packageId,
            @RequestBody Map<String, Object> ratingRequest,
            @RequestParam String username) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate rating value
            if (!ratingRequest.containsKey("rating")) {
                response.put("status", "error");
                response.put("message", "Rating is required");
                return ResponseEntity.badRequest().body(response);
            }

            Double rating = ((Number) ratingRequest.get("rating")).doubleValue();
            if (rating < 1 || rating > 5) {
                response.put("status", "error");
                response.put("message", "Rating must be between 1 and 5 stars");
                return ResponseEntity.badRequest().body(response);
            }

            Rating savedRating = deliveryPackageService.rateDelivery(packageId, username, ratingRequest);
            
            response.put("status", "success");
            response.put("message", "Delivery rated successfully");
            response.put("data", savedRating);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @GetMapping("/delivery/{packageId}")
    public ResponseEntity<?> getDeliveryRating(
            @PathVariable Long packageId,
            @RequestParam String username) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Rating rating = deliveryPackageService.getDeliveryRating(packageId, username);
            
            if (rating == null) {
                response.put("status", "success");
                response.put("rated", false);
                response.put("showRatingPopup", true);
            } else {
                response.put("status", "success");
                response.put("rated", true);
                response.put("data", rating);
                response.put("showRatingPopup", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
