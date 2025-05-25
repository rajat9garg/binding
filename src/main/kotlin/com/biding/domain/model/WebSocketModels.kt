package com.biding.domain.model

import java.time.Instant

/**
 * Represents a WebSocket connection in the system.
 *
 * @property id Unique database identifier
 * @property connectionId Unique identifier for the WebSocket connection
 * @property userId ID of the user who established the connection (nullable for anonymous connections)
 * @property sessionId HTTP session ID associated with the connection
 * @property connectedAt Timestamp when the connection was established
 * @property disconnectedAt Timestamp when the connection was closed (null if still connected)
 * @property lastActiveAt Timestamp of the last activity on this connection
 * @property userAgent User agent string from the connecting client
 * @property ipAddress IP address of the connecting client
 * @property status Current status of the WebSocket connection
 */
data class WebSocketConnection(
    val id: Long? = null,
    val connectionId: String,
    val userId: Long? = null,
    val sessionId: String,
    val connectedAt: Instant = Instant.now(),
    val disconnectedAt: Instant? = null,
    val lastActiveAt: Instant = Instant.now(),
    val userAgent: String? = null,
    val ipAddress: String? = null,
    val status: ConnectionStatus = ConnectionStatus.CONNECTED
) {
    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    /**
     * Creates a new instance with the status set to DISCONNECTED and updates the disconnectedAt timestamp.
     */
    fun markDisconnected(): WebSocketConnection = copy(
        status = ConnectionStatus.DISCONNECTED,
        disconnectedAt = Instant.now()
    )

    /**
     * Creates a new instance with the status set to ERROR.
     */
    fun markError(): WebSocketConnection = copy(status = ConnectionStatus.ERROR)

    /**
     * Updates the last active timestamp to the current time.
     */
    fun updateLastActive(): WebSocketConnection = copy(lastActiveAt = Instant.now())
}

/**
 * Base sealed class for all WebSocket messages.
 * Each message type includes a 'type' field for easy deserialization on the client side.
 */
sealed class AuctionWebSocketMessage {
    /**
     * Message type for identifying the message type on the client side.
     */
    abstract val type: String
}

/**
 * Message containing a list of auction items.
 */
data class AuctionListMessage(
    override val type: String = "AUCTION_LIST",
    val items: List<Item>
) : AuctionWebSocketMessage()

/**
 * Message for bid-related updates.
 */
data class BidUpdateMessage(
    override val type: String = "BID_UPDATE",
    val itemId: Long,
    val currentPrice: Double,
    val bidderId: Long?,
    val timestamp: Instant = Instant.now()
) : AuctionWebSocketMessage()

/**
 * Message for auction status changes.
 */
data class AuctionStatusMessage(
    override val type: String = "AUCTION_STATUS",
    val itemId: Long,
    val status: ItemStatus,
    val timestamp: Instant = Instant.now()
) : AuctionWebSocketMessage()

/**
 * Message for error conditions.
 */
data class ErrorMessage(
    override val type: String = "ERROR",
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now()
) : AuctionWebSocketMessage()

/**
 * Message for connection status updates.
 */
data class ConnectionStatusMessage(
    override val type: String = "CONNECTION_STATUS",
    val status: String,
    val connectionId: String? = null,
    val timestamp: Instant = Instant.now()
) : AuctionWebSocketMessage()
