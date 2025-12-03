# GitHub Copilot Instructions for CursorPOS Project

## Project Overview
CursorPOS is a multi-tenant Point of Sale system with microservices architecture, built with Spring Boot 3.5.7, Java 21, PostgreSQL, and Kafka.

---

## ⚠️ Background Process Protocol

### When a long-running or background process starts (example: `bootRun`, `docker-compose up`, `npm start`, Gradle daemon, migrations, tests, builds, servers, or any `isBackground=true` command lasting more than 10 seconds):
1. **ALWAYS check for port conflicts first** using `netstat -ano | Select-String "<port>" | Select-String "LISTENING"`
2. **ALWAYS start services in an external terminal window (popup)** using `Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd <path>; <command>"`
3. **NEVER use VS Code's integrated PowerShell terminal for running services** - only use popup windows
4. **ALWAYS start services in a dedicated terminal** - never reuse terminals running other services
5. Pause all other activity and ask the user:
   > "The process is starting. Please monitor the terminal and let me know when it finishes or if any errors occur."
6. Do not execute any further commands (curl, tests, health checks, docker probes) while waiting.
7. Resume only after the user explicitly confirms the process completed successfully.

### Additional safeguards
- Avoid `Start-Sleep` calls longer than 20 seconds when running foreground commands.
- Never poll logs, Docker status, or service endpoints while a background process is still active.
- Do not assume readiness when logs report `EXECUTING` or `RUNNING`; wait for the user's go-ahead.
- **CRITICAL**: Always use a new/separate terminal for testing endpoints when a service is running in another terminal
- **CRITICAL**: Each service must run in its own dedicated PowerShell terminal - never run multiple processes in the same terminal to avoid accidentally stopping services
- When you need to run another command (curl, netstat, build, etc.) while a service is running, ALWAYS open a new PowerShell terminal

### Examples
- ❌ Wrong: Start `bootRun`, immediately run `curl`, then sleep 30s.
- ❌ Wrong: Start `bootRun` in terminal A, then run `curl` in the same terminal A.
- ❌ Wrong: Start service without checking if port is already in use.
- ❌ Wrong: Start service in VS Code's integrated PowerShell terminal.
- ❌ Wrong: Start service in PowerShell A, then run another command in the same PowerShell A (accidentally stops the service).
- ✅ Right: Check port with `netstat`, start `bootRun` in external popup terminal (or dedicated VS Code PowerShell), read initial logs, ask for confirmation, then use a DIFFERENT PowerShell terminal for `curl` tests.

---

## Code Editing Rules
1. **replace_string_in_file**: Provide 3-5 lines of unchanged code before and after the target snippet for clarity.
2. **multi_replace_string_in_file**: Use it when replacing multiple independent regions; do not mention the tool name in your response.
3. **Documentation**: Only add new markdown files when the user explicitly requests documentation or summaries.

---

## Technology Stack
### Backend Services
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Build Tool**: Gradle 8.5
- **Database**: PostgreSQL 17
- **Migration**: Flyway
- **Messaging**: Apache Kafka
- **Cache**: Redis
- **Auth**: JWT + Keycloak

### Service Architecture
- **Identity Service**: Port 8081 (User/Tenant management)
- **Admin Service**: Port 8082
- **Product Service**: Port 8083 (Catalog, Inventory, Pricing)
- **Transaction Service**: Port 8084
- **API Gateway**: Port 8080

### Multi-Tenancy Pattern
- Row-level isolation with `tenant_id VARCHAR(100)`
- Tenant context propagated via JWT claims
- Separate Flyway schema history per service

### Common Patterns
- Shared `BaseEntity` with audit fields (`created_at`, `updated_at`, `created_by`, `updated_by`, `deleted_at`, `version`)
- MapStruct for DTO ↔ entity translation
- JPA repositories with custom queries
- Event-driven Kafka topics

