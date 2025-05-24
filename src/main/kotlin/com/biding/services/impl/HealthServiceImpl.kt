package com.biding.services.impl

import com.biding.services.HealthService
import org.springframework.stereotype.Service

@Service
class HealthServiceImpl : HealthService {
    override fun checkHealth(): String {
        return "UP"
    }
}
