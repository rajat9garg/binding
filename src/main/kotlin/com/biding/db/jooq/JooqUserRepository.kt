package com.biding.db.jooq

import com.biding.db.UserRepository
import com.biding.db.exception.DuplicatePhoneNumberException
import com.biding.domain.model.User
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JooqUserRepository(
    private val dsl: DSLContext
) : UserRepository {

    override fun save(user: User): User {
        return if (user.id == null) {
            insert(user)
        } else {
            update(user)
        }
    }

    private fun insert(user: User): User {
        val now = java.time.OffsetDateTime.now()
        
        // Insert the user and get the generated ID
        val userId = dsl.insertInto(DSL.table("users"))
            .set(DSL.field("phone_number"), user.phoneNumber)
            .set(DSL.field("name"), user.name)
            .set(DSL.field("email"), user.email)
            .set(DSL.field("created_at"), now)
            .set(DSL.field("updated_at"), now)
            .returningResult(DSL.field("id", Long::class.java))
            .fetchOne()
            ?.get(0, Long::class.java)
            ?: throw IllegalStateException("Failed to create user: Could not retrieve generated ID")
            
        println("[DEBUG] Inserted user with ID: $userId")
        
        // Fetch the complete user record
        val result = dsl.select()
            .from(DSL.table("users"))
            .where(DSL.field("id").eq(userId))
            .fetchOne()
            ?: throw IllegalStateException("Failed to fetch created user with ID: $userId")
            
        return toUser(result)
    }

    private fun update(user: User): User {
        val now = java.time.OffsetDateTime.now()
        
        dsl.update(DSL.table("users"))
            .set(DSL.field("phone_number"), user.phoneNumber)
            .set(DSL.field("name"), user.name)
            .set(DSL.field("email"), user.email)
            .set(DSL.field("updated_at"), now)
            .where(DSL.field("id").eq(user.id))
            .execute()
        
        return user.copy(updatedAt = now.toInstant())
    }

    override fun findByPhoneNumber(phoneNumber: String): User? {
        return dsl.select()
            .from(DSL.table("users"))
            .where(DSL.field("phone_number").eq(phoneNumber))
            .fetchOne()
            ?.let { toUser(it) }
    }

    override fun existsByPhoneNumber(phoneNumber: String): Boolean {
        return dsl.fetchExists(
            dsl.selectOne()
                .from(DSL.table("users"))
                .where(DSL.field("phone_number").eq(phoneNumber))
        )
    }

    private fun toUser(record: Record): User {
        try {
            val id = record.get(DSL.field("id", Long::class.java))
            val phoneNumber = record.get(DSL.field("phone_number", String::class.java))
            val name = record.get(DSL.field("name", String::class.java))
            val email = record.get(DSL.field("email", String::class.java))
            
            // Handle OffsetDateTime to Instant conversion
            val createdAt = record.get(DSL.field("created_at", java.time.OffsetDateTime::class.java))?.toInstant()
                ?: throw IllegalStateException("created_at cannot be null")
            val updatedAt = record.get(DSL.field("updated_at", java.time.OffsetDateTime::class.java))?.toInstant()
                ?: throw IllegalStateException("updated_at cannot be null")
            
            println("[DEBUG] Mapping user record - ID: $id, Name: $name, Email: $email")
            
            return User(
                id = id,
                phoneNumber = phoneNumber,
                name = name,
                email = email,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            println("[ERROR] Error mapping user record: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