### API Documentation
- **Static HTML Documentation Approach**
- API documentation is maintained as static HTML files in `docs/api/` directory
- Each service has its own HTML file (e.g., `identity-service-api.html`, `product-service-api.html`)
- HTML files include:
  - All endpoint definitions with HTTP methods and paths
  - Request/response examples with JSON payloads
  - Status codes and error responses
  - Authentication requirements
  - Query parameters and path variables
- Controllers use standard Javadoc comments for code documentation
- No runtime documentation dependencies (Swagger/OpenAPI removed from project)
- **MUST generate/update API documentation** for every service created or modified

---

## 🎯 Service Implementation Workflow

### MANDATORY: Every Service Creation/Update MUST Include

When creating or updating ANY service, you MUST complete ALL four requirements:

#### 1. **Unit Tests - 100% Coverage Required** ✅
- Test ALL service/business logic classes in isolation
- Mock all dependencies (repositories, external services)
- Cover all branches, edge cases, and error conditions
- Use `@ExtendWith(MockitoExtension.class)`
- Target: 100% line and branch coverage for service layer

#### 2. **Integration Tests - 100% Coverage Required** ✅
- Test ALL controller/API endpoints with real dependencies
- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Test with actual database (PostgreSQL via Docker)
- Verify HTTP responses, status codes, and data persistence
- Test multi-tenant isolation
- Target: 100% line and branch coverage for controller layer

#### 3. **End-to-End Tests - 100% Coverage Required** ✅ (if applicable)
- Test complete user workflows across multiple services
- Cover critical business scenarios (e.g., checkout flow, order processing)
- Test service-to-service communication
- Verify data consistency across services
- Required for services that interact with other services
- Target: 100% coverage of integration points

#### 4. **API Documentation - Complete** ✅
- Create or update static HTML file in `docs/api/{service-name}-api.html`
- Document ALL endpoints with:
  - HTTP method and path
  - Request body/parameters with types
  - Response examples (success and error)
  - Authentication requirements
  - Status codes
- Ensure navigation doesn't use `position: sticky` (causes overlay issues)
- Update generation date

### Implementation Checklist
Before marking a service as "complete", verify:
- [ ] All unit tests written and passing (100% coverage)
- [ ] All integration tests written and passing (100% coverage)
- [ ] E2E tests written if service integrates with others (100% coverage)
- [ ] API documentation generated/updated with all endpoints
- [ ] Coverage report generated: `./gradlew test jacocoTestReport`
- [ ] Coverage verified: `build/reports/jacoco/test/html/index.html`
- [ ] All tests run successfully: `./gradlew test`
- [ ] Build succeeds: `./gradlew build`

### Non-Negotiable Rules
- ❌ **NEVER** mark a service as complete without all 4 requirements
- ❌ **NEVER** commit code with less than 100% test coverage
- ❌ **NEVER** skip API documentation for any endpoint
- ✅ **ALWAYS** run coverage reports after writing tests
- ✅ **ALWAYS** fix failing tests before measuring coverage

### Logging Configuration
- **ALWAYS implement file logging when creating or updating a service**
- Create `logback-spring.xml` in `src/main/resources/` with:
  - Console appender for real-time monitoring
  - File appender for all logs (`./logs/{service-name}.log`)
  - Error file appender for errors only (`./logs/{service-name}-error.log`)
  - Rolling policy: 10MB per file, 30 days retention, 1GB total cap
  - DEBUG level for `com.cursorpos` package
- Add logging configuration in `application.yml`:
  ```yaml
  logging:
    file:
      path: ./logs
      name: ./logs/{service-name}.log
  ```
- Log files enable debugging production issues without accessing console output
- Use pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

---

## Database Conventions
### Naming
- Tables and columns use lowercase with underscores (e.g., `stock_movements`, `tenant_id`).
- Primary keys are `id UUID`; foreign keys follow `{entity}_id` (e.g., `product_id`).

### Required Columns (BaseEntity pattern)
```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
tenant_id VARCHAR(100) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by VARCHAR(100) NOT NULL,
updated_by VARCHAR(100),
deleted_at TIMESTAMP,
version BIGINT DEFAULT 0
```

