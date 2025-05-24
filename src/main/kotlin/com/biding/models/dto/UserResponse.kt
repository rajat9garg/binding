package com.biding.models.dto

import com.biding.domain.model.User
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Response DTO for user data
 * @property id Unique identifier of the user
 * @property phoneNumber User's phone number
 * @property name User's full name
 * @property email User's email (optional)
 * @property createdAt When the user was created
 * @property updatedAt When the user was last updated
 */
data class UserResponse(
    @JsonProperty("id")
    val id: Long,
    
    @JsonProperty("phoneNumber")
    val phoneNumber: String,
    
    @JsonProperty("name")
    val name: String,
    
    @JsonProperty("email")
    val email: String? = null,
    
    @JsonProperty("createdAt")
    val createdAt: Instant,
    
    @JsonProperty("updatedAt")
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(user: User): UserResponse {
            return UserResponse(
                id = user.id ?: throw IllegalArgumentException("User ID cannot be null"),
                phoneNumber = user.phoneNumber,
                name = user.name,
                email = user.email,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
