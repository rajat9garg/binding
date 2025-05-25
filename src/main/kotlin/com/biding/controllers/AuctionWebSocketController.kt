package com.biding.controllers

import com.biding.domain.model.*
import com.biding.services.ItemService
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import java.security.Principal
import java.time.Instant

/**
 * WebSocket controller for handling real-time auction events.
 * 
 * This controller handles WebSocket messages related to auction activities:
 * - Subscribing to auction updates
 * - Placing bids
 * - Receiving auction status changes
 */
@Controller
@RequestMapping("\${api.base-path:/api/v1}")
class AuctionWebSocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val itemService: ItemService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles subscription to auction updates.
     * Sends the current state of the auction to the subscribing user.
     */
    @MessageMapping("/auctions/{auctionId}/subscribe")
    @SendTo("/topic/auctions/{auctionId}")
    fun subscribeToAuction(
        @DestinationVariable auctionId: Long,
        principal: Principal
    ): AuctionStatusMessage {
        logger.info("User ${principal.name} subscribed to auction $auctionId")
        
        val item = itemService.getItemById(auctionId)

        return AuctionStatusMessage(
            itemId = item?.id!!,
            status = item.status,
            timestamp = Instant.now()
        )

    }

    /**
     * Handles bid placement from WebSocket clients.
     * Validates the bid and broadcasts the update to all subscribers.
     */
    @MessageMapping("/auctions/{auctionId}/bid")
    fun placeBid(
        @DestinationVariable auctionId: Long,
        @Payload bidRequest: Map<String, Any>,
        principal: Principal
    ) {
        val userId = principal.name.toLong()
        val amount = (bidRequest["amount"] as Number).toDouble()
        
        logger.info("Received bid from user $userId for auction $auctionId: $amount")
        
        try {
            // TODO: create bid function
//            val updatedItem = itemService.placeBid(auctionId, userId, amount)
            
            // Broadcast bid update to all subscribers
            val bidUpdate = BidUpdateMessage(
                itemId = auctionId,
                currentPrice = 0.0,
                bidderId = userId,
                timestamp = Instant.now()
            )
            
            messagingTemplate.convertAndSend("/topic/auctions/$auctionId/bids", bidUpdate)
            logger.debug("Broadcasted bid update for auction $auctionId")
            
        } catch (e: Exception) {
            logger.error("Error processing bid: ${e.message}", e)
            val error = ErrorMessage(
                code = "BID_ERROR",
                message = e.message ?: "Failed to place bid",
                timestamp = Instant.now()
            )
            messagingTemplate.convertAndSendToUser(principal.name, "/queue/errors", error)
        }
    }

    /**
     * Handles auction status change notifications.
     * Broadcasts status updates to all subscribers of the auction.
     */
    fun broadcastAuctionStatus(itemId: Long, status: ItemStatus) {
        logger.info("Broadcasting status update for auction $itemId: $status")
        
        val statusMessage = AuctionStatusMessage(
            itemId = itemId,
            status = status,
            timestamp = Instant.now()
        )
        
        messagingTemplate.convertAndSend("/topic/auctions/$itemId/status", statusMessage)
    }

    /**
     * Sends a notification to a specific user.
     */
    fun sendUserNotification(userId: Long, message: String) {
        val notification = mapOf(
            "type" to "NOTIFICATION",
            "message" to message,
            "timestamp" to Instant.now().toString()
        )
        
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", notification)
    }
}
