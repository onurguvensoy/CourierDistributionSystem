package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByCourier(Courier courier);
    List<Rating> findByCustomer(Customer customer);
    Optional<Rating> findByDeliveryPackageAndCustomer(DeliveryPackage deliveryPackage, Customer customer);
    boolean existsByDeliveryPackageAndCustomer(DeliveryPackage deliveryPackage, Customer customer);
    
    @Query("SELECT AVG(r.courierRating) FROM Rating r WHERE r.courier = :courier")
    Double getAverageCourierRating(Courier courier);
    
    @Query("SELECT AVG(r.deliveryRating) FROM Rating r WHERE r.courier = ?1")
    Double getAverageDeliveryRating(Courier courier);
} 