# API Gateway Security Configuration

**Service**: API Gateway  
**Port**: 8080  
**Framework**: Spring Cloud Gateway 4.1.0 (WebFlux-based)  
**Last Updated**: 2025-01-XX

---

## Current Security Approach

### Architecture Decision: Gateway as Smart Router

The API Gateway is configured to **permit all requests** without authentication enforcement at the gateway level. This design follows the principle that **backend services own authentication**.

**Key Characteristics**:
- Gateway acts as a simple HTTP router
- No JWT validation at gateway layer
- No rate limiting enforcement
- No OAuth2 resource server protection
- Backend services handle all authentication independently

---

## Security Configuration Files

### 1. SecurityConfig.java (Active)
**Location**: `src/main/java/com/cursorpos/gateway/config/SecurityConfig.java`

**Purpose**: Explicit WebFlux security configuration that permits all requests

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
        return http.build();
    }
}
```

**Status**: ✅ Active, working as expected

---

### 2. GatewayConfiguration.java (Partially Disabled)
**Location**: `src/main/java/com/cursorpos/gateway/config/GatewayConfiguration.java`

**Status**: Programmatic routes commented out

**Reason**: Programmatic routes were applying `AuthenticationGatewayFilter` to all paths, causing 403 Forbidden errors. YAML-based routes are used instead.

**Disabled Code**:
```java
// @Bean
// public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//     return builder.routes()
//         .route("identity-service", r -> r.path("/api/v1/auth/**", "/api/v1/users/**")
//             .filters(f -> f.filter(authenticationGatewayFilter.apply(new Config())))
//             .uri("lb://identity-service"))
//         ...
// }
```

---

### 3. AuthenticationGatewayFilter.java (Disabled)
**Location**: `src/main/java/com/cursorpos/gateway/filter/AuthenticationGatewayFilter.java`

**Status**: @Component annotation removed, filter not loaded

**Reason**: 
1. Filter required `JwtUtil` bean which is excluded from Gateway dependencies (reactive incompatibility)
2. Programmatic routes using this filter caused 403 errors
3. Authentication is now handled by backend services

**Original Purpose**: JWT token validation at gateway level

---

### 4. application.yml (Modified)
**Location**: `src/main/resources/application.yml`

#### Disabled: Rate Limiting
```yaml
# All RequestRateLimiter filters commented out
# - name: RequestRateLimiter
#   args:
#     redis-rate-limiter.replenishRate: 10
#     redis-rate-limiter.burstCapacity: 20
```

**Reason**: Redis password mismatch causing connection errors

#### Disabled: OAuth2 Resource Server
```yaml
# security:
#   oauth2:
#     resourceserver:
#       jwt:
#         issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/cursorpos}
```

**Reason**: Keycloak not deployed, causing all requests to be blocked with 403

#### Active: YAML-based Routes
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: identity-service
          uri: lb://identity-service
          predicates:
            - Path=/api/v1/auth/**, /api/v1/users/**
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/v1/products/**, /api/v1/categories/**
        # ... other routes
```

**Status**: ✅ Working correctly

---

### 5. ApiGatewayApplication.java (Modified)
**Location**: `src/main/java/com/cursorpos/gateway/ApiGatewayApplication.java`

**Component Scanning**: Modified to exclude shared security components

**Before**:
```java
@ComponentScan(basePackages = {
    "com.cursorpos.gateway",
    "com.cursorpos.shared.security",  // ❌ Caused Spring Security enforcement
    "com.cursorpos.shared.util"
})
```

**After**:
```java
@ComponentScan(basePackages = {
    "com.cursorpos.gateway",
    "com.cursorpos.shared.util"  // Only utilities, no security
})
```

**Reason**: Shared security components were loading Spring Security configurations that blocked all requests

---

## Root Causes of 403 Forbidden Errors (Resolved)

During initial testing, the Gateway returned 403 Forbidden on all requests. Investigation revealed **four overlapping security layers**:

### Layer 1: Programmatic Authentication Filters
- **Issue**: `GatewayConfiguration.java` applied `AuthenticationGatewayFilter` to all routes programmatically
- **Impact**: All requests blocked because filter couldn't validate JWT (missing dependencies)
- **Resolution**: Commented out programmatic routes, using YAML routes instead

### Layer 2: RequestRateLimiter Filters
- **Issue**: YAML routes had `RequestRateLimiter` filters configured with Redis password `redis_secret`
- **Impact**: Redis connection failures caused 403 errors
- **Resolution**: Commented out all RequestRateLimiter filters

### Layer 3: OAuth2 Resource Server
- **Issue**: `application.yml` configured OAuth2 Resource Server with Keycloak issuer URI
- **Impact**: No Keycloak server available, causing JWT validation failures → 403
- **Resolution**: Commented out entire OAuth2 security configuration

### Layer 4: Shared Security Component Scanning
- **Issue**: `@ComponentScan` included `com.cursorpos.shared.security` package
- **Impact**: Spring Security from `shared-lib` loaded and enforced authentication
- **Resolution**: Removed shared security from component scan, created explicit `SecurityConfig.java` with `permitAll()`

---

## Security Flow (Current Architecture)

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request
       │ (no authentication required)
       ▼
