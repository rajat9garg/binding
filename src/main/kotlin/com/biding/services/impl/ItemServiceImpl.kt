package com.biding.services.impl

import com.biding.db.ItemRepository
import com.biding.domain.exception.InvalidItemStateException
import com.biding.domain.exception.ItemNotFoundException
import com.biding.domain.exception.UnauthorizedOperationException
import com.biding.domain.model.Item
import com.biding.domain.model.ItemStatus
import com.biding.services.ItemService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import jakarta.validation.ValidationException

@Service
class ItemServiceImpl(
    private val itemRepository: ItemRepository
) : ItemService {

    private val logger = LoggerFactory.getLogger(ItemServiceImpl::class.java)

    @Transactional
    override fun createItem(request: Item, createdBy: Long): Item {
        logger.info("Creating new item for user: $createdBy")
        
        validateItemRequest(request)
        val now = Instant.now()
        
        val newItem = request.copy(
            id = null,
            createdBy = createdBy,
            status = ItemStatus.DRAFT,
            version = 0,
            createdAt = now,
            updatedAt = now
        )

        return try {
            itemRepository.save(newItem).also {
                logger.info("Successfully created item with id: ${it.id} for user: $createdBy")
            }
        } catch (ex: Exception) {
            logger.error("Failed to create item: ${ex.message}", ex)
            throw IllegalStateException("Failed to create item: ${ex.message}", ex)
        }
    }

    @Transactional
    override fun updateItem(id: Long, request: Item, updatedBy: Long): Item {
        logger.info("Updating item: $id by user: $updatedBy")
        
        validateItemRequest(request)
        
        val existingItem = getItemById(id) ?: throw com.biding.domain.exception.ItemNotFoundException(id)
        validateItemOwnership(existingItem, updatedBy)
        validateItemIsModifiable(existingItem)

        val updatedItem = existingItem.copyWith(
            name = request.name,
            description = request.description,
            status = request.status,
            auctionStartTime = request.auctionStartTime,
            auctionEndTime = request.auctionEndTime
        )

        return try {
            itemRepository.save(updatedItem).also {
                logger.info("Successfully updated item: $id by user: $updatedBy")
            }
        } catch (ex: Exception) {
            logger.error("Failed to update item: $id - ${ex.message}", ex)
            throw IllegalStateException("Failed to update item: ${ex.message}", ex)
        }
    }

    @Transactional(readOnly = true)
    override fun getItemById(id: Long): Item? {
        logger.debug("Fetching item with id: $id")
        return try {
            itemRepository.findById(id)?.also {
                logger.debug("Found item with id: $id")
            } ?: run {
                logger.debug("Item not found with id: $id")
                null
            }
        } catch (ex: Exception) {
            logger.error("Error fetching item with id: $id - ${ex.message}", ex)
            throw IllegalStateException("Error fetching item with id: $id", ex)
        }
    }

    @Transactional(readOnly = true)
    override fun searchItems(criteria: Item, pageable: Pageable): List<Item> {
        logger.debug("Searching items with criteria: $criteria, page: $pageable")
        
        return try {
            itemRepository.findAll(criteria.status, pageable.pageNumber, pageable.pageSize).also {
                logger.debug("Found ${it.size} items matching criteria")
            }
        } catch (ex: Exception) {
            logger.error("Error searching items: ${ex.message}", ex)
            throw IllegalStateException("Error searching items: ${ex.message}", ex)
        }
    }

    @Transactional
    override fun deleteItem(id: Long, userId: Long) {
        logger.info("Deleting item: $id by user: $userId")
        
        val item = getItemById(id) ?: throw com.biding.domain.exception.ItemNotFoundException(id)
        validateItemOwnership(item, userId)
        validateItemIsModifiable(item)
        
        try {
            itemRepository.deleteById(id)
            logger.info("Successfully deleted item: $id by user: $userId")
        } catch (ex: Exception) {
            logger.error("Failed to delete item: $id - ${ex.message}", ex)
            throw IllegalStateException("Failed to delete item: $id", ex)
        }
    }

    @Transactional
    override fun updateCurrentBid(itemId: Long, bidId: Long): Boolean {
        logger.info("Updating current bid for item: $itemId with bid: $bidId")
        
        if (bidId <= 0) {
            throw ValidationException("Invalid bid ID: $bidId")
        }
        
        val item = getItemById(itemId) ?: throw com.biding.domain.exception.ItemNotFoundException(itemId)
        
        if (item.status != ItemStatus.ONGOING) {
            logger.warn("Cannot update bid for item: $itemId - Item is not in ONGOING status")
            return false
        }
        
        // TODO: Implement actual bid update logic
        logger.debug("Bid update logic will be implemented here")
        
        return true
    }

    @Transactional
    override fun updateItemStatus(itemId: Long, status: ItemStatus): Boolean {
        logger.info("Updating status for item: $itemId to: $status")
        
        val item = getItemById(itemId) ?: throw com.biding.domain.exception.ItemNotFoundException(itemId)
        
        if (item.status == status) {
            logger.debug("Item $itemId already has status: $status")
            return true
        }
        
        val updatedItem = item.copyWith(status = status)
        
        return try {
            itemRepository.save(updatedItem)
            logger.info("Successfully updated status for item: $itemId to: $status")
            true
        } catch (ex: Exception) {
            logger.error("Failed to update status for item: $itemId - ${ex.message}", ex)
            false
        }
    }

    private fun validateItemRequest(item: Item) {
        require(item.name.isNotBlank()) { "Item name cannot be blank" }
        require(item.basePrice > java.math.BigDecimal.ZERO) { "Base price must be positive" }
        require(item.auctionEndTime.isAfter(item.auctionStartTime)) {
            "Auction end time must be after start time"
        }
    }

    private fun validateItemOwnership(item: Item, userId: Long) {
        if (item.createdBy != userId) {
            logger.warn("User $userId attempted to access item ${item.id} without ownership")
            throw com.biding.domain.exception.UnauthorizedOperationException("User $userId is not the owner of item ${item.id}")
        }
    }

    private fun validateItemIsModifiable(item: Item) {
        if (item.status !in listOf(ItemStatus.DRAFT, ItemStatus.UPCOMING)) {
            logger.warn("Attempted to modify item ${item.id} in non-modifiable state: ${item.status}")
            throw InvalidItemStateException("Item ${item.id} cannot be modified in status ${item.status}")
        }
    }
}
