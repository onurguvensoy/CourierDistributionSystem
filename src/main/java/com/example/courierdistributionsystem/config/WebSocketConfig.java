package com.example.courierdistributionsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import java.util.concurrent.Executors;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.SimpleMessageConverter;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(new long[]{10000, 20000})
            .setTaskScheduler(new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor()));
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
    }

    @Override
    public void configureWebSocketTransport(@NonNull WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024)    // Max message size 128KB
            .setSendBufferSizeLimit(512 * 1024)         // Max buffer size 512KB
            .setSendTimeLimit(20 * 1000);               // 20 second timeout
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor == null) {
                    return message;
                }

                var command = accessor.getCommand();
                if (command == null) {
                    return message;
                }

                try {
                    switch (command) {
                        case CONNECT -> handleConnect(accessor);
                        case SUBSCRIBE -> {
                            if (!handleSubscribe(accessor)) {
                                return null; // Block invalid subscriptions
                            }
                        }
                        case UNSUBSCRIBE -> {} // Allow unsubscribe without additional handling
                        case DISCONNECT -> handleDisconnect(accessor);
                        case SEND -> handleSend(accessor);
                        case CONNECTED, ACK, NACK, BEGIN, COMMIT, ABORT, 
                             STOMP, MESSAGE, RECEIPT, ERROR -> {
                            // Default handling for other commands
                        }
                    }
                } catch (Exception e) {
                    // Log error and allow message through
                    System.err.println("Error processing STOMP command: " + e.getMessage());
                }

                return message;
            }
        });
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        var sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }
        
        HttpSession session = (HttpSession) sessionAttributes.get("HTTP.SESSION");
        if (session != null) {
            String username = (String) session.getAttribute("username");
            if (username != null && !username.isEmpty()) {
                accessor.setUser(() -> username);
            }
        }
    }

    private boolean handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return false;
        }

        var user = accessor.getUser();
        if (user == null) {
            return false;
        }

        return isValidSubscription(destination, user.getName());
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        // Clean up any resources if needed
        var sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.remove("HTTP.SESSION");
        }
    }

    private void handleSend(StompHeaderAccessor accessor) {
        // Validate send permissions if needed
        var user = accessor.getUser();
        if (user == null) {
            // Could throw exception or handle differently
            return;
        }

        // Add any additional headers or validations
        accessor.setNativeHeader("sender", user.getName());
    }

    @Override
    public void configureClientOutboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null) {
                    accessor.setLeaveMutable(true);
                }
                return message;
            }
        });
    }

    private boolean isValidSubscription(String destination, String username) {
        if (username == null || destination == null) {
            return false;
        }

        // Allow admin to subscribe to all topics
        if (destination.startsWith("/topic/admin/")) {
            return username.startsWith("admin");
        }
        
        // Allow users to subscribe only to their own queues
        if (destination.startsWith("/user/queue/")) {
            return true;
        }
        
        // Allow subscription to public topics
        return destination.startsWith("/topic/packages") || 
               destination.startsWith("/topic/notifications");
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setStrictContentTypeMatch(false);
        messageConverters.add(converter);
        messageConverters.add(new SimpleMessageConverter());
        return false;
    }
} 