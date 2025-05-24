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
- **Build Tool:** Gradle 8.13
- **API Documentation:** OpenAPI 3.0.3

### Database & ORM
- **Database System:** PostgreSQL 15.13
- **ORM Framework:** JOOQ 3.19.3
  - Type-safe SQL queries
  - Generated DAOs for database access
  - Kotlin DSL support
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
  - `users` table with fields: id, phone_number, name, email, created_at, updated_at
  - Indexes on phone_number for fast lookups

### API Layer
- **RESTful Endpoints:**
  - `POST /api/v1/users/register` - Register a new user
- **Request/Response Formats:**
  - Request: JSON with phone_number, name, and optional email
  - Response: JSON with user details and timestamps
- **Validation:**
  - Phone number validation (E.164 format)
  - Name validation (2-100 characters)
  - Optional email validation
- **Error Handling:**
  - 400 Bad Request for validation errors
  - 409 Conflict for duplicate phone numbers
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
- `org.springframework.boot:spring-boot-starter-web` - Spring Web MVC
- `org.springframework.boot:spring-boot-starter-validation` - Bean Validation
- `org.springframework.boot:spring-boot-starter-jooq` - JOOQ integration
- `org.jooq:jooq:3.19.3` - JOOQ core library
- `org.postgresql:postgresql:42.6.0` - PostgreSQL JDBC driver
- `org.flywaydb:flyway-core:9.16.1` - Database migrations
- `com.fasterxml.jackson.module:jackson-module-kotlin` - Kotlin JSON support
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0` - OpenAPI documentation
- `org.jetbrains.kotlin:kotlin-reflect` - Kotlin reflection

#### Development Dependencies
- `org.springframework.boot:spring-boot-starter-test` - Testing support
- `io.mockk:mockk:1.13.4` - Mocking library for Kotlin
- `org.testcontainers:postgresql:1.17.6` - Test containers for integration tests
- `org.testcontainers:junit-jupiter:1.17.6` - JUnit 5 support for Testcontainers

## Development Environment
### Prerequisites
- Java 21 JDK (e.g., Amazon Corretto 21)
- Docker Desktop 4.15.0+
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
