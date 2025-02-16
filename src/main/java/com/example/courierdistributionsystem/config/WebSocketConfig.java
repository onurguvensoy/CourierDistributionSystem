package com.example.courierdistributionsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.lang.NonNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.List;
import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper;

    public WebSocketConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/ws");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
               .setAllowedOrigins("http://localhost:3000")
               .setHandshakeHandler(new DefaultHandshakeHandler())
               .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        try {
                            Claims claims = Jwts.parser()
                                .setSigningKey(jwtSecret)
                                .parseClaimsJws(token)
                                .getBody();

                            String username = claims.getSubject();
                            String role = claims.get("role", String.class);
                            
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                            
                            accessor.setUser(auth);
                            logger.debug("WebSocket connection authenticated for user: {}", username);
                        } catch (Exception e) {
                            logger.error("Invalid JWT token in WebSocket connection: {}", e.getMessage());
                            return null;
                        }
                    } else {
                        logger.warn("No JWT token found in WebSocket connection");
                        return null;
                    }
                }
                return message;
            }
        });
    }

    @Override
    public void configureWebSocketTransport(@NonNull WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(8192 * 8192);
        registration.setSendBufferSizeLimit(8192 * 8192);
        registration.setSendTimeLimit(20000);
    }

    @Override
    public boolean configureMessageConverters(@NonNull List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        messageConverters.add(converter);
        return false;
    }
} 