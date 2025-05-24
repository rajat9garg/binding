package com.biding.services.impl

import com.biding.models.dto.HealthResponse
import com.biding.services.HealthService
import org.springframework.stereotype.Service

@Service
class HealthServiceImpl : HealthService {
    override fun checkHealth(): HealthResponse {
        return HealthResponse(
            status = "UP"
        )
    }
}
