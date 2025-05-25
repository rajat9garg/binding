package com.biding.db.jooq

import com.biding.db.ItemRepository
import com.biding.domain.model.Item
import com.biding.domain.model.ItemStatus
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.RecordMapper
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JooqItemRepository(
    private val dsl: DSLContext
) : ItemRepository {
    
    companion object {
        private const val ITEMS_TABLE = "items"
        private const val ID = "id"
        private const val NAME = "name"
        private const val DESCRIPTION = "description"
        private const val BASE_PRICE = "base_price"
        private const val STATUS = "status"
        private const val AUCTION_START_TIME = "auction_start_time"
        private const val AUCTION_END_TIME = "auction_end_time"
        private const val CURRENT_BID_ID = "current_bid_id"
        private const val CREATED_BY = "created_by"
        private const val CREATED_AT = "created_at"
        private const val UPDATED_AT = "updated_at"
        private const val VERSION = "version"
    }
    
    private val itemRecordMapper = RecordMapper<Record, Item> { record ->
        Item(
            id = record.get(ID, Long::class.java),
            name = record.get(NAME, String::class.java),
            description = record.get(DESCRIPTION, String::class.java),
            basePrice = record.get(BASE_PRICE, BigDecimal::class.java),
            status = ItemStatus.valueOf(record.get(STATUS, String::class.java)),
            auctionStartTime = record.get(AUCTION_START_TIME, Instant::class.java),
            auctionEndTime = record.get(AUCTION_END_TIME, Instant::class.java),
            currentBidId = record.get(CURRENT_BID_ID, Long::class.java),
            createdBy = record.get(CREATED_BY, Long::class.java),
            version = record.get(VERSION, Long::class.java) ?: 0,
            createdAt = record.get(CREATED_AT, Instant::class.java),
            updatedAt = record.get(UPDATED_AT, Instant::class.java)
        )
    }
    
    override fun save(item: Item): Item {
        return if (item.id == null) {
            createItem(item)
        } else {
            updateItem(item)
        }
    }
    
    private fun createItem(item: Item): Item {
        val now = OffsetDateTime.now()
        
        val insert = dsl.insertInto(DSL.table(ITEMS_TABLE))
            .set(DSL.field(NAME), item.name)
            .set(DSL.field(DESCRIPTION), item.description)
            .set(DSL.field(BASE_PRICE), item.basePrice)
            .set(DSL.field(STATUS), item.status.name)
            .set(DSL.field(AUCTION_START_TIME), item.auctionStartTime)
            .set(DSL.field(AUCTION_END_TIME), item.auctionEndTime)
            .set(DSL.field(CURRENT_BID_ID), item.currentBidId)
            .set(DSL.field(CREATED_BY), item.createdBy)
            .set(DSL.field(CREATED_AT), now)
            .set(DSL.field(UPDATED_AT), now)
            .set(DSL.field(VERSION), 1)
            .returningResult(DSL.field(ID, Long::class.java))
            
        val itemId = try {
            insert.fetchOne()
                ?.get(0, Long::class.java)
                ?: throw IllegalStateException("No ID returned after insert")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create item: ${e.message}", e)
        }
            
        return findById(itemId) ?: throw IllegalStateException("Failed to fetch created item with ID: $itemId")
    }
    
    private fun updateItem(item: Item): Item {
        val now = OffsetDateTime.now()
        
        val updated = try {
            dsl.update(DSL.table(ITEMS_TABLE))
                .set(DSL.field(NAME), item.name)
                .set(DSL.field(DESCRIPTION), item.description)
                .set(DSL.field(BASE_PRICE), item.basePrice)
                .set(DSL.field(STATUS), item.status.name)
                .set(DSL.field(AUCTION_START_TIME), item.auctionStartTime)
                .set(DSL.field(AUCTION_END_TIME), item.auctionEndTime)
                .set(DSL.field(CURRENT_BID_ID), item.currentBidId)
                .set(DSL.field(UPDATED_AT), now)
                .set(DSL.field(VERSION), (item.version ?: 0) + 1)
                .where(DSL.field(ID).eq(item.id))
                .and(DSL.field(VERSION).eq(item.version ?: 0))
                .execute()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to update item: ${e.message}", e)
        }
        
        if (updated == 0) {
            // Check if item exists to provide a better error message
            if (!existsById(item.id!!)) {
                throw NoSuchElementException("Item not found with ID: ${item.id}")
            }
            throw IllegalStateException("Item was modified by another transaction or version mismatch")
        }
        
        return findById(item.id!!) ?: throw IllegalStateException("Failed to fetch updated item with ID: ${item.id}")
    }
    
    override fun findById(id: Long): Item? {
        return dsl.select()
            .from(ITEMS_TABLE)
            .where(DSL.field(ID).eq(id))
            .fetchOne()
            ?.let(itemRecordMapper::map)
    }
    
    override fun findAll(
        status: ItemStatus?,
        page: Int,
        size: Int
    ): List<Item> {
        require(page >= 0) { "Page index must not be negative" }
        require(size > 0) { "Page size must be greater than zero" }
        
        val offset = (page * size).toLong()
        
        return dsl.select()
            .from(ITEMS_TABLE)
            .apply {
                status?.let { status ->
                    where(DSL.field(STATUS).eq(status.name))
                }
            }
            .orderBy(
                DSL.field(AUCTION_END_TIME).asc(),
                DSL.field(ID).asc()  // Add secondary sort for consistent ordering
            )
            .limit(size.toLong())
            .offset(offset)
            .fetch()
            .mapNotNull { record ->
                try {
                    itemRecordMapper.map(record)
                } catch (e: Exception) {
                    // Log the error and skip the record
                    // In a real app, you might want to use a proper logger
                    System.err.println("Error mapping item record: ${e.message}")
                    null
                }
            }
            .filterNotNull()
    }
    
    override fun findItemsNeedingStatusUpdate(currentTime: Instant): List<Item> {
        if (currentTime.isBefore(Instant.EPOCH)) {
            throw IllegalArgumentException("Current time cannot be before epoch")
        }
        
        return dsl.select()
            .from(ITEMS_TABLE)
            .where(
                DSL.field(STATUS).`in`(
                    ItemStatus.UPCOMING.name,
                    ItemStatus.ONGOING.name
                )
                .and(
                    // Items that need status update:
                    // 1. UPCOMING items where start time has passed
                    DSL.condition("""
                        (status = ? AND auction_start_time <= ?) OR
                        (status = ? AND auction_end_time <= ?)
                    """.trimIndent(),
                    ItemStatus.UPCOMING.name, currentTime,
                    ItemStatus.ONGOING.name, currentTime
                    )
                )
            )
            .orderBy(DSL.field(AUCTION_END_TIME).asc())
            .fetch()
            .mapNotNull { record ->
                try {
                    itemRecordMapper.map(record)
                } catch (e: Exception) {
                    System.err.println("Error mapping item record for status update: ${e.message}")
                    null
                }
            }
            .filterNotNull()
    }
    
    override fun updateCurrentBid(itemId: Long, bidId: Long): Boolean {
        val updated = dsl.update(DSL.table(ITEMS_TABLE))
            .set(DSL.field(CURRENT_BID_ID), bidId)
            .set(DSL.field(UPDATED_AT), OffsetDateTime.now())
            .where(DSL.field(ID).eq(itemId))
            .execute()
            
        return updated > 0
    }
    
    override fun updateStatus(itemId: Long, status: ItemStatus): Boolean {
        val updated = dsl.update(DSL.table(ITEMS_TABLE))
            .set(DSL.field(STATUS), status.name)
            .set(DSL.field(UPDATED_AT), OffsetDateTime.now())
            .where(DSL.field(ID).eq(itemId))
            .execute()
            
        return updated > 0
    }
    
    override fun existsById(id: Long): Boolean {
        return dsl.selectCount()
            .from(ITEMS_TABLE)
            .where(DSL.field(ID).eq(id))
            .fetchOne(0, Int::class.java) ?: 0 > 0
    }
    
    override fun deleteById(id: Long): Boolean {
        val deleted = dsl.deleteFrom(DSL.table(ITEMS_TABLE))
            .where(DSL.field(ID).eq(id))
            .execute()
            
        return deleted > 0
    }
}
