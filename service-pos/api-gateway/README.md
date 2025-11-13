# API Gateway Service

Spring Cloud Gateway for routing, authentication, rate limiting, and circuit breaking.

## Port
- **8080** (default)

## Features

### Routing
Routes requests to downstream microservices:
- Identity Service (8081): `/api/v1/auth/**`, `/api/v1/users/**`
- Admin Service (8082): `/api/v1/tenants/**`, `/api/v1/customers/**`, `/api/v1/stores/**`
- Product Service (8083): `/api/v1/products/**`, `/api/v1/categories/**`, `/api/v1/inventory/**`
- Transaction Service (8084): `/api/v1/transactions/**`, `/api/v1/sales/**`, `/api/v1/payments/**`

### Authentication
- JWT token validation for all endpoints (except public)
- Extracts tenant context and adds headers for downstream services
- Public endpoints: `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/tenants/signup`

### Rate Limiting
- Redis-based rate limiting per tenant
- Configurable rates per service
- Fallback to IP-based rate limiting for unauthenticated requests

### Circuit Breaker
- Resilience4j circuit breakers for each service
- Fallback responses when services are unavailable
- Configurable thresholds and timeouts

### CORS
- Configured for development with localhost origins
- Supports credentials
- All HTTP methods allowed

## Configuration

Key configuration in `application.yml`:
- Route definitions with predicates and filters
- Circuit breaker settings (failure rate, timeout)
- Rate limiting (replenish rate, burst capacity)
- Redis connection for rate limiting

## Running

```bash
cd service-pos
./gradlew :api-gateway:bootRun
```

Or with Docker:
```bash
docker-compose up api-gateway
```

## Endpoints

### Health Check
- `GET /actuator/health` - Service health status
- `GET /api/v1/gateway/health` - Gateway-specific health
- `GET /api/v1/gateway/info` - Service information

### Metrics
- `GET /actuator/metrics` - Micrometer metrics
- `GET /actuator/prometheus` - Prometheus metrics endpoint

## Headers Added for Downstream Services

The gateway adds these headers to authenticated requests:
- `X-Tenant-Id` - Tenant identifier
- `X-User-Id` - User identifier
- `X-User-Role` - User role
- `X-Store-Id` - Store identifier (if present)
- `X-Branch-Id` - Branch identifier (if present)

## Circuit Breaker Fallbacks

When a service is unavailable, fallback responses are returned:
- `/fallback/identity` - Identity service fallback
- `/fallback/admin` - Admin service fallback
- `/fallback/product` - Product service fallback
- `/fallback/transaction` - Transaction service fallback

## Environment Variables

- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `REDIS_PASSWORD` - Redis password
- `JWT_SECRET` - JWT secret key
- `KEYCLOAK_ISSUER_URI` - Keycloak issuer URI
