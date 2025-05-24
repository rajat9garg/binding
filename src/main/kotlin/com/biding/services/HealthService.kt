package com.biding.services

import com.biding.models.dto.HealthResponse

interface HealthService {
    fun checkHealth(): HealthResponse
}
