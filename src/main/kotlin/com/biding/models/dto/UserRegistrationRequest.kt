package com.biding.models.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Request DTO for user registration
 * @property phoneNumber User's phone number in E.164 format
 * @property name User's full name (2-100 characters)
 * @property email User's email (optional)
 */
data class UserRegistrationRequest(
    @field:NotBlank(message = "Phone number is required")
    @field:Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Phone number must be in E.164 format (e.g., +1234567890)"
    )
    @JsonProperty("phoneNumber")
    val phoneNumber: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z\\s'-]+",
        message = "Name can only contain letters, spaces, hyphens, and apostrophes"
    )
    @JsonProperty("name")
    val name: String,

    @field:Pattern(
        regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
        message = "Invalid email format"
    )
    @JsonProperty("email")
    val email: String? = null
)
