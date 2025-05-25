package com.biding.db

import com.biding.domain.model.Item
import com.biding.domain.model.ItemStatus
import java.util.*

/**
 * Repository for item persistence operations.
 */
interface ItemRepository {
    /**
     * Saves an item to the database.
     * @param item The item to save
     * @return The saved item with generated ID
     */
    fun save(item: Item): Item

    /**
     * Finds an item by its ID.
     * @param id The ID of the item to find
     * @return The found item, or null if not found
     */
    fun findById(id: Long): Item?

    /**
     * Finds all items with optional status filter and pagination.
     * @param status Optional status to filter by
     * @param page Zero-based page index
     * @param size The size of the page to be returned
     * @return A page of items
     */
    fun findAll(
        status: ItemStatus? = null,
        page: Int = 0,
        size: Int = 20
    ): List<Item>

    /**
     * Finds items that need status updates (e.g., UPCOMING → ONGOING, ONGOING → ENDED).
     * @param currentTime The current time to check against
     * @return List of items needing status updates
     */
    fun findItemsNeedingStatusUpdate(currentTime: java.time.Instant): List<Item>

    /**
     * Updates the current highest bid for an item.
     * @param itemId The ID of the item
     * @param bidId The ID of the new highest bid
     * @return true if the update was successful, false otherwise
     */
    fun updateCurrentBid(itemId: Long, bidId: Long): Boolean

    /**
     * Updates an item's status.
     * @param itemId The ID of the item
     * @param status The new status
     * @return true if the update was successful, false otherwise
     */
    fun updateStatus(itemId: Long, status: ItemStatus): Boolean

    /**
     * Checks if an item exists with the given ID.
     * @param id The ID to check
     * @return true if an item with the ID exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Deletes an item by its ID.
     * @param id The ID of the item to delete
     * @return true if the item was deleted, false otherwise
     */
    fun deleteById(id: Long): Boolean
}
