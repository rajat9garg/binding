package com.biding.mappers

import com.biding.models.dto.HealthResponse
import org.springframework.stereotype.Component

@Component
class HealthMapper {
    fun toHealthResponse(status: String): HealthResponse {
        return HealthResponse(status = status)
    }
}
