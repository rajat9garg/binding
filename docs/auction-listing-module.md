# Item & Auction Listing Module - Implementation Plan

## 1. Database Schema

### 1.1 Items Table
```sql
-- V2__create_items_table.sql
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    base_price DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'UPCOMING', 'ONGOING', 'ENDED', 'CANCELLED')),
    auction_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    auction_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_auction_times ON items(auction_start_time, auction_end_time);
```

### 1.2 Item Images Table
```sql
-- V3__create_item_images_table.sql
CREATE TABLE item_images (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_item_images_item_id ON item_images(item_id);
```

## 2. Domain Layer

### 2.1 ItemStatus.kt
```kotlin
// domain/model/ItemStatus.kt
package com.biding.domain.model

enum class ItemStatus {
    DRAFT,
    UPCOMING,
    ONGOING,
    ENDED,
    CANCELLED
}
```

### 2.2 Item.kt
```kotlin
// domain/model/Item.kt
package com.biding.domain.model

import java.math.BigDecimal
import java.time.Instant

data class Item(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val basePrice: BigDecimal,
    val status: ItemStatus,
    val auctionStartTime: Instant,
    val auctionEndTime: Instant,
    val currentBid: BigDecimal? = null,
    val imageUrls: List<String> = emptyList(),
    val version: Long = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val createdBy: Long? = null
) {
    init {
        require(name.length in 1..200) { "Name must be between 1 and 200 characters" }
        require(basePrice > BigDecimal.ZERO) { "Base price must be positive" }
        require(auctionEndTime.isAfter(auctionStartTime)) { 
            "Auction end time must be after start time" 
        }
    }
}
```

## 3. Repository Layer

### 3.1 ItemRepository.kt
```kotlin
// domain/port/out/ItemRepository.kt
package com.biding.domain.port.out

import com.biding.domain.model.Item
import org.springframework.data.domain.Page

interface ItemRepository {
    fun save(item: Item): Item
    fun findById(id: Long): Item?
    fun findAll(
        status: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Page<Item>
    
    fun findUpcomingOrOngoing(): List<Item>
    fun updateStatus(id: Long, status: String): Boolean
    fun updateCurrentBid(itemId: Long, bidAmount: BigDecimal): Boolean
}
```

### 3.2 JooqItemRepository.kt
```kotlin
// adapter/out/persistence/jooq/JooqItemRepository.kt
package com.biding.adapter.out.persistence.jooq

import com.biding.domain.model.Item
import com.biding.domain.port.out.ItemRepository
import org.jooq.DSLContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
    }
    
    override fun save(item: Item): Item {
        // Implementation using JOOQ DSL
        TODO("Implement JOOQ save operation")
    }
    
    override fun findById(id: Long): Item? {
        // Implementation using JOOQ DSL
        TODO("Implement JOOQ find by ID operation")
    }
    
    // Other method implementations...
}
```

## 4. Service Layer

### 4.1 ItemService.kt
kotlin
// domain/service/ItemService.kt
package com.biding.domain.service

import com.biding.domain.model.Item
import com.biding.domain.port.out.ItemRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.math.BigDecimal

@Service
class ItemService(
    private val itemRepository: ItemRepository,
    private val clock: Clock = Clock.systemUTC()
) {
    
    fun createItem(
        name: String,
        description: String?,
        basePrice: BigDecimal,
        auctionStartTime: Instant,
        auctionEndTime: Instant,
        createdBy: Long
    ): Item {
        val item = Item(
            name = name,
            description = description,
            basePrice = basePrice,
            status = "DRAFT",
            auctionStartTime = auctionStartTime,
            auctionEndTime = auctionEndTime,
            createdBy = createdBy
        )
        return itemRepository.save(item)
    }
    
    fun listItems(
        status: String? = null,
        page: Int = 0,
        size: Int = 20
    ) = itemRepository.findAll(status, page, size)
    
    fun getItemDetails(itemId: Long): Item {
        return itemRepository.findById(itemId)
            ?: throw ItemNotFoundException("Item not found with id: $itemId")
    }
    
    fun updateAuctionStatus() {
        val now = Instant.now(clock)
        val items = itemRepository.findUpcomingOrOngoing()
        
        items.forEach { item ->
            when {
                item.auctionStartTime.isBefore(now) && item.status == "UPCOMING" -> {
                    itemRepository.updateStatus(item.id!!, "ONGOING")
                    // Publish event: Auction started
                }
                item.auctionEndTime.isBefore(now) && item.status == "ONGOING" -> {
                    itemRepository.updateStatus(item.id!!, "ENDED")
                    // Publish event: Auction ended
                }
            }
        }
    }
}

class ItemNotFoundException(message: String) : RuntimeException(message)
```

## 5. API Layer

### 5.1 ItemController.kt
```kotlin
// adapter/`in`/web/rest/ItemController.kt
package com.biding.adapter.`in`.web.rest

