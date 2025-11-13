# Shared Library

Common utilities, DTOs, entities, and configurations shared across all CursorPOS microservices.

## Contents

### Core Entities
- `BaseEntity` - Base class for all JPA entities with auditing and soft delete support

### Security
- `TenantContext` - Thread-local storage for tenant isolation
- `JwtUtil` - JWT token parsing and validation
- `JwtAuthenticationFilter` - JWT authentication filter for Spring Security
- `SecurityConfig` - Security configuration with stateless authentication

### DTOs
- `ApiResponse<T>` - Standard API response wrapper
- `PagedResponse<T>` - Paginated response for list endpoints

### Exceptions
- `CursorPosException` - Base exception class
- `ResourceNotFoundException` - 404 exception
- `TenantIsolationException` - Security violation exception
- `GlobalExceptionHandler` - Global exception handling for REST APIs

### Configuration
- `KafkaConfig` - Kafka producer/consumer configuration
- `JpaAuditingConfig` - JPA auditing configuration
- `SecurityConfig` - Spring Security configuration

### Events
- `BaseEvent` - Base class for Kafka events

### Utilities
- `AuditorAwareImpl` - JPA auditor provider
- `DateTimeUtil` - Date/time utility methods
- `ValidationUtil` - Common validation methods

## Usage

Add dependency in service `build.gradle`:

```gradle
dependencies {
    implementation project(':shared-lib')
}
```

## Multi-Tenant Support

All services using this library automatically get:
- Tenant context extraction from JWT tokens
- Automatic tenant ID injection for database queries
- Thread-local tenant isolation
- Audit trail support

## Security

JWT tokens must contain:
- `sub` - User ID
- `tenant_id` - Tenant identifier (required)
- `role` - User role
- `permissions` - List of permissions
- `store_id` - Store ID (optional)
- `branch_id` - Branch ID (optional)
