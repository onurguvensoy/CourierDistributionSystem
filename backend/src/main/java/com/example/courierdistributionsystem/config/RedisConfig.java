package com.example.courierdistributionsystem.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;

import com.example.courierdistributionsystem.model.*;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.example.courierdistributionsystem.repository.redis")
public class RedisConfig {

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long timeToLive;

    private final ObjectMapper objectMapper;

    public RedisConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        

        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        template.setKeySerializer(new StringRedisSerializer());
        
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setEnableDefaultSerializer(true);
        
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }

    @Bean
    public RedisTemplate<String, DeliveryReport> deliveryReportRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, DeliveryReport> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<DeliveryReport> serializer = new Jackson2JsonRedisSerializer<>(
            objectMapper,
            DeliveryReport.class
        );
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, DeliveryPackage> deliveryPackageRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, DeliveryPackage> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<DeliveryPackage> serializer = new Jackson2JsonRedisSerializer<>(
            objectMapper,
            DeliveryPackage.class
        );
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, LocationHistory> locationHistoryRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, LocationHistory> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<LocationHistory> serializer = new Jackson2JsonRedisSerializer<>(
            objectMapper,
            LocationHistory.class
        );
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, User> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(
            objectMapper,
            User.class
        );
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, List<DeliveryReport>> deliveryReportListRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<DeliveryReport>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, DeliveryReport.class);
        Jackson2JsonRedisSerializer<List<DeliveryReport>> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, listType);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }
} 