package com.biding.services.impl

import com.biding.db.UserRepository
import com.biding.domain.model.User
import com.biding.exception.DuplicatePhoneNumberException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

/**
 * Tests for [UserServiceImpl] focusing on the service layer logic.
 * Model validation is tested separately in the domain model tests.
 */
@ExtendWith(MockKExtension::class)
class UserServiceImplTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserServiceImpl

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = 1L,
            phoneNumber = "+1234567890",
            name = "Test User",
            email = "test@example.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    @Test
    fun `registerUser should save and return user when phone number is unique`() {
        // Given
        every { userRepository.existsByPhoneNumber(testUser.phoneNumber) } returns false
        every { userRepository.save(any()) } returns testUser.copy(id = 1L)

        // When
        val result = userService.registerUser(testUser)

        // Then
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(1L)
        assertThat(result.phoneNumber).isEqualTo(testUser.phoneNumber)
        assertThat(result.name).isEqualTo(testUser.name)
        
        verify(exactly = 1) { userRepository.existsByPhoneNumber(testUser.phoneNumber) }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `registerUser should throw DuplicatePhoneNumberException when phone number already exists`() {
        // Given
        every { userRepository.existsByPhoneNumber(testUser.phoneNumber) } returns true

        // When & Then
        assertThatThrownBy { userService.registerUser(testUser) }
            .isInstanceOf(DuplicatePhoneNumberException::class.java)
            .hasMessageContaining("is already registered")

        verify(exactly = 1) { userRepository.existsByPhoneNumber(testUser.phoneNumber) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `registerUser should throw exception when user with same phone number exists`() {
        // Given
        val existingUser = testUser.copy(id = 2L)
        every { userRepository.existsByPhoneNumber(testUser.phoneNumber) } returns true

        // When & Then
        assertThatThrownBy { userService.registerUser(testUser) }
            .isInstanceOf(DuplicatePhoneNumberException::class.java)
            .hasMessageContaining("is already registered")

        // Verify repository interactions
        verify(exactly = 1) { userRepository.existsByPhoneNumber(testUser.phoneNumber) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
    
    @Test
    fun `registerUser should save user when phone number is unique`() {
        // Given
        val newUser = testUser.copy(id = null)
        val savedUser = testUser.copy(id = 1L)
        
        every { userRepository.existsByPhoneNumber(newUser.phoneNumber) } returns false
        every { userRepository.save(newUser) } returns savedUser

        // When
        val result = userService.registerUser(newUser)

        // Then
        assertThat(result).isEqualTo(savedUser)
        assertThat(result.id).isEqualTo(1L)
        
        verify(exactly = 1) { userRepository.existsByPhoneNumber(newUser.phoneNumber) }
        verify(exactly = 1) { userRepository.save(newUser) }
    }
    
    @Test
    fun `registerUser should handle repository save failure`() {
        // Given
        val newUser = testUser.copy(id = null)
        val errorMessage = "Database error"
        
        every { userRepository.existsByPhoneNumber(newUser.phoneNumber) } returns false
        every { userRepository.save(newUser) } throws RuntimeException(errorMessage)

        // When & Then
        assertThatThrownBy { userService.registerUser(newUser) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining(errorMessage)
            
        verify(exactly = 1) { userRepository.existsByPhoneNumber(newUser.phoneNumber) }
        verify(exactly = 1) { userRepository.save(newUser) }
    }
}
