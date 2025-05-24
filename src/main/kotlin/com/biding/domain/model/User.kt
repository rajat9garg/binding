package com.biding.domain.model

import java.time.Instant

data class User(
    val id: Long? = null,
    val phoneNumber: String,
    val name: String,
    val email: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(name.length in 2..100) { "Name must be between 2 and 100 characters" }
        require(phoneNumber.matches(Regex("^\\+[1-9]\\d{1,14}$"))) {
            "Phone number must be in E.164 format"
        }
    }
}