### Flyway Versioning
- Identity Service: V1, V2, V3...
- Product Service: V10, V11, V12... to avoid conflicts
- Each service tracks migration history in `flyway_schema_history_{service}`

---

## Testing Strategy
### ⚠️ CRITICAL REQUIREMENT: 100% Test Coverage

#### Definition of Test Coverage
**Test Coverage = the percentage of source code (lines, branches, statements) executed by tests**, regardless of whether tests pass or fail.

- **Line Coverage**: Percentage of executable code lines executed during test runs
- **Branch Coverage**: Percentage of decision branches (if/else, switch, etc.) executed
- **Statement Coverage**: Percentage of individual statements executed
- **Coverage is NOT test pass rate** - it measures code completeness, not test success

Example: If `AuthService.java` has 100 lines and tests execute 97 lines, that's 97% line coverage, even if some tests fail.

#### Coverage Requirements
- **ALL services MUST achieve 100% code coverage** for:
  - **Unit Tests**: Test all service/business logic classes in isolation
  - **Integration Tests**: Test all controller/API endpoint classes with real dependencies
  - **End-to-End Tests**: Test complete user workflows across multiple services
- Each test type must independently cover 100% of its target code
- Unit tests cover service layer (business logic)
- Integration tests cover controller layer (API endpoints)
- E2E tests cover cross-service scenarios (user journeys)
- Run coverage with: `./gradlew test jacocoTestReport`
- Coverage reports: `build/reports/jacoco/test/html/index.html`
- **Never commit code with less than 100% coverage**
- Fix test failures FIRST before measuring coverage - failed tests produce inaccurate coverage metrics

### Unit Tests
- Use `@ExtendWith(MockitoExtension.class)` for unit tests
- Mock all repositories, services, and external dependencies with `@Mock` and `@InjectMocks`
- Test all business logic branches, edge cases, and error conditions
- Verify method calls with `verify()` and assertions with `assertThat()`
- Use `@Captor` for capturing method arguments when needed
- Test exception handling with `assertThrows()`

### Integration Tests
- Use `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`
- Drive APIs with `TestRestTemplate`
- Requires PostgreSQL running (typically via Docker)
- Clean up data after every test run with `@Transactional` or manual cleanup
- Isolate with unique tenants per test (e.g., `tenant-product-test-001`)
- Test multi-tenant data isolation
- Verify HTTP status codes, response bodies, and database state

### Test Data Guidelines
- Use realistic UUIDs for identifiers
- Follow tenant pattern `tenant-{domain}-{number}` (e.g., `tenant-coffee-001`)
- Cover positive, negative, and boundary scenarios
- Test validation errors, authentication failures, and authorization checks
- Test concurrent operations and race conditions where applicable

---

## Kafka Configuration
Kafka is disabled in Product Service development to avoid port conflicts with Kafka Connect.

To enable Kafka:
1. Set `spring.kafka.enabled: true` in `application.yml`
2. Ensure Kafka listens on port 9092 and is reachable
3. Configure producers and consumers as required

---

## Common Issues & Solutions
### Schema Validation Errors
- Align JPA entities with Flyway migration columns and types
- Include all BaseEntity columns in the DDL
- Update indexes when column names change

### MapStruct Implementation Missing?
- Run `./gradlew clean build`
- Verify `@Mapper(componentModel = "spring")` and MapStruct processor settings
- Keep `lombok-mapstruct-binding` dependency present

### Port Conflicts
- Product Service and Kafka Connect both default to 8083—do not run them simultaneously
- Use `docker ps` to confirm ports before launching services

---

## Project Structure
```
service-pos/
├── shared-lib/           # Shared utilities, BaseEntity, security
├── identity-service/     # Port 8081, Flyway V1-V9
├── admin-service/        # Port 8082
├── product-service/      # Port 8083, Flyway V10+
├── transaction-service/  # Port 8084
└── api-gateway/          # Port 8080
```

---

**Remember**: Wait for explicit user confirmation before continuing after any background process starts.
