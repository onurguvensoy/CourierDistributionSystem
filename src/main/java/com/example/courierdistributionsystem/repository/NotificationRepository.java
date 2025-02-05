package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Notification;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Customer notifications
    List<Notification> findByCustomerOrderByTimestampDesc(Customer customer);
    List<Notification> findByCustomerAndReadFalseOrderByTimestampDesc(Customer customer);
    Page<Notification> findByCustomerOrderByTimestampDesc(Customer customer, Pageable pageable);

    // Courier notifications
    List<Notification> findByCourierOrderByTimestampDesc(Courier courier);
    List<Notification> findByCourierAndReadFalseOrderByTimestampDesc(Courier courier);
    Page<Notification> findByCourierOrderByTimestampDesc(Courier courier, Pageable pageable);

    // Combined queries
    List<Notification> findByCustomerOrCourierOrderByTimestampDesc(Customer customer, Courier courier);
    List<Notification> findByCustomerOrCourierAndReadFalseOrderByTimestampDesc(Customer customer, Courier courier);
} 