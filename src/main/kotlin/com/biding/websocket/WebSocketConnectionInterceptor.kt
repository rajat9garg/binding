package com.biding.websocket

import com.biding.domain.model.WebSocketConnection
import com.biding.db.WebSocketConnectionRepository
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component
import java.security.Principal

/**
 * Intercepts WebSocket messages to track connection lifecycle events.
 * 
 * This interceptor is responsible for:
 * - Tracking when clients connect and disconnect
 * - Updating connection status in the database
 * - Extracting user information from the WebSocket session
 * - Managing connection metadata (user agent, IP address, etc.)
 */
@Component
class WebSocketConnectionInterceptor(
    private val connectionRepository: WebSocketConnectionRepository
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Intercepts messages before they are sent to the message channel.
     * Handles connection lifecycle events (CONNECT, DISCONNECT, SUBSCRIBE).
     */
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        when (accessor.command) {
            StompCommand.CONNECT -> handleConnect(accessor)
            StompCommand.DISCONNECT -> handleDisconnect(accessor)
            StompCommand.SUBSCRIBE -> handleSubscribe(accessor)
            else -> {}
        }

        return message
    }

    /**
     * Handles a new WebSocket connection.
     * Extracts user information and connection details, then saves them to the database.
     */
    private fun handleConnect(accessor: StompHeaderAccessor) {
        val sessionId = accessor.sessionId ?: return
        val userId = (accessor.user as? Principal)?.name?.toLongOrNull()
        val userAgent = accessor.getFirstNativeHeader("user-agent")
        val ipAddress = getClientIp(accessor)

        val connection = WebSocketConnection(
            connectionId = sessionId,
            userId = userId,
            sessionId = sessionId,
            userAgent = userAgent,
            ipAddress = ipAddress,
            status = WebSocketConnection.ConnectionStatus.CONNECTED
        )

        try {
            connectionRepository.save(connection)
            logger.info("WebSocket connected: session=$sessionId, user=$userId")
        } catch (e: Exception) {
            logger.error("Error saving WebSocket connection: ${e.message}", e)
        }
    }

    /**
     * Handles WebSocket disconnection.
     * Updates the connection status in the database.
     */
    private fun handleDisconnect(accessor: StompHeaderAccessor) {
        val sessionId = accessor.sessionId ?: return

        try {
            connectionRepository.markDisconnected(sessionId)?.let {
                logger.info("WebSocket disconnected: session=$sessionId, user=${it.userId}")
            } ?: logger.warn("No active connection found for session: $sessionId")
        } catch (e: Exception) {
            logger.error("Error handling WebSocket disconnect: ${e.message}", e)
        }
    }

    /**
     * Handles subscription events to update the last active timestamp.
     */
    private fun handleSubscribe(accessor: StompHeaderAccessor) {
        val sessionId = accessor.sessionId ?: return

        try {
            connectionRepository.updateLastActive(sessionId)?.let {
                logger.trace("Updated last active for session: $sessionId")
            }
        } catch (e: Exception) {
            logger.error("Error updating last active time: ${e.message}", e)
        }
    }

    /**
     * Extracts the client IP address from the WebSocket headers.
     * Checks common proxy headers (X-Forwarded-For, X-Real-IP) before falling back to the remote address.
     */
    private fun getClientIp(accessor: StompHeaderAccessor): String? {
        val xff = accessor.getFirstNativeHeader("x-forwarded-for")
        if (!xff.isNullOrBlank()) {
            return xff.split(",").firstOrNull()?.trim()
        }
        
        return accessor.getFirstNativeHeader("x-real-ip") ?: accessor.host
    }
}
