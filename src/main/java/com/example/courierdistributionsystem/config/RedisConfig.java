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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Duration;
import java.util.List;

import com.example.courierdistributionsystem.model.*;


@Configuration
@EnableCaching
@EnableRedisRepositories(basePackages = "com.example.courierdistributionsystem.repository.redis")
public class RedisConfig {

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long timeToLive;

    @Bean
    @Primary
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMillis(timeToLive))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withCacheConfiguration("users", 
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)))
            .withCacheConfiguration("deliveryPackages", 
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)))
            .withCacheConfiguration("locationHistory", 
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)))
            .withCacheConfiguration("deliveryReports", 
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(24))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)))
            .build();
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, DeliveryReport> deliveryReportRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, DeliveryReport> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<DeliveryReport> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, DeliveryReport.class);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, DeliveryPackage> deliveryPackageRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, DeliveryPackage> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<DeliveryPackage> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, DeliveryPackage.class);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, LocationHistory> locationHistoryRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, LocationHistory> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<LocationHistory> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, LocationHistory.class);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, User> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, User.class);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, List<DeliveryReport>> deliveryReportListRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, List<DeliveryReport>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        JavaType listType = redisObjectMapper.getTypeFactory().constructCollectionType(List.class, DeliveryReport.class);
        Jackson2JsonRedisSerializer<List<DeliveryReport>> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, listType);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableDefaultSerializer(false);
        
        template.afterPropertiesSet();
        return template;
    }
} 