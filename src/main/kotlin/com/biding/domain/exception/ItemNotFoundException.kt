package com.biding.domain.exception

/**
 * Exception thrown when an item is not found in the system.
 *
 * @param id The ID of the item that was not found
 */
class ItemNotFoundException(id: Long) : 
    RuntimeException("Item with id $id not found")


class UnauthorizedOperationException(message: String) : RuntimeException(message)