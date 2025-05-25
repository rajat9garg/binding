# Lessons Learned

**Created:** 2025-05-24  
**Last Updated:** 2025-05-24  
**Last Updated By:** Bidding Platform Team  
**Related Components:** Auction System, API Design, Database, ORM, Validation, Spring Boot, Real-time Bidding

## Auction System Implementation

### API Design
1. **RESTful Endpoints**
   - **Lesson:** Clear, consistent endpoint structure improves API usability
   - **Example:** `/api/v1/auctions/{id}/bids` for bid management
   - **Best Practice:** Follow REST conventions and use HTTP methods appropriately

2. **Real-time Bidding**
   - **Lesson:** WebSockets provide efficient real-time communication
   - **Example:** Implemented STOMP over WebSockets for bid updates
   - **Best Practice:** Handle connection drops and reconnection logic

3. **Validation**
   - **Lesson:** Domain-specific validation rules are crucial for auction integrity
   - **Example:** Minimum bid increment validation
   - **Best Practice:** Validate both at API and domain levels

4. **Idempotency**
   - **Lesson:** Critical for bid submission to prevent duplicate processing
   - **Example:** Implemented idempotency keys for bid requests
   - **Best Practice:** Use idempotency keys for all mutating operations

## Database & ORM Configuration

### JOOQ for Auction System
1. **Complex Query Handling**
   - **Lesson:** JOOQ excels at complex auction queries (e.g., active listings, bid history)
   - **Example:** Used JOOQ's DSL for time-based auction queries
   - **Best Practice:** Create reusable query components

2. **Type-Safe SQL**
   - **Lesson:** Catch SQL errors at compile time
   - **Example:** Generated Kotlin types for auction tables
   - **Best Practice:** Regenerate code after schema changes

3. **Transaction Management**
   - **Lesson:** Critical for bid processing
   - **Example:** `@Transactional` with proper isolation levels
   - **Best Practice:** Keep transactions short and focused

4. **JSONB Support**
   - **Lesson:** Store flexible auction metadata
   - **Example:** Item details as JSONB
   - **Best Practice:** Define JSON schemas for complex fields

### Performance Optimization
1. **Bid Processing**
   - **Lesson:** Optimistic locking for high concurrency
   - **Example:** Version column for bid updates
   - **Best Practice:** Handle `OptimisticLockException`

2. **Caching Strategy**
   - **Lesson:** Cache frequently accessed auction data
   - **Example:** Cached active auctions with TTL
   - **Best Practice:** Invalidate cache on state changes

3. **Database Indexing**
   - **Lesson:** Critical for auction queries
   - **Example:** Indexes on `end_time`, `status`
   - **Best Practice:** Monitor query performance

### Domain Modeling for Auctions
1. **Auction State Management**
   - **Lesson:** Explicit state machine prevents invalid transitions
   - **Example:** `DRAFT -> SCHEDULED -> ACTIVE -> COMPLETED/CANCELLED`
   - **Best Practice:** Use enums for state representation

2. **Value Objects**
   - **Lesson:** Strong typing for domain concepts
   - **Example:** `Money` for currency handling
   - **Best Practice:** Validate invariants in constructors

3. **Aggregate Roots**
   - **Lesson:** Clear boundaries for auction operations
   - **Example:** `Auction` as aggregate root for bids
   - **Best Practice:** Reference by ID across aggregates

### Build and Deployment
1. **CI/CD Pipeline**
   - **Lesson:** Automated testing is crucial
   - **Example:** GitHub Actions for build/test/deploy
   - **Best Practice:** Test database migrations

2. **Containerization**
   - **Lesson:** Consistent environments
   - **Example:** Multi-stage Docker builds
   - **Best Practice:** Small container images

3. **Configuration Management**
   - **Lesson:** Environment-specific configs
   - **Example:** Spring profiles
   - **Best Practice:** Externalize configuration

## Spring Boot Integration

### Transaction Management
1. **@Transactional Usage**
   - **Lesson:** Be explicit about transaction boundaries
   - **Example:** Used `@Transactional` at service layer
   - **Best Practice:** Keep transactions as short as possible

### Testing
1. **Test Containers**
   - **Lesson:** Use Testcontainers for integration tests
   - **Example:** Added PostgreSQL container for repository tests
   - **Best Practice:** Test against real database in integration tests

## Testing Strategies

### Unit Testing
1. **Domain Logic**
   - **Lesson:** Test domain rules in isolation
   - **Example:** Auction state transitions
   - **Best Practice:** Use property-based testing

2. **Mocking**
   - **Lesson:** Use MockK for idiomatic Kotlin mocking
   - **Example:** Mocked bid repository
   - **Best Practice:** Verify interactions

### Integration Testing
1. **Test Containers**
   - **Lesson:** Real database testing
   - **Example:** PostgreSQL test container
   - **Best Practice:** Reuse containers

2. **API Testing**
   - **Lesson:** Test API contracts
   - **Example:** REST Assured
   - **Best Practice:** Test error cases

## Timestamp Handling

### Database Timestamps
1. **Time Zone Handling**
   - **Lesson:** PostgreSQL stores timestamps in UTC by default but can return them in different formats
   - **Example:** Use `OffsetDateTime` for database operations and convert to/from `Instant` in the domain layer
   - **Best Practice:** Consistently handle time zones across the application

