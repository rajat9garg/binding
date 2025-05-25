package com.biding.domain.model

import java.math.BigDecimal
import java.time.Instant

/**
 * Represents an item available for auction.
 * 
 * @property id Unique identifier for the item (auto-generated)
 * @property name Name of the item (2-200 characters)
 * @property description Optional detailed description of the item
 * @property basePrice Starting price for the auction (must be positive)
 * @property status Current status of the auction
 * @property auctionStartTime When the auction becomes active
 * @property auctionEndTime When the auction ends
 * @property currentBidId ID of the current highest bid (if any)
 * @property createdBy ID of the user who created the listing
 * @property version For optimistic locking
 * @property createdAt When the item was created
 * @property updatedAt When the item was last updated
 */
data class Item(
    val id: Long? = null,
    var name: String,
    var description: String?,
    val basePrice: BigDecimal,
    var status: ItemStatus,
    var auctionStartTime: Instant,
    var auctionEndTime: Instant,
    val currentBidId: Long? = null,
    var createdBy: Long,
    var version: Long = 0,
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now(),
) {
    init {
        require(name.length in 2..200) { "Name must be between 2 and 200 characters" }
        require(basePrice > BigDecimal.ZERO) { "Base price must be positive" }
        require(auctionEndTime.isAfter(auctionStartTime)) { 
            "Auction end time must be after start time" 
        }
        require(createdBy > 0) { "Created by user ID must be positive" }
    }
    
    /**
     * Creates a new copy of the item with updated fields
     */
    fun copyWith(
        name: String = this.name,
        description: String? = this.description,
        basePrice: BigDecimal = this.basePrice,
        status: ItemStatus = this.status,
        auctionStartTime: Instant = this.auctionStartTime,
        auctionEndTime: Instant = this.auctionEndTime,
        currentBidId: Long? = this.currentBidId,
        version: Long = this.version + 1
    ): Item {
        return copy(
            name = name,
            description = description,
            basePrice = basePrice,
            status = status,
            auctionStartTime = auctionStartTime,
            auctionEndTime = auctionEndTime,
            currentBidId = currentBidId,
            version = version,
            updatedAt = Instant.now()
        )
    }
    
    /**
     * Checks if the auction is active (status is ONGOING and current time is within auction period)
     */
    fun isAuctionActive(now: Instant = Instant.now()): Boolean {
        return status == ItemStatus.ONGOING && 
               now.isAfter(auctionStartTime) && 
               now.isBefore(auctionEndTime)
    }
    
    /**
     * Checks if the auction has ended
     */
    fun isAuctionEnded(now: Instant = Instant.now()): Boolean {
        return status == ItemStatus.ENDED || now.isAfter(auctionEndTime)
    }
}
