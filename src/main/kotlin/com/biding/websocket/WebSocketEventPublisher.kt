package com.biding.websocket

import com.biding.domain.model.AuctionStatusMessage
import com.biding.domain.model.BidUpdateMessage
import com.biding.domain.model.ItemStatus
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset

/**
 * Component responsible for publishing WebSocket events.
 * 
 * This class provides methods to broadcast auction-related events to connected WebSocket clients.
 * It decouples the event generation from the WebSocket messaging infrastructure.
 */
@Component
class WebSocketEventPublisher(
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Broadcasts a bid update to all subscribers of an auction.
     * 
     * @param auctionId The ID of the auction
     * @param currentPrice The new current price after the bid
     * @param bidderId The ID of the user who placed the bid
     */
    @Async
    fun broadcastBidUpdate(auctionId: Long, currentPrice: Double, bidderId: Long?) {
        try {
            val message = BidUpdateMessage(
                itemId = auctionId,
                currentPrice = currentPrice,
                bidderId = bidderId,
                timestamp = Instant.now()
            )
            
            val destination = "/topic/auctions/$auctionId/bids"
            messagingTemplate.convertAndSend(destination, message)
            logger.debug("Broadcasted bid update for auction $auctionId")
        } catch (e: Exception) {
            logger.error("Failed to broadcast bid update for auction $auctionId: ${e.message}", e)
        }
    }

    /**
     * Broadcasts an auction status change to all subscribers.
     * 
     * @param auctionId The ID of the auction
     * @param status The new status of the auction
     */
    @Async
    fun broadcastAuctionStatus(auctionId: Long, status: ItemStatus) {
        try {
            val message = AuctionStatusMessage(
                itemId = auctionId,
                status = status,
                timestamp = Instant.now()
            )
            
            val destination = "/topic/auctions/$auctionId/status"
            messagingTemplate.convertAndSend(destination, message)
            logger.info("Broadcasted status update for auction $auctionId: $status")
        } catch (e: Exception) {
            logger.error("Failed to broadcast status update for auction $auctionId: ${e.message}", e)
        }
    }

    /**
     * Sends a notification to a specific user.
     * 
     * @param userId The ID of the user to notify
     * @param message The notification message
     * @param type The type of notification (optional)
     */
    @Async
    fun sendUserNotification(userId: Long, message: String, type: String = "INFO") {
        try {
            val payload = mapOf(
                "type" to type,
                "message" to message,
                "timestamp" to Instant.now().atOffset(ZoneOffset.UTC).toString()
            )
            
            val destination = "/queue/notifications"
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload)
            logger.debug("Sent notification to user $userId: $message")
        } catch (e: Exception) {
            logger.error("Failed to send notification to user $userId: ${e.message}", e)
        }
    }

    /**
     * Handles application events for auction updates.
     * This allows other parts of the application to publish events without direct dependencies.
     */
    @EventListener
    fun handleAuctionUpdateEvent(event: AuctionUpdateEvent) {
        when (event) {
            is AuctionBidEvent -> broadcastBidUpdate(event.auctionId, event.currentPrice, event.bidderId)
            is AuctionStatusChangeEvent -> broadcastAuctionStatus(event.auctionId, event.newStatus)
        }
    }
}

/**
 * Sealed class representing different types of auction update events.
 */
sealed class AuctionUpdateEvent

data class AuctionBidEvent(
    val auctionId: Long,
    val currentPrice: Double,
    val bidderId: Long?
) : AuctionUpdateEvent()

data class AuctionStatusChangeEvent(
    val auctionId: Long,
    val newStatus: ItemStatus
) : AuctionUpdateEvent()
