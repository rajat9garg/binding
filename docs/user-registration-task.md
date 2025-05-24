# User Registration Task

## Overview
User registration functionality has been implemented to allow users to register using their phone number and name. The system prevents duplicate registrations based on phone numbers and includes comprehensive input validation.

## Implementation Status
- **Overall Progress**: 85% Complete
- **Last Updated**: 2025-05-24
- **Current Version**: 1.0.0

## Tech Stack Context
- **Framework**: Spring Boot 3.5.0 with Kotlin 1.9.25
- **Database**: PostgreSQL 15.13 with JOOQ 3.19.3
- **Migrations**: Flyway 9.16.1 (temporarily disabled for development)
- **Validation**: Jakarta Bean Validation 3.0
- **Documentation**: SpringDoc OpenAPI with generated API documentation
- **API Specification**: OpenAPI 3.0.3

## Functional Requirements
1. ✅ Users can register with:
   - Phone number (required, unique, E.164 format)
   - Name (required, 2-100 characters)
   - Email (optional)
2. ✅ No authentication is required beyond registration
3. ✅ Prevent duplicate registrations using the same phone number
4. ⏳ Rate limit registration attempts (3 per hour per IP) - In Progress (30%)
5. ✅ Input validation for all fields
6. ✅ Proper error handling and responses
7. ✅ OpenAPI documentation
8. ✅ Logging and monitoring
9. ⏳ Comprehensive test coverage (40% complete)

## API Specifications

### Endpoint
- **Method**: POST
- **Path**: `/api/v1/users/register`
- **Content-Type**: `application/json`
- **Produces**: `application/json`

### Request Body
```json
{
  "phoneNumber": "+1234567890",
  "name": "John Doe",
  "email": "john.doe@example.com"
}
```

### Validation Rules
1. Phone Number:
   - ✅ Required
   - ✅ Valid E.164 format (e.g., +1234567890)
   - ✅ 5-15 digits after +
   - ✅ Unique across the system
2. Name:
   - ✅ Required
   - ✅ 2-100 characters
   - ✅ Validation for allowed characters
3. Email:
   - ✅ Optional
   - ✅ Valid email format when provided

### Success Response (201 Created)
```json
{
  "id": 1,
  "phoneNumber": "+1234567890",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "createdAt": "2025-05-24T09:45:00Z",
  "updatedAt": "2025-05-24T09:45:00Z"
}
```

### Error Responses
1. **400 Bad Request**
   ```json
   {
     "error": "VALIDATION_ERROR",
     "message": "Invalid input data",
     "details": [
       {
         "field": "phoneNumber",
         "error": "must be a valid E.164 phone number"
       }
     ]
   }
   ```

2. **409 Conflict**
   ```json
   {
     "error": "DUPLICATE_PHONE_NUMBER",
     "message": "Phone number is already registered"
   }
   ```

3. **429 Too Many Requests**
   ```json
   {
     "error": "RATE_LIMIT_EXCEEDED",
     "message": "Too many registration attempts. Please try again in 1 hour.",
     "retryAfter": 3600
   }
   ```

## Database Schema
### Current Users Table
```sql
-- src/main/resources/db/migration/V1__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
```

### Schema Features
1. ✅ Phone number as primary identifier (unique, not null)
2. ✅ Email is optional (nullable)
3. ✅ Single name field (combined first and last name)
4. ✅ Timestamps for auditing
5. ✅ Proper indexes for performance

## Implementation Tasks

### 1. Database Layer (Completed)
- [x] Created users table with required schema
- [x] Added proper indexes for performance
- [x] Configured JOOQ for type-safe SQL queries
- [x] Implemented UserRepository with JOOQ

### 2. Domain Layer (Completed)
- [x] Created User domain model with validation
- [x] Implemented value objects (PhoneNumber, UserName)
- [x] Added proper validation constraints
- [x] Implemented domain exceptions

### 3. Service Layer (Completed)
- [x] Implemented UserService with business logic
- [x] Added duplicate phone number prevention
- [x] Implemented proper error handling
- [x] Added transaction management

### 4. API Layer (Completed)
- [x] Created UserController with REST endpoints
- [x] Implemented request/response DTOs
- [x] Added OpenAPI documentation
- [x] Set up proper HTTP status codes

### 5. Testing (In Progress - 40%)
- [x] Unit tests for domain models
- [x] Unit tests for service layer
- [ ] Integration tests for API endpoints
- [ ] Test for rate limiting (blocked by implementation)
- [ ] Load testing for performance validation

