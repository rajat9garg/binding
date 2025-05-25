package com.biding.domain.model

/**
 * Represents the status of an item's auction.
 * 
 * - DRAFT: Item is being prepared and not yet published
 * - UPCOMING: Auction is scheduled but hasn't started yet
 * - ONGOING: Auction is active and accepting bids
 * - ENDED: Auction has completed
 * - CANCELLED: Auction was cancelled
 */
enum class ItemStatus {
    DRAFT,
    UPCOMING,
    ONGOING,
    ENDED,
    CANCELLED;

    companion object {
        fun fromString(value: String): ItemStatus {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid item status: $value")
            }
        }
    }
}
