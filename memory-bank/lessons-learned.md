# Lessons Learned

**Created:** 2025-05-24  
**Last Updated:** 2025-05-24  
**Last Updated By:** Cascade AI Assistant  
**Related Components:** User Registration, API Design, Database, ORM, Validation, Spring Boot

## User Registration Implementation

### API Design
1. **DTO Pattern**
   - **Lesson:** Clear separation between API contracts and domain models improves maintainability
   - **Example:** Used separate DTOs for request/response in user registration
   - **Best Practice:** Always validate DTOs at the API boundary

2. **Validation**
   - **Lesson:** Jakarta Bean Validation provides powerful declarative validation
   - **Example:** Used `@Pattern` for phone number validation
   - **Best Practice:** Create custom validators for complex validation rules

3. **Error Handling**
   - **Lesson:** Consistent error responses improve client experience
   - **Example:** Implemented `@ControllerAdvice` for global exception handling
   - **Best Practice:** Include error codes and user-friendly messages

## Database & ORM Configuration

### Flyway Configuration
1. **Version Compatibility**
   - **Lesson:** Flyway 9.16.1 has compatibility issues with PostgreSQL 15.13
   - **Solution:** Temporarily disabled Flyway auto-configuration in `application.yml` and `@SpringBootApplication`
   - **Best Practice:** Always verify Flyway version compatibility with your PostgreSQL version before implementation

2. **Migration File Naming**
   - Always use the correct naming convention: `V{version}__{description}.sql`
   - Double underscores are required in the filename
   - Example: `V1__create_users_table.sql`

3. **Migration Location**
   - Default location is `src/main/resources/db/migration`
   - Can be customized in `build.gradle.kts` but requires explicit configuration

4. **Clean Operation**
   - Disable clean by default in production (`cleanDisabled = true`)
   - Always test migrations in a development environment first

### JOOQ Configuration
1. **Dependency Management**
   - Ensure the JOOQ version matches between the plugin and runtime dependencies
   - Add PostgreSQL JDBC driver to both runtime and JOOQ generator classpaths
   - **Lesson:** Use `implementation` for runtime dependencies and `jooqCodegen` for code generation dependencies
   - **Example:**
     ```kotlin
     dependencies {
         implementation("org.postgresql:postgresql:42.6.0")
         jooqCodegen("org.postgresql:postgresql:42.6.0")
     }
     ```

2. **Code Generation**
   - Run `./gradlew clean generateJooq` after schema changes
   - Generated code goes to `build/generated/jooq` by default
   - Configure the target package for generated code in `build.gradle.kts`

3. **Kotlin Support**
   - Enable Kotlin data classes with `isImmutablePojos = true`
   - Use `isFluentSetters = true` for better Kotlin integration

### Domain Modeling
1. **Rich Domain Models**
   - **Lesson:** Encapsulate business logic in domain objects
   - **Example:** Moved validation logic to the User domain class
   - **Best Practice:** Keep domain models free from infrastructure concerns

2. **Value Objects**
   - **Lesson:** Use value objects for domain concepts (e.g., PhoneNumber)
   - **Example:** Created PhoneNumber value object with validation
   - **Best Practice:** Make value objects immutable

### Build Configuration
1. **Gradle Setup**
   - Use the correct plugin version (we used `nu.studer.jooq` version `7.1`)
   - Configure JOOQ tasks in the `jooq` block
   - Ensure proper task dependencies (e.g., `generateJooq` should run after `flywayMigrate`)
   - **Lesson:** When Flyway is disabled, ensure database schema is manually created before JOOQ code generation
   - **Example:**
     ```kotlin
     tasks.named<org.jooq.meta.jaxb.Generate>("generateJooq") {
         // Disable Flyway dependency when Flyway is disabled
         if (!project.hasProperty("disableFlyway") || project.property("disableFlyway") != "true") {
             dependsOn("flywayMigrate")
         } else {
             logger.lifecycle("Skipping Flyway migration as it's disabled")
         }
     }
     ```

2. **Error Handling**
   - Common error: `ClassNotFoundException` for JDBC driver - ensure it's in the correct configuration
   - Check Gradle logs with `--info` or `--debug` for detailed error information

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

2. **Mocking**
   - **Lesson:** Use MockK for idiomatic Kotlin mocking
   - **Example:** Mocked repository in service tests
   - **Best Practice:** Focus on behavior, not implementation details

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

3. **Rate Limiting**
   - **Lesson:** Protect registration endpoints
   - **Example:** Planning to implement rate limiting
   - **Best Practice:** Use existing libraries (e.g., Spring Cloud Gateway)

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
