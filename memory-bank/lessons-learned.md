# Lessons Learned

**Created:** 2025-05-24  
**Last Updated:** 2025-05-24  
**Last Updated By:** Cascade AI Assistant  
**Related Components:** Database, ORM, Build Configuration, Spring Boot

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
