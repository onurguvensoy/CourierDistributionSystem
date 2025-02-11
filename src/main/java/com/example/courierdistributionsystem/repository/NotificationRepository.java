package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.Notification;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // User notifications
    List<Notification> findByUserOrderByTimestampDesc(User user);
    List<Notification> findByUserAndReadFalseOrderByTimestampDesc(User user);
    Page<Notification> findByUserOrderByTimestampDesc(User user, Pageable pageable);
} 