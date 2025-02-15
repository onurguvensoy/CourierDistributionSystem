package com.example.courierdistributionsystem.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import com.example.courierdistributionsystem.model.*;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.example.courierdistributionsystem.repository.redis")
public class RedisConfig {

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long timeToLive;

    private ObjectMapper configureObjectMapper(ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean
    @Primary
    public ObjectMapper redisObjectMapper() {
        return configureObjectMapper(new ObjectMapper());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Create Jackson2JsonRedisSerializer with configured ObjectMapper
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(
            configureObjectMapper(new ObjectMapper()),
            Object.class
        );

        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper mapper = configureObjectMapper(new ObjectMapper());
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(
            mapper,
            Object.class
        );

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
            configureObjectMapper(new ObjectMapper()),
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
            configureObjectMapper(new ObjectMapper()),
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
            configureObjectMapper(new ObjectMapper()),
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
            configureObjectMapper(new ObjectMapper()),
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
        
        ObjectMapper mapper = configureObjectMapper(new ObjectMapper());
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, DeliveryReport.class);
        Jackson2JsonRedisSerializer<List<DeliveryReport>> serializer = new Jackson2JsonRedisSerializer<>(mapper, listType);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }
} 