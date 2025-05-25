package com.biding.db

import com.biding.domain.model.WebSocketConnection

/**
 * Repository interface for managing WebSocket connections.
 * 
 * This interface defines the contract for storing and retrieving WebSocket connection information.
 * Implementations should handle the persistence of connection state.
 */
interface WebSocketConnectionRepository {

    /**
     * Saves or updates a WebSocket connection.
     * 
     * @param connection The WebSocket connection to save
     * @return The saved WebSocket connection
     */
    fun save(connection: WebSocketConnection): WebSocketConnection

    /**
     * Finds a WebSocket connection by its connection ID.
     * 
     * @param connectionId The ID of the connection to find
     * @return The WebSocket connection, or null if not found
     */
    fun findById(connectionId: String): WebSocketConnection?

    /**
     * Marks a WebSocket connection as disconnected.
     * 
     * @param connectionId The ID of the connection to mark as disconnected
     * @return The updated WebSocket connection, or null if not found
     */
    fun markDisconnected(connectionId: String): WebSocketConnection?

    /**
     * Updates the last active timestamp for a WebSocket connection.
     * 
     * @param connectionId The ID of the connection to update
     * @return The updated WebSocket connection, or null if not found
     */
    fun updateLastActive(connectionId: String): WebSocketConnection?

    /**
     * Finds all active WebSocket connections.
     * 
     * @return A list of active WebSocket connections
     */
    fun findAllActive(): List<WebSocketConnection>

    /**
     * Finds all WebSocket connections for a specific user.
     * 
     * @param userId The ID of the user
     * @return A list of WebSocket connections for the user
     */
    fun findByUserId(userId: Long): List<WebSocketConnection>

    /**
     * Removes WebSocket connections that haven't been active since the specified time.
     * 
     * @param olderThan The cutoff time (connections older than this will be removed)
     * @return The number of connections removed
     */
    fun cleanupStaleConnections(olderThan: java.time.Instant): Int
}