import com.biding.domain.service.ItemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/v1/auctions")
@Tag(name = "Auctions", description = "Auction management APIs")
class ItemController(
    private val itemService: ItemService
) {
    
    @GetMapping
    @Operation(
        summary = "List all auctions",
        description = "Get a paginated list of auctions with optional status filter"
    )
    fun listAuctions(
        @Parameter(description = "Filter by auction status")
        @RequestParam(required = false) status: String?,
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Number of items per page")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ItemResponse>> {
        val items = itemService.listItems(status, page, size)
        val response = items.map { it.toResponse() }
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/{itemId}")
    @Operation(summary = "Get auction details")
    fun getAuctionDetails(
        @Parameter(description = "ID of the auction item")
        @PathVariable itemId: Long
    ): ResponseEntity<ItemResponse> {
        val item = itemService.getItemDetails(itemId)
        return ResponseEntity.ok(item.toResponse())
    }
}

// DTOs
data class ItemResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val basePrice: BigDecimal,
    val status: String,
    val auctionStartTime: Instant,
    val auctionEndTime: Instant,
    val currentBid: BigDecimal?,
    val imageUrls: List<String>,
    val timeRemaining: Long?,
    val createdAt: Instant,
    val updatedAt: Instant
)

// Extension function to convert domain model to DTO
private fun Item.toResponse(): ItemResponse {
    return ItemResponse(
        id = id!!,
        name = name,
        description = description,
        basePrice = basePrice,
        status = status,
        auctionStartTime = auctionStartTime,
        auctionEndTime = auctionEndTime,
        currentBid = currentBid,
        imageUrls = imageUrls,
        timeRemaining = if (status == "ONGOING") {
            auctionEndTime.epochSecond - Instant.now().epochSecond
        } else {
            null
        },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

## 6. Implementation Steps

1. **Database Setup**
   - Create Flyway migration scripts for new tables
   - Add indexes for performance

2. **Domain Layer**
   - Implement domain models with validation
   - Define repository interfaces

3. **Infrastructure Layer**
   - Implement JOOQ repositories
   - Configure database access

4. **Service Layer**
   - Implement business logic
   - Add transaction management
   - Handle domain events

5. **API Layer**
   - Create REST controllers
   - Implement request/response DTOs
   - Add OpenAPI documentation

6. **Testing**
   - Write unit tests for domain logic
   - Add integration tests for API endpoints
   - Test edge cases and error conditions

7. **Documentation**
   - Update API documentation
   - Add code comments
   - Document database schema

## 7. Dependencies to Add

```gradle
// build.gradle.kts
dependencies {
    // Existing dependencies...
    
    // JOOQ for type-safe SQL
    implementation("org.jooq:jooq-kotlin:3.19.3")
    
    // Pageable support
    implementation("org.springframework.data:spring-data-commons")
    
    // Testing
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}
```

## 8. API Endpoints

### 8.1 List Auctions
- **Method:** GET
- **Path:** `/api/v1/auctions`
- **Query Parameters:**
  - `status`: Filter by status (optional)
  - `page`: Page number (0-based, default: 0)
  - `size`: Number of items per page (default: 20)
- **Response:**
  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "Vintage Watch",
        "description": "Rare vintage watch from 1960s",
        "basePrice": 100.00,
        "status": "ONGOING",
        "auctionStartTime": "2025-05-24T10:00:00Z",
        "auctionEndTime": "2025-05-31T10:00:00Z",
        "currentBid": 150.00,
        "imageUrls": ["https://example.com/watch.jpg"],
        "timeRemaining": 86400,
        "createdAt": "2025-05-24T09:00:00Z",
        "updatedAt": "2025-05-24T09:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
  ```

### 8.2 Get Auction Details
- **Method:** GET
- **Path:** `/api/v1/auctions/{itemId}`
- **Path Parameters:**
  - `itemId`: ID of the auction item
- **Response:**
  ```json
  {
    "id": 1,
    "name": "Vintage Watch",
    "description": "Rare vintage watch from 1960s",
    "basePrice": 100.00,
    "status": "ONGOING",
    "auctionStartTime": "2025-05-24T10:00:00Z",
    "auctionEndTime": "2025-05-31T10:00:00Z",
    "currentBid": 150.00,
    "imageUrls": ["https://example.com/watch.jpg"],
    "timeRemaining": 86400,
    "createdAt": "2025-05-24T09:00:00Z",
    "updatedAt": "2025-05-24T09:00:00Z"
  }
  ```

## 9. Error Handling

### 9.1 Error Response Format
```json
{
  "code": "ITEM_NOT_FOUND",
  "message": "Item not found with id: 999",
  "timestamp": "2025-05-24T10:00:00Z"
}
```

### 9.2 Common Error Codes
- `ITEM_NOT_FOUND`: Requested item does not exist
- `INVALID_AUCTION_DATES`: Auction end time is before start time
- `INVALID_ITEM_STATUS`: Invalid item status transition
- `VALIDATION_ERROR`: Request validation failed

## 10. Next Steps

1. Implement the database migrations
2. Create the domain models and repositories
3. Implement the service layer with business logic
4. Create the REST controllers
5. Add comprehensive tests
6. Document the API with OpenAPI
7. Set up monitoring and logging
