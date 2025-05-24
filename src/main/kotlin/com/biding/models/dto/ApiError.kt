package com.biding.models.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * Standardized error response for the API
 * @property status HTTP status code
 * @property error Error type
 * @property message Human-readable error message
 * @property path Request path
 * @property timestamp When the error occurred
 * @property details Additional error details (optional)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiError(
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val timestamp: Instant = Instant.now(),
    val details: List<ValidationError>? = null
)

/**
 * Represents a validation error for a specific field
 * @property field The field that failed validation
 * @property error The validation error message
 */
data class ValidationError(
    val field: String,
    val error: String
)
