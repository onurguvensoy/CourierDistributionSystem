package com.example.courierdistributionsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CourierDistributionSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourierDistributionSystemApplication.class, args);
    }
} 