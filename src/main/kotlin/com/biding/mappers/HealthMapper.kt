package com.biding.mappers

import com.biding.generated.model.HealthResponse as ApiHealthResponse
import com.biding.models.dto.HealthResponse as DomainHealthResponse
import org.springframework.stereotype.Component

@Component
class HealthMapper {
    fun toApiResponse(domain: DomainHealthResponse): ApiHealthResponse {
        return ApiHealthResponse(
            status = ApiHealthResponse.Status.valueOf(domain.status),
            timestamp = domain.timestamp,
        )
    }
    
    fun toDomainResponse(status: String): DomainHealthResponse {
        return DomainHealthResponse(
            status = status
        )
    }
}
