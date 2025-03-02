package com.example.courierdistributionsystem.repository.jpa;

import com.example.courierdistributionsystem.model.DeliveryHistory;
import com.example.courierdistributionsystem.model.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface DeliveryHistoryRepository extends JpaRepository<DeliveryHistory, Long> {
    List<DeliveryHistory> findByCourierOrderByCreatedAtDesc(Courier courier);
    
    @Query("SELECT dh FROM DeliveryHistory dh WHERE dh.courier.username = :username ORDER BY dh.createdAt DESC")
    List<DeliveryHistory> findByCourierUsername(@Param("username") String username);
    
    @Query("SELECT dh FROM DeliveryHistory dh WHERE dh.courier.username = :username AND dh.createdAt BETWEEN :startDate AND :endDate ORDER BY dh.createdAt DESC")
    List<DeliveryHistory> findByCourierUsernameAndDateRange(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
} 