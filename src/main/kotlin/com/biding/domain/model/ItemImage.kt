package com.biding.domain.model

import java.time.Instant

/**
 * Represents an image associated with an auction item.
 * 
 * @property id Unique identifier for the image (auto-generated)
 * @property itemId ID of the item this image belongs to
 * @property imageUrl URL where the image is stored
 * @property isPrimary Whether this is the primary image for the item
 * @property displayOrder Order in which the image should be displayed
 * @property createdAt When the image was added
 */
data class ItemImage(
    val id: Long? = null,
    val itemId: Long,
    val imageUrl: String,
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0,
    val createdAt: Instant = Instant.now()
) {
    init {
        require(itemId > 0) { "Item ID must be positive" }
        require(imageUrl.isNotBlank()) { "Image URL cannot be blank" }
        require(displayOrder >= 0) { "Display order cannot be negative" }
    }
    
    /**
     * Creates a new copy of the item image with updated fields
     */
    fun copyWith(
        imageUrl: String = this.imageUrl,
        isPrimary: Boolean = this.isPrimary,
        displayOrder: Int = this.displayOrder
    ): com.biding.domain.model.ItemImage {
        return copy(
            imageUrl = imageUrl,
            isPrimary = isPrimary,
            displayOrder = displayOrder
        )
    }
}
