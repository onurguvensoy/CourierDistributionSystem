package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Rating;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByCourier(Courier courier);
    List<Rating> findByCustomer(Customer customer);
    List<Rating> findByDeliveryPackage(DeliveryPackage deliveryPackage);
    
    @Query("SELECT AVG(r.courierRating) FROM Rating r WHERE r.courier = ?1")
    Double getAverageCourierRating(Courier courier);
    
    @Query("SELECT AVG(r.deliveryRating) FROM Rating r WHERE r.courier = ?1")
    Double getAverageDeliveryRating(Courier courier);
} 