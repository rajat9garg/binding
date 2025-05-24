package com.biding.db

import com.biding.domain.model.User

interface UserRepository {
    fun save(user: User): User
    fun findByPhoneNumber(phoneNumber: String): User?
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}