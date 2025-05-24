package com.biding.services

import com.biding.domain.model.User

interface UserService {
    fun registerUser(user: User): User
}
