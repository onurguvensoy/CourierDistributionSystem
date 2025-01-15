package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Rating;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.RatingRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }

    @GetMapping("/courier/{courierId}")
    public List<Rating> getCourierRatings(@PathVariable Long courierId) {
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));
        return ratingRepository.findByCourier(courier);
    }

    @GetMapping("/my-ratings")
    public List<Rating> getMyRatings(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ratingRepository.findByCustomer(user);
    }

    @PostMapping("/courier/{courierId}")
    public ResponseEntity<Rating> createRating(
            @PathVariable Long courierId,
            @Valid @RequestBody Rating rating,
            @RequestParam String username) {
        
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User courier = userRepository.findById(courierId)
                .orElseThrow(() -> new RuntimeException("Courier not found"));

        rating.setCustomer(customer);
        rating.setCourier(courier);

        Rating savedRating = ratingRepository.save(rating);
        
        // Update courier's average rating
        Double averageRating = ratingRepository.getAverageCourierRating(courier);
        courier.setAverageRating(averageRating != null ? averageRating : 0.0);
        userRepository.save(courier);

        return ResponseEntity.ok(savedRating);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRating(@PathVariable Long id) {
        Rating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        ratingRepository.delete(rating);
        return ResponseEntity.ok().build();
    }
} 