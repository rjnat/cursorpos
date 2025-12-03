# CursorPOS Developer Setup Guide

This guide will help you set up the CursorPOS project on your local machine.

## Prerequisites

### Required Software
1. **Java 21** - [Download OpenJDK 21](https://adoptium.net/)
2. **Docker Desktop** - [Download Docker](https://www.docker.com/products/docker-desktop/)
3. **PostgreSQL Client Tools** (optional, for direct DB access)
4. **VS Code** - [Download VS Code](https://code.visualstudio.com/)

### Required VS Code Extensions
- **Extension Pack for Java** (vscjava.vscode-java-pack)
- **Spring Boot Extension Pack** (vmware.vscode-boot-dev-pack)
- **Gradle for Java** (vscjava.vscode-gradle)
- **SonarLint** (SonarSource.sonarlint-vscode) - for code quality

## Project Setup Steps

### 1. Clone the Repository
```bash
git clone https://github.com/rjnat/cursorpos.git
cd cursorpos
```

### 2. Open in VS Code
```bash
code cursorpos.code-workspace
```

This will open the multi-root workspace with proper Java configuration.

### 3. Set Up Environment Variables

Create a `.env` file in `service-pos/` directory:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cursorpos
DB_USER=posuser
DB_PASSWORD=pos_db_password_2025

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_dev_password_2025

# JWT Configuration
JWT_SECRET=cursorpos-jwt-secret-key-2025-minimum-256-bits-required-for-hs256-algorithm-security

# Kafka Configuration (optional)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Keycloak Configuration (for OAuth2)
KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/cursorpos
```

### 4. Start Infrastructure Services

#### Start PostgreSQL
```powershell
docker run -d `
  --name postgre `
  -e POSTGRES_USER=posuser `
  -e POSTGRES_PASSWORD=pos_db_password_2025 `
  -e POSTGRES_DB=cursorpos `
  -p 5432:5432 `
  --network cursorpos-network `
  postgres:17
```

#### Start Redis
```powershell
docker run -d `
  --name cursorpos-redis `
  -p 6379:6379 `
  --network cursorpos-network `
  redis:7-alpine `
  redis-server --requirepass redis_dev_password_2025
```

#### Start Keycloak (Optional - for OAuth2 testing)
```powershell
docker run -d `
  --name cursorpos-keycloak `
  -p 8180:8080 `
  -e KEYCLOAK_ADMIN=admin `
  -e KEYCLOAK_ADMIN_PASSWORD=admin_password_2025 `
  --network cursorpos-network `
  quay.io/keycloak/keycloak:26.0.7 `
  start-dev
```

### 5. Build the Project

From the `service-pos` directory:

```powershell
# Windows
.\gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

This will:
- Download all dependencies
- Compile all services
- Run all tests
- Generate MapStruct implementations
- Create executable JARs

### 6. Start Services

You can start services individually or use the provided script:

#### Option A: Use Start Script (Recommended)
```powershell
cd service-pos
.\start-services.ps1
```

This will start all services in separate PowerShell windows.

#### Option B: Start Services Manually

**Identity Service (Port 8081)**
```powershell
cd service-pos
.\gradlew.bat :identity-service:bootRun
```

**Product Service (Port 8083)**
```powershell
cd service-pos
.\gradlew.bat :product-service:bootRun
```

**Transaction Service (Port 8084)**
```powershell
cd service-pos
.\gradlew.bat :transaction-service:bootRun
```

**API Gateway (Port 8080)**
```powershell
cd service-pos
.\gradlew.bat :api-gateway:bootRun
```

### 7. Verify Setup

Test the services are running:

```powershell
# Test Identity Service
Invoke-RestMethod http://localhost:8081/api/health

# Test Product Service
Invoke-RestMethod http://localhost:8083/api/health

# Test Transaction Service
Invoke-RestMethod http://localhost:8084/api/health

# Test API Gateway
Invoke-RestMethod http://localhost:8080/actuator/health
```

### 8. Test Login Flow

```powershell
$body = @{
    tenantId = "tenant-coffee-001"
    email = "admin@coffee.test"
    password = "Test@123456"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/login `
    -Method POST `
    -Body $body `
    -ContentType "application/json"

Write-Host "Token: $($response.data.token)"
```

## Project Structure

```
cursorpos/
├── service-pos/              # Backend microservices
│   ├── identity-service/     # Authentication & user management (8081)
│   ├── product-service/      # Products, inventory, categories (8083)
│   ├── transaction-service/  # Orders, payments, receipts (8084)
│   ├── api-gateway/          # API Gateway with routing (8080)
│   ├── shared-lib/           # Shared utilities, security, base entities
│   ├── build.gradle          # Root build configuration
│   ├── settings.gradle       # Multi-project settings
│   └── start-services.ps1    # Service startup script
├── docs/                     # API documentation
│   └── api/                  # HTML API docs for each service
├── web-pos/                  # Web frontend (React) - Coming soon
├── mobile-pos/               # Mobile app (React Native) - Coming soon
└── IMPLEMENTATION_PLAN.md    # Detailed implementation roadmap
```

## Common Issues & Solutions

### Issue: Port Already in Use
```powershell
# Find process using port 8080
netstat -ano | Select-String ":8080" | Select-String "LISTENING"

# Kill the process (replace PID)
Stop-Process -Id <PID> -Force
```

### Issue: PostgreSQL Not Ready
```powershell
# Check PostgreSQL status
docker exec postgre pg_isready -U posuser

# View PostgreSQL logs
docker logs postgre
```

### Issue: Redis Connection Refused
```powershell
# Test Redis connection
docker exec cursorpos-redis redis-cli -a redis_dev_password_2025 ping

# Should return: PONG
```

### Issue: Flyway Migration Errors
```powershell
# Clean and rebuild
.\gradlew.bat clean build -x test

# Each service has independent migration history:
# - identity-service uses: flyway_schema_history_identity
# - product-service uses: flyway_schema_history_product
# - transaction-service uses: flyway_schema_history_transaction
```

### Issue: MapStruct Implementation Not Found
```powershell
# Rebuild with MapStruct annotation processing
.\gradlew.bat clean build

# Generated mappers are in: build/generated/sources/annotationProcessor/
```

## Running Tests

### All Tests
```powershell
.\gradlew.bat test
```

### Service-Specific Tests
```powershell
.\gradlew.bat :identity-service:test
.\gradlew.bat :product-service:test
.\gradlew.bat :transaction-service:test
```

### Test Coverage Report
```powershell
.\gradlew.bat test jacocoTestReport

# View reports in:
# - identity-service/build/reports/jacoco/test/html/index.html
# - product-service/build/reports/jacoco/test/html/index.html
# - transaction-service/build/reports/jacoco/test/html/index.html
```

## Development Workflow

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Follow existing code patterns
   - Write unit tests (target 95%+ coverage)
   - Write integration tests
   - Update API documentation if needed

3. **Run Tests**
   ```powershell
   .\gradlew.bat test
   ```

4. **Check Code Quality**
   - SonarLint runs automatically in VS Code
   - Fix all warnings before committing

5. **Commit Changes**
   ```bash
   git add .
   git commit -m "feat: your feature description"
   ```

6. **Push and Create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

## API Documentation

API documentation is available as static HTML files:
- **API Gateway**: `docs/api/api-gateway.html`
- **Identity Service**: `docs/api/identity-service-api.html`
- **Product Service**: `docs/api/product-service-api.html`
- **Transaction Service**: `docs/api/transaction-service-api.html`

Open these files in a browser for complete API reference.

## Database Access

### Connect to PostgreSQL
```bash
docker exec -it postgre psql -U posuser -d cursorpos
```

### Useful SQL Queries
```sql
-- List all tables
\dt

-- View migration history for Identity Service
SELECT * FROM flyway_schema_history_identity;

-- View all tenants
SELECT * FROM tenants;

-- View all products
SELECT * FROM products;
```

## Troubleshooting Checklist

- [ ] Java 21 installed and in PATH?
- [ ] Docker Desktop running?
- [ ] PostgreSQL container running?
- [ ] Redis container running?
- [ ] .env file created with correct passwords?
- [ ] All ports available (8080, 8081, 8083, 8084)?
- [ ] Gradle build successful?
- [ ] All tests passing?

## Getting Help

- Review `IMPLEMENTATION_PLAN.md` for project roadmap
- Check `docs/api/` for API documentation
- Review test files for usage examples
- Check Docker logs: `docker logs <container-name>`
- Check service logs: `service-pos/<service>/logs/`

## Next Steps

Once your environment is set up:
1. Review the codebase structure
2. Run all tests to verify setup
3. Test login flow through API Gateway
4. Review IMPLEMENTATION_PLAN.md for current status
5. Pick a task from the pending items

Happy coding! 🚀
