package com.biding.services.impl

import com.biding.models.dto.HealthResponse
import com.biding.services.HealthService
import org.springframework.stereotype.Service

@Service
class HealthServiceImpl : HealthService {
    override fun checkHealth(): com.biding.models.dto.HealthResponse {
        return com.biding.models.dto.HealthResponse(
            status = "UP"
        )
    }
}