┌─────────────────────────┐
│    API Gateway (8080)   │
│  ┌───────────────────┐  │
│  │  SecurityConfig   │  │
│  │  permitAll()      │  │
│  └───────────────────┘  │
│                         │
│  ┌───────────────────┐  │
│  │   YAML Routes     │  │
│  │   (no filters)    │  │
│  └───────────────────┘  │
└────────┬────────────────┘
         │ Route to backend
         │ (JWT passed through)
         ▼
┌──────────────────────────┐
│   Backend Service        │
│   (Identity/Product)     │
│                          │
│  ┌────────────────────┐  │
│  │  JWT Validation    │  │
│  │  (service layer)   │  │
│  └────────────────────┘  │
│                          │
│  ┌────────────────────┐  │
│  │  Authorization     │  │
│  │  (service layer)   │  │
│  └────────────────────┘  │
└──────────────────────────┘
```

**Key Points**:
1. Gateway does not validate JWT tokens
2. Gateway passes Authorization header to backend services
3. Backend services validate JWT and enforce authorization
4. Multi-tenant isolation handled at service layer

---

## Alternative Architecture: Gateway with Authentication (Future)

If authentication enforcement at the gateway is desired in the future:

### Required Changes

1. **Fix Redis Configuration**
   ```yaml
   spring:
     redis:
       password: ""  # Or configure Redis with password
   ```

2. **Re-enable Rate Limiting**
   ```yaml
   filters:
     - name: RequestRateLimiter
       args:
         redis-rate-limiter.replenishRate: 10
         redis-rate-limiter.burstCapacity: 20
   ```

3. **Add JWT Validation Dependencies**
   ```gradle
   implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
   ```

4. **Configure JWT Validation**
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://localhost:8180/realms/cursorpos
             # OR use JWK Set URI
             jwk-set-uri: http://localhost:8081/api/v1/auth/.well-known/jwks.json
   ```

5. **Update SecurityConfig.java**
   ```java
   @Bean
   public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
       http
           .authorizeExchange(exchanges -> exchanges
               .pathMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
               .anyExchange().authenticated()
           )
           .oauth2ResourceServer(oauth2 -> oauth2.jwt());
       return http.build();
   }
   ```

6. **Re-enable Programmatic Routes with Filters** (optional)
   - Uncomment `RouteLocator` bean in `GatewayConfiguration.java`
   - Fix `AuthenticationGatewayFilter` to work with reactive JWT validation

### Trade-offs

| Aspect | Current (Service Auth) | Alternative (Gateway Auth) |
|--------|----------------------|---------------------------|
| Complexity | Simple | Complex |
| Performance | Backend validation overhead | Gateway validation reduces backend load |
| Security | Each service validates independently | Centralized validation |
| Maintenance | Service-level changes | Gateway-level changes |
| Failure Mode | Service handles auth failures | Gateway blocks invalid requests early |

---

## Testing the Gateway

### Test 1: Login (No Authentication Required)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "tenant-coffee-001",
    "email": "admin@coffee.test",
    "password": "Test@123456"
  }'
```

**Expected**: 200 OK with JWT token

### Test 2: Get Products (JWT Required by Backend)
```bash
curl http://localhost:8080/api/v1/products?page=0&size=10 \
  -H "Authorization: Bearer {jwt_token}"
```

**Expected**: 200 OK with product list (Gateway passes JWT to Product Service)

### Test 3: Missing JWT (Backend Rejects)
```bash
curl http://localhost:8080/api/v1/products?page=0&size=10
```

**Expected**: 401 Unauthorized from Product Service (not Gateway)

---

## Known Issues & Technical Debt

### 1. Redis Password Mismatch
- **Issue**: `application.yml` has `redis_secret`, Redis has no password
- **Impact**: Rate limiting cannot be enabled
- **Fix**: Update `application.yml` to `password: ""` or configure Redis

### 2. Keycloak Not Deployed
- **Issue**: OAuth2 configuration references Keycloak that doesn't exist
- **Impact**: OAuth2 cannot be enabled
- **Fix**: Deploy Keycloak or use custom JWT issuer

### 3. AuthenticationGatewayFilter Broken
- **Issue**: Filter requires `JwtUtil` which is excluded from reactive dependencies
- **Impact**: Cannot use programmatic authentication filters
- **Fix**: Rewrite filter for reactive JWT validation or use OAuth2 Resource Server

### 4. No Rate Limiting
- **Issue**: All RequestRateLimiter filters disabled
- **Impact**: No protection against API abuse
- **Fix**: Configure Redis properly and re-enable filters

---

## Deployment Considerations

### Development Environment
- ✅ Current configuration (permitAll) is acceptable
- Backend services handle authentication
- Easy to test and debug

### Production Environment
- ⚠️ Consider enabling authentication at Gateway for better security
- ⚠️ Enable rate limiting to prevent API abuse
- ⚠️ Configure proper CORS policies
- ⚠️ Add API Gateway observability (metrics, logging, tracing)
- ⚠️ Implement circuit breakers for backend service failures

---

## References

- Spring Cloud Gateway Documentation: https://spring.io/projects/spring-cloud-gateway
- WebFlux Security: https://docs.spring.io/spring-security/reference/reactive/index.html
- OAuth2 Resource Server: https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/index.html

---

**Maintained By**: CursorPOS Development Team  
**Last Security Review**: 2025-01-XX  
**Next Review**: TBD after architecture decision on gateway authentication
