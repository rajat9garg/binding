package com.biding.controllers

import com.biding.generated.api.HealthApi
import com.biding.mappers.HealthMapper
import com.biding.services.HealthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    private val healthService: HealthService,
    private val healthMapper: HealthMapper
) : HealthApi {

    init {
        println("HealthController initialized")
    }

    override fun healthCheck(): ResponseEntity<com.biding.generated.model.HealthResponse> {
        val healthStatus = healthService.checkHealth()
        return ResponseEntity.ok(healthMapper.toApiResponse(healthStatus))
    }
}
