package com.biding.domain.exception

/**
 * Exception thrown when an operation is performed on an item in an invalid state.
 *
 * @param message A descriptive message explaining the invalid state
 */
class InvalidItemStateException(message: String) : RuntimeException(message)