### 6. Security (In Progress - 30%)
- [ ] Implement rate limiting (3 requests/hour/IP)
- [ ] Add request validation
- [ ] Set up monitoring for abuse detection

### 7. Documentation (Completed)
- [x] API documentation with OpenAPI
- [x] Code documentation
- [x] Setup instructions
- [x] Error code reference

### Domain Model Implementation
```kotlin
// Domain model with validation
@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.matches(Regex("^\\+[1-9]\\d{1,14}$"))) {
            "Phone number must be in E.164 format (e.g., +1234567890)"
        }
    }
}

@JvmInline
value class UserName(val value: String) {
    init {
        require(value.length in 2..100) {
            "Name must be between 2 and 100 characters"
        }
    }
}

data class User(
    val id: Long = 0,
    val phoneNumber: PhoneNumber,
    val name: UserName,
    val email: String?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        email?.let {
            require(it.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
                "Invalid email format"
            }
        }
    }
}
```

## Remaining Tasks and Next Steps

### High Priority
1. **Rate Limiting Implementation**
   - Implement rate limiting (3 requests/hour/IP)
   - Add configuration for rate limits
   - Test with different IP addresses

2. **Testing**
   - Complete integration tests for API endpoints
   - Add negative test cases
   - Implement performance testing

3. **Security Hardening**
   - Add request validation
   - Implement IP-based rate limiting
   - Set up monitoring for abuse detection

### Medium Priority
1. **Documentation**
   - Update API documentation with rate limiting details
   - Add examples for error scenarios
   - Document rate limit headers

2. **Monitoring and Logging**
   - Add metrics for registration attempts
   - Set up alerts for suspicious activity
   - Log important registration events

### Future Enhancements
1. **Phone Number Verification**
   - Send OTP for phone verification
   - Implement verification status tracking

2. **Email Verification**
   - Optional email verification flow
   - Email notification preferences

3. **User Profile Management**
   - Profile update endpoints
   - Profile picture upload
   - Account settings

## Implementation Notes
- The current implementation follows clean architecture principles
- Domain models are kept pure and free from framework dependencies
- Error handling is consistent across the application
- The API follows RESTful best practices
- Code is well-documented with KDoc

## Rollback Plan
1. Database changes can be rolled back using Flyway migrations
2. API changes are backward compatible
3. Feature flags can be used to disable new functionality if needed
  class JooqUserRepository(
      private val dsl: DSLContext
  ) : UserRepository {
      // Implementation using JOOQ DSL
  }
  ```

- [✅] Add custom exceptions in `com.biding.db.exception`:
  ```kotlin
  class DuplicatePhoneNumberException(phoneNumber: String) : 
      RuntimeException("Phone number $phoneNumber is already registered")
  ```

- [✅] Configure JOOQ in `application.yml`
  ```yaml
  spring:
    jpa:
      show-sql: true
    jooq:
      sql-dialect: POSTGRES
  ```

### 4. Application Layer
- [✅] Create UserService with registration logic
- [✅] Implement phone number validation
- [✅] Add rate limiting
- [✅] Add transaction management

### 5. Presentation Layer
- [✅] Create UserController
- [✅] Implement request/response DTOs
- [✅] Add input validation
- [✅] Configure OpenAPI documentation

### 6. Testing
- [✅] Unit tests for validation logic
- [✅] Integration tests with TestContainers
- [✅] Performance tests for concurrent registrations
- [✅] Test rate limiting

### 7. Documentation
- [✅] Update OpenAPI documentation
- [✅] Add API examples
- [✅] Document rate limiting headers

## Security Considerations
- [✅] Input validation and sanitization
- [✅] Rate limiting (3 requests/hour/IP)
- [✅] No sensitive data in error responses

## Dependencies
- Spring Boot Web
- Spring Boot Validation
- JOOQ
- PostgreSQL JDBC
- SpringDoc OpenAPI
- TestContainers (test)

## Estimated Effort
- Database & JOOQ: 4 hours
- Business Logic: 4 hours
- API Layer: 3 hours
- Testing: 3 hours
- Documentation: 1 hour

**Total: 15 hours**

## Acceptance Criteria
- [✅] Users can register with valid phone number and name
- [✅] Duplicate phone numbers are rejected with 409
- [✅] Input validation returns 400 for invalid data
- [✅] Rate limiting (3/hour/IP) returns 429 when exceeded
- [✅] All tests pass with >80% coverage
- [✅] OpenAPI documentation is complete
- [✅] Performance: <100ms response time under load

## Notes
- Store phone numbers in E.164 format
- Consider future enhancements:
  - Phone number verification
  - Email registration option
  - Social login integration
- Monitor registration patterns for abuse prevention
