package com.biding.services

import com.biding.domain.model.Item
import com.biding.domain.model.ItemStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ItemService {
    fun createItem(request: Item, createdBy: Long): Item
    fun updateItem(id: Long, request: Item, updatedBy: Long): Item
    fun getItemById(id: Long): Item?
    fun searchItems(criteria: Item, pageable: Pageable): List<Item>
    fun deleteItem(id: Long, userId: Long)
    fun updateCurrentBid(itemId: Long, bidId: Long): Boolean
    fun updateItemStatus(itemId: Long, status: ItemStatus): Boolean
}
