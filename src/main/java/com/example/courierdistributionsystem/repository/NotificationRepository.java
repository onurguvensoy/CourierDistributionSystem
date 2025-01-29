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
    Page<Notification> findByCustomerOrderByCreatedAtDesc(Customer customer, Pageable pageable);
    Page<Notification> findByCourierOrderByCreatedAtDesc(Courier courier, Pageable pageable);
    
    List<Notification> findByCustomerAndIsReadFalseOrderByCreatedAtDesc(Customer customer);
    List<Notification> findByCourierAndIsReadFalseOrderByCreatedAtDesc(Courier courier);
} 