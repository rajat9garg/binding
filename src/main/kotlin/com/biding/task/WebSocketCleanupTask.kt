package com.biding.task

import com.biding.db.WebSocketConnectionRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * Scheduled task for cleaning up stale WebSocket connections.
 * 
 * This task runs periodically to:
 * - Remove connections that haven't been active for a configured duration
 * - Clean up any orphaned or stale connection records
 */
@Component
class WebSocketCleanupTask(
    private val connectionRepository: WebSocketConnectionRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // Maximum age of inactive connections before they're considered stale (24 hours)
    private val maxInactiveDuration = Duration.ofHours(24)


    /**
     * Scheduled task that runs every hour to clean up stale connections.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    fun cleanupStaleConnections() {
        try {
            val cutoffTime = Instant.now().minus(maxInactiveDuration)
            val removedCount = connectionRepository.cleanupStaleConnections(cutoffTime)
            
            if (removedCount > 0) {
                logger.info("Cleaned up $removedCount stale WebSocket connections")
            } else {
                logger.trace("No stale WebSocket connections to clean up")
            }
        } catch (e: Exception) {
            logger.error("Error during WebSocket connection cleanup: ${e.message}", e)
        }
    }
}
