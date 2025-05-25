package com.biding.controllers

import com.biding.controllers.mappers.toItemResponse
import com.biding.controllers.mappers.toModel
import com.biding.controllers.mappers.toResponse
import com.biding.generated.api.ItemsApi
import com.biding.generated.model.CreateItemRequest
import com.biding.generated.model.ItemPageResponse
import com.biding.generated.model.ItemResponse
import com.biding.generated.model.UpdateItemRequest
import com.biding.services.ItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("\${api.base-path:/api/v1}")
class ItemController(
    private val itemService: ItemService
): ItemsApi {

    private val logger = org.slf4j.LoggerFactory.getLogger(ItemController::class.java)

    override fun createItem(createItemRequest: CreateItemRequest): ResponseEntity<ItemResponse> {
        return try {
            // Validate auction times
            if (createItemRequest.auctionEndTime.isBefore(createItemRequest.auctionStartTime)) {
                throw IllegalArgumentException("Auction end time must be after start time")
            }
            
            val createdItem = itemService.createItem(createItemRequest.toModel(createdBy = 1), 1)
            ResponseEntity.ok(createdItem.toResponse())
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error creating item", e)
            throw e
        }
    }

    override fun getItem(id: Long): ResponseEntity<ItemResponse> {
        try {
            val item = itemService.getItemById(id)
                ?: return ResponseEntity.notFound().build()

            return ResponseEntity.ok(item.toItemResponse())
        } catch (e: Exception) {
            logger.error("Error fetching item", e)
            throw e
        }

    }

    override fun searchItems(
        page: Int,
        size: Int,
        status: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?
    ): ResponseEntity<ItemPageResponse> {
        return super.searchItems(page, size, status, minPrice, maxPrice)
    }

    override fun updateItem(id: Long, updateItemRequest: UpdateItemRequest): ResponseEntity<ItemResponse> {
        return super.updateItem(id, updateItemRequest)
    }
}