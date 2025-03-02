package com.example.courierdistributionsystem.interceptor;

import com.example.courierdistributionsystem.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            logger.debug("Validating WebSocket connection with Authorization header");

            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0).replace("Bearer ", "");
                
                try {
                    if (jwtUtils.validateToken(token)) {
                        String username = jwtUtils.getUsernameFromToken(token);
                        
                        logger.debug("WebSocket connection authenticated for user: {}", username);
                        
                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(username, null, null);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        accessor.setUser(auth);
                    } else {
                        logger.warn("Invalid JWT token in WebSocket connection attempt");
                        return null;
                    }
                } catch (Exception e) {
                    logger.error("Error validating WebSocket connection token: {}", e.getMessage());
                    return null;
                }
            } else {
                logger.warn("No authorization header found in WebSocket connection attempt");
                return null;
            }
        }
        
        return message;
    }
} 