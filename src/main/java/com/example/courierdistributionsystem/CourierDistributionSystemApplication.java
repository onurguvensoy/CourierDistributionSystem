package com.example.courierdistributionsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CourierDistributionSystemApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CourierDistributionSystemApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CourierDistributionSystemApplication.class, args);
    }
} 