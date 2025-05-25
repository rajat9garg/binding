# Technical Context

**Created:** 2025-05-24  
**Status:** [ACTIVE]  
**Author:** Cascade AI Assistant  
**Last Modified:** 2025-05-24
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
- **Backend Framework:** Spring Boot 3.5.0
- **Database:** PostgreSQL 15.13
- **Programming Languages:** Kotlin 1.9.25, Java 21
- **Build Tool:** Gradle 8.13 (Kotlin DSL)
- **API Documentation:** OpenAPI 3.0.3
- **Code Generation:** OpenAPI Generator 6.6.0
- **Dependency Management:** Gradle Version Catalog

### Database & ORM
- **Database System:** PostgreSQL 15.13
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
- **Database Schema:**
  - `items` table: Core auction items with fields for title, description, status, etc.
  - `bids` table: Tracks all bids placed on items
  - `users` table: User management
  - Appropriate indexes and foreign key constraints

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
- [Testing framework]
- [Coverage requirements]
- [Mocking strategy]

### Integration Tests
- [Testing approach]
- [Test data management]

### E2E Tests
- [Tools and frameworks]
- [Test scenarios]

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
