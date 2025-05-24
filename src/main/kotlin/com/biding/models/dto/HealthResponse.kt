package com.biding.models.dto

import java.time.OffsetDateTime

data class HealthResponse(
    val status: String,
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val version: String = "1.0.0"
)
