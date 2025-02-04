package com.example.courierdistributionsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for asynchronous operations.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfig.class);
    
    @Value("${app.async.core-pool-size}")
    private int corePoolSize;
    
    @Value("${app.async.max-pool-size}")
    private int maxPoolSize;
    
    @Value("${app.async.queue-capacity}")
    private int queueCapacity;
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        LOGGER.debug("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
} 