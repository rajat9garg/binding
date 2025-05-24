package com.biding.services.impl

import com.biding.db.UserRepository
import com.biding.domain.model.User
import com.biding.exception.DuplicatePhoneNumberException
import com.biding.services.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    
    override fun registerUser(user: User): User {
        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(user.phoneNumber)) {
            throw DuplicatePhoneNumberException("Phone number ${user.phoneNumber} is already registered")
        }
        
        // Save and return the user
        return userRepository.save(user)
    }
}