2. **Type Conversion**
   - **Lesson:** Explicitly handle type conversion between database and domain models
   - **Example:** Convert `OffsetDateTime` to `Instant` when mapping database records to domain objects
   - **Best Practice:** Centralize conversion logic in repository layer

3. **Default Values**
   - **Lesson:** Set default timestamps in the database or application code
   - **Example:** Use `java.time.OffsetDateTime.now()` for current timestamp in database operations
   - **Best Practice:** Be consistent with timestamp generation across the application

4. **Query Results**
   - **Lesson:** JOOQ may return different timestamp types based on database driver
   - **Example:** Always cast timestamp fields explicitly in queries
   - **Best Practice:** Test timestamp handling with real database queries

### Health Check Implementation
1. **Basic Health Check**
   - Implemented at `/api/v1/health`
   - Returns basic application status and timestamp
   - **Lesson:** Keep health checks lightweight and fast
   - **Improvement Needed:** Add database connectivity check

2. **Configuration Management**
   - Use `application.yml` for environment-specific configurations
   - **Lesson:** Externalize database configuration for different environments
   - **Example:**
     ```yaml
     spring:
       datasource:
         url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/biding}
         username: ${DATABASE_USER:postgres}
         password: ${DATABASE_PASSWORD:postgres}
     ```

## Best Practices

### API Design
1. **RESTful Principles**
   - Use proper HTTP methods and status codes
   - Example: 201 Created for successful registration
   - Include Location header for created resources

2. **Versioning**
   - Version your API from the start
   - Example: `/api/v1/users/register`
   - Plan for backward compatibility

### Database Design
1. **Schema Versioning**
   - Always use Flyway for schema changes
   - Never modify production schema directly
   - Test migrations thoroughly before deployment

2. **Naming Conventions**
   - Use snake_case for database identifiers
   - Be consistent with naming across the application
   - Document any naming conventions in the project documentation

### Development Workflow
1. **Local Development**
   - Use Docker for consistent database environments
   - Document all required environment variables
   - Include database initialization in the project setup guide

2. **Code Organization**
   - Keep migration files organized by feature or component
   - Document database schema decisions in the codebase
   - Use meaningful commit messages for database changes

## Common Pitfalls & Solutions

1. **Flyway Migration Issues**
   - Problem: Migrations not found
     - Solution: Check the `locations` configuration in `build.gradle.kts`
   - Problem: Migration checksum mismatch
     - Solution: Never modify applied migrations, create a new one instead

2. **JOOQ Code Generation**
   - Problem: Missing tables in generated code
     - Solution: Check `includes`/`excludes` patterns in JOOQ configuration
   - Problem: Type mismatches
     - Solution: Configure custom data type bindings if needed

## Performance Considerations

1. **Database Indexing**
   - **Lesson:** Indexes are crucial for query performance
   - **Example:** Added index on `phone_number` for fast lookups
   - **Best Practice:** Analyze query patterns and add indexes accordingly

2. **Connection Pooling**
   - **Lesson:** Proper connection pool configuration is essential
   - **Example:** Configured HikariCP with optimal pool size
   - **Best Practice:** Monitor connection pool metrics in production

3. **DTO Projection**
   - **Lesson:** Select only needed columns in queries
   - **Example:** Used JOOQ's `select()` to specify fields
   - **Best Practice:** Avoid `select *` in production queries

## Security Considerations

1. **Input Validation**
   - **Lesson:** Validate all user inputs
   - **Example:** Used Bean Validation with custom constraints
   - **Best Practice:** Validate early, validate often

2. **Error Messages**
   - **Lesson:** Be careful with error messages
   - **Example:** Generic messages in production, detailed in dev
   - **Best Practice:** Don't leak system details in error responses

## Production Readiness

### Monitoring and Observability
1. **Metrics**
   - **Lesson:** Track key auction metrics
   - **Example:** Bids per second
   - **Best Practice:** Use Micrometer

2. **Logging**
   - **Lesson:** Structured logging
   - **Example:** JSON logs with correlation IDs
   - **Best Practice:** Include request context

3. **Rate Limiting**
   - **Lesson:** Protect bid submission
   - **Example:** Redis-based rate limiting
   - **Best Practice:** Gradual rollout

### Security
1. **Authentication**
   - **Lesson:** JWT for API auth
   - **Example:** Role-based access
   - **Best Practice:** Short-lived tokens

2. **Data Protection**
   - **Lesson:** Encrypt sensitive data
   - **Example:** PII encryption
   - **Best Practice:** Regular audits

3. **Build Configuration**
   - Problem: Build fails with configuration errors
     - Solution: Check for syntax errors in `build.gradle.kts`
   - Problem: Inconsistent dependency versions
     - Solution: Use Gradle's dependency constraints or BOMs

## Recommendations

1. **Documentation**
   - Document all database schema decisions
   - Keep an up-to-date ER diagram
   - Document any non-obvious JOOQ usage patterns

2. **Testing**
   - Write integration tests for database operations
   - Test migrations in a CI/CD pipeline
   - Include database state in test fixtures

3. **Performance**
   - Monitor query performance
   - Add appropriate indexes
   - Consider connection pooling configuration

---
*This document will be updated as new lessons are learned throughout the project.*
