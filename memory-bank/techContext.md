# Technical Context

**Created:** 2025-05-24  
**Status:** ACTIVE  
**Author:** Cascade AI Assistant  
**Last Modified:** 2025-05-25
**Last Updated By:** Cascade AI Assistant

## Table of Contents
- [Technology Stack](#technology-stack)
- [Development Environment](#development-environment)
- [Build & Deployment](#build--deployment)
- [Infrastructure](#infrastructure)
- [Development Workflow](#development-workflow)
- [Testing Strategy](#testing-strategy)
- [Monitoring & Logging](#monitoring--logging)
- [Security Considerations](#security-considerations)

## Technology Stack
### Core Technologies
- **Backend Framework:** Spring Boot 3.5.0 (with WebMVC)
- **Database:** PostgreSQL 15.13
- **Programming Languages:** Kotlin 1.9.25 (JVM 21)
- **Build Tool:** Gradle 8.13 (Kotlin DSL)
- **API Documentation:** OpenAPI 3.0.3 with SpringDoc OpenAPI
- **Code Generation:** OpenAPI Generator 6.6.0
- **Containerization:** Docker 24.0.7
- **CI/CD:** GitHub Actions
- **Dependency Management:** Gradle Version Catalog

### Database & ORM
- **Database System:** PostgreSQL 15.13
  - Connection Pool: HikariCP 5.1.0
  - Max Pool Size: 10
  - Connection Timeout: 30s
  - Idle Timeout: 10m

- **ORM Framework:** JOOQ 3.19.3
  - Type-safe SQL queries with Kotlin DSL
  - Generated DAOs and POJOs
  - Custom type bindings for Kotlin types
  - **Timestamp Handling:**
    - Uses `OffsetDateTime` for all database operations
    - Converts to/from domain `Instant` in repository layer
    - Explicit type casting for timestamp fields
    - Consistent timezone handling (UTC)

- **Database Migration:** Flyway 9.16.1
  - Migration Location: `src/main/resources/db/migration`
  - Schema: `public`
  - Migration Table: `flyway_schema_history`
  - Clean Disabled: `true` (safety measure)
  - Baseline on Migrate: `true`
  - Baseline Version: `1`
  - Encoding: `UTF-8`
  - Placeholder Replacement: `true`

- **Database Schema:**
  - `users` table:
    - `id`: BIGSERIAL PRIMARY KEY
    - `phone_number`: VARCHAR(15) UNIQUE NOT NULL
    - `name`: VARCHAR(100) NOT NULL
    - `email`: VARCHAR(255)
    - `created_at`: TIMESTAMP WITH TIME ZONE NOT NULL
    - `updated_at`: TIMESTAMP WITH TIME ZONE NOT NULL
  - `items` table: Core auction items
  - `bids` table: Tracks all bids placed on items
  - **Indexes:**
    - `idx_users_phone`: `users(phone_number)`
    - `idx_users_email`: `users(email)` WHERE email IS NOT NULL

### API Layer
- **RESTful Endpoints:**
  - `GET /api/v1/items` - List all auction items (paginated)
  - `GET /api/v1/items/{id}` - Get item details
  - `POST /api/v1/items` - Create new auction item
  - `PUT /api/v1/items/{id}` - Update auction item
  - `DELETE /api/v1/items/{id}` - Delete auction item
  - `POST /api/v1/items/{id}/bids` - Place a bid

- **Request/Response Formats:**
  - Request/Response: JSON with appropriate DTOs
  - Pagination support with `page`, `size`, and `sort` parameters
  - Consistent error response format

- **Validation:**
  - Bean Validation (JSR-380)
  - Custom validators for business rules
  - Input sanitization

- **Error Handling:**
  - Global exception handler with consistent error responses
  - 400 Bad Request for validation errors
  - 404 Not Found for non-existent resources
  - 409 Conflict for business rule violations
  - 500 Internal Server Error for unexpected errors

### Important Notes
- **Flyway Compatibility:** Configured to work with PostgreSQL 15.13
- **Health Check Endpoint:** Implemented at `/api/v1/health` with database connectivity check
- **OpenAPI Documentation:** Available at `/v3/api-docs` and `/swagger-ui.html`

#### JOOQ Configuration
- **Version:** 3.19.3 (OSS Edition)
- **Generated Code Location:** `build/generated/jooq`
- **Target Package:** `com.biding.infrastructure.jooq`
- **Key Features:**
  - Record Generation
  - Immutable POJOs
  - Fluent Setters
  - Kotlin Data Classes
  - Java Time Types
  - Custom data type bindings for Kotlin types

### Dependencies
#### Runtime Dependencies
- **Spring Boot Starters**
  - `spring-boot-starter-web` - Web MVC support
  - `spring-boot-starter-validation` - Bean Validation
  - `spring-boot-starter-jooq` - JOOQ integration
  - `spring-boot-starter-data-jpa` - JPA support (for repository abstraction)

- **Database**
  - `jooq` - Type-safe SQL queries
  - `postgresql` - PostgreSQL JDBC driver
  - `flyway-core` - Database migrations
  - `h2` - In-memory database for tests

- **Kotlin**
  - `kotlin-stdlib-jdk8`
  - `kotlin-reflect`
  - `jackson-module-kotlin` - JSON support

- **API Documentation**
  - `springdoc-openapi-starter-webmvc-ui` - OpenAPI 3.0 documentation
  - `springdoc-openapi-kotlin` - Kotlin support for OpenAPI

- **Utilities**
  - `hibernate-types-60` - Custom Hibernate types
  - `kotlin-logging` - Kotlin logging utilities

#### Development Dependencies
- **Testing**
  - `spring-boot-starter-test` - Core testing support
  - `mockk` - Mocking library for Kotlin
  - `testcontainers` - Container-based testing
  - `junit-jupiter` - JUnit 5 support
  - `kotlin-test` - Kotlin test utilities

- **Code Quality**
  - `detekt` - Static code analysis
  - `ktlint` - Kotlin linter
  - `jacoco` - Code coverage

- **Build Tools**
  - `openapi-generator-gradle-plugin` - API client generation
  - `jooq-codegen` - JOOQ code generation
  - `spring-boot-gradle-plugin` - Spring Boot support

## Development Environment
### Prerequisites
- **Java 21 JDK** (Amazon Corretto 21 recommended)
- **Docker Desktop** 4.15.0+
- **Gradle** 8.13 (included in wrapper)
- **PostgreSQL** 15.13 (via Docker)
- **IntelliJ IDEA** (recommended) or VS Code with Kotlin plugin

### Development Setup
1. **Database**
   - Run PostgreSQL via Docker: `docker-compose up -d postgres`
   - Database will be available at `localhost:5432`
   - Default credentials: postgres/postgres

2. **Build & Run**
   - Build project: `./gradlew clean build`
   - Run application: `./gradlew bootRun`
   - Run tests: `./gradlew test`
   - Generate JOOQ classes: `./gradlew generateJooq`
   - Generate OpenAPI clients: `./gradlew openApiGenerate`

3. **Code Quality**
   - Lint: `./gradlew ktlintCheck`
   - Static analysis: `./gradlew detekt`
   - Test coverage: `./gradlew jacocoTestReport`

4. **API Documentation**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs
   - Actuator: http://localhost:8080/actuator
- Gradle 8.13
- IntelliJ IDEA or VS Code with Kotlin plugin
- PostgreSQL 15.13 (or Docker)

### Setup Instructions
1. Clone the repository
2. Install dependencies:
   ```bash
   [installation command]
   ```
3. Configure environment variables (copy .env.example to .env)
4. Start development server:
   ```bash
   [start command]
   ```

### Configuration
#### Environment Variables
```
# Application
NODE_ENV=development
PORT=3000

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=myapp_dev
DB_USER=user
DB_PASSWORD=password

# External Services
API_KEY=your_api_key
```

## Build & Deployment
### Build Process
```bash
[build command]
```

### Deployment
#### Staging
```bash
[staging deployment command]
```

#### Production
```bash
[production deployment command]
```

### CI/CD Pipeline
- **Build:** [Build configuration]
- **Test:** [Test configuration]
- **Deploy:** [Deployment configuration]

## Infrastructure
### Development
- [Local development setup]
- [Development services]

### Staging/Production
- [Hosting platform]
- [Server specifications]
- [Database hosting]
- [CDN/Caching]

## Development Workflow
### Branching Strategy
[Description of git workflow, e.g., Git Flow, GitHub Flow]

### Code Review Process
1. Create feature branch from `main`
2. Make changes and commit with descriptive messages
3. Push and create pull request
4. Address review comments
5. Merge after approval

### Versioning
[Versioning strategy, e.g., Semantic Versioning]

## Testing Strategy
### Unit Tests
- **Framework:** JUnit 5 with Kotlin support
- **Mocking:** MockK for mocking dependencies
- **Assertions:** AssertJ for fluent assertions
- **Coverage:** Aim for 80%+ code coverage
- **Location:** `src/test/kotlin`
- **Naming:** 
  - Test classes: `{ClassName}Test`
  - Test methods: `should {expected behavior} when {condition}`

#### Example Unit Test
```kotlin
@Test
fun `should save user when phone number is unique`() {
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
```

## Testing Strategy

### Unit Testing
- **Framework:** JUnit 5 + Kotest
- **Mocking:** MockK
- **Assertions:** AssertJ
- **Coverage:** JaCoCo (min 80% coverage)
- **Best Practices:**
  - Follow AAA pattern (Arrange-Act-Assert)
  - One assertion per test (when possible)
  - Clear, descriptive test method names
  - Test edge cases and error conditions
  - Use test data builders for complex objects

### Integration Testing
- **Framework:** Spring Boot Test
- **Database:** Testcontainers with PostgreSQL
- **HTTP Client:** TestRestTemplate
- **Test Slices:** 
  - `@WebMvcTest` for controller layer
  - `@DataJpaTest` for repository layer
  - `@JsonTest` for JSON serialization
  - `@RestClientTest` for REST clients
- **Test Data:**
  - Use `@Sql` for database setup
  - Implement `TestExecutionListener` for custom setup/teardown
  - Use `@DynamicPropertySource` for dynamic properties

### E2E Testing
- **Tools:** Testcontainers + REST Assured
- **Scope:** Complete user flows
- **Environment:** Isolated test environment with:
  - Application container
  - Database container
  - Any required external services
- **Test Data:**
  - Each test manages its own data
  - Use `@Testcontainers` for container lifecycle
  - Implement test data factories

### Performance Testing
- **Tools:** JMeter, Gatling, or k6
- **Scenarios:**
  - Load testing
  - Stress testing
  - Endurance testing
- **Metrics:**
  - Response times
  - Throughput
  - Error rates
  - Resource utilization

### Test Dependencies
```kotlin
// Core Testing
testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
}
testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

// Mocking
testImplementation("io.mockk:mockk:1.13.11")
testImplementation("io.mockk:mockk-agent-jvm:1.13.11")

// Assertions
testImplementation("org.assertj:assertj-core:3.25.3")
testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")

// Testcontainers
testImplementation("org.testcontainers:postgresql:1.19.3")
testImplementation("org.testcontainers:junit-jupiter:1.19.3")
testImplementation("org.testcontainers:testcontainers:1.19.3")

// API Testing
testImplementation("io.rest-assured:rest-assured:5.4.0")
testImplementation("io.rest-assured:kotlin-extensions:5.4.0")

// Test Fixtures
testImplementation("com.appmattus.fixture:fixture:1.2.0")

// Code Coverage
testImplementation("org.jacoco:org.jacoco.agent:0.8.11")

// Property-based Testing
testImplementation("io.kotest:kotest-property-jvm:5.8.0")
```

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.biding.services.impl.UserServiceImplTest"

# Run integration tests
./gradlew integrationTest

# Run tests with coverage report
./gradlew test jacocoTestReport

# Run performance tests
./gradlew performanceTest

# Run mutation testing
./gradlew pitest
```

### Best Practices
1. **Test Structure**
   - Follow Arrange-Act-Assert pattern
   - Use descriptive test names (e.g., `shouldReturnUserWhenValidPhoneNumber`)
   - Test one behavior per test method
   - Keep tests independent and isolated
   - Use nested classes to group related tests

2. **Test Data**
   - Use builders for complex objects
   - Implement test data factories
   - Use random data generation for property-based testing
   - Clean up test data after each test

3. **Mocking**
   - Mock external dependencies
   - Verify interactions with mocks
   - Use argument captors for complex verifications
   - Prefer relaxed mocks for optional dependencies

4. **Assertions**
   - Use AssertJ's fluent assertions
   - Add descriptive messages to assertions
   - Verify both happy path and error cases
   - Use custom assertions for domain objects

5. **Test Performance**
   - Keep tests fast (unit tests < 100ms)
   - Use `@DirtiesContext` sparingly
   - Reuse test containers between tests
   - Run tests in parallel when possible

6. **Code Coverage**
   - Aim for 80%+ line coverage
   - Focus on business logic coverage
   - Use mutation testing to improve test quality
   - Review coverage reports regularly

2. **Mocking**
   - Use MockK for all mocking needs
   - Verify interactions with mocks
   - Keep mock setup clear and concise

3. **Assertions**
   - Use AssertJ for fluent assertions
   - Verify both happy path and error cases
   - Include meaningful assertion messages

## Monitoring & Logging
### Application Logs
- [Log levels]
- [Log aggregation]
- [Retention policy]

### Performance Monitoring
- [Monitoring tools]
- [Key metrics]
- [Alerting thresholds]

## Security Considerations
### Authentication
- [Authentication mechanism]
- [Session management]

### Data Protection
- [Encryption methods]
- [Data retention policy]

### Dependencies
- [Dependency scanning]
- [Vulnerability management]
