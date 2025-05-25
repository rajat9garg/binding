package com.biding.controllers

import com.biding.domain.model.User
import com.biding.generated.api.UsersApi
import com.biding.generated.model.UserRegistrationRequest
import com.biding.generated.model.UserResponse
import com.biding.services.UserService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
@RequestMapping("\${api.base-path:/api/v1}")
class UserController(
    private val userService: UserService
) : UsersApi {

    override fun registerUser(userRegistrationRequest: UserRegistrationRequest): ResponseEntity<UserResponse> {
        try {
            // Map OpenAPI model to domain model
            println("[DEBUG] Received registration request for phone: ${userRegistrationRequest.phoneNumber}")
            
            val user = User(
                phoneNumber = userRegistrationRequest.phoneNumber,
                name = userRegistrationRequest.name,
            )
            
            println("[DEBUG] Calling userService.registerUser")
            val savedUser = userService.registerUser(user)
            println("[DEBUG] User registered with ID: ${savedUser.id}")
            
            // Map domain model back to OpenAPI response
            val response = UserResponse(
                id = savedUser.id ?: throw IllegalStateException("User ID should not be null after save"),
                phoneNumber = savedUser.phoneNumber,
                name = savedUser.name,
                createdAt = savedUser.createdAt.atOffset(ZoneOffset.UTC),
            )
            
            val location: URI = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id)
                .toUri()
                
            println("[DEBUG] Returning success response for user ID: ${response.id}")
            return ResponseEntity.created(location).body(response)
        } catch (e: Exception) {
            println("[ERROR] Error in registerUser: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}