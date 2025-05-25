package com.biding.domain.exception

/**
 * Exception thrown when there is an error accessing the data store.
 * This is a runtime exception as data access failures are typically non-recoverable
 * and should be handled at a higher level.
 *
 * @param message The detail message
 * @param cause The root cause of the exception
 */
class DataAccessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
