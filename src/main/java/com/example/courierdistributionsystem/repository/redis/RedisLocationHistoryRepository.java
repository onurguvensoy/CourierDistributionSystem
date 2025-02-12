package com.example.courierdistributionsystem.repository.redis;

import com.example.courierdistributionsystem.model.LocationHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedisLocationHistoryRepository extends CrudRepository<LocationHistory, String> {
} 