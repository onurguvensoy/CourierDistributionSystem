package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.LocationHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RedisLocationHistoryRepository extends CrudRepository<LocationHistory, String> {
    List<LocationHistory> findByCourierUsername(String username);
    List<LocationHistory> findByZone(String zone);
    List<LocationHistory> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<LocationHistory> findByCourierUsernameAndTimestampBetween(String username, LocalDateTime startTime, LocalDateTime endTime);
    void deleteByCourierUsername(String username);
} 