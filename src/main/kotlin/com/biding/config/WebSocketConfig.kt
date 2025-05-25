package com.biding.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * Configuration class for WebSocket and STOMP message handling.
 * 
 * This class configures the message broker, application destinations, and WebSocket endpoints.
 * It enables WebSocket support with STOMP protocol for real-time communication.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker options.
     * 
     * - Enables a simple in-memory message broker with the "/topic" prefix
     * - Sets the application destination prefix to "/app"
     */
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Enable a simple message broker with the specified destination prefix
        config.enableSimpleBroker(
            "/topic"  // Prefix for topics (pub-sub)
        )
        
        // Set the application destination prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app")
        
        // Set the user destination prefix for user-specific message delivery
        config.setUserDestinationPrefix("/user")
    }

    /**
     * Registers STOMP endpoints and configures CORS.
     * 
     * @param registry The STOMP endpoint registry
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }
}
