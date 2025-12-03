# Quick Reference Guide

## Essential Commands

### Start Infrastructure
```powershell
# Create Docker network (first time only)
docker network create cursorpos-network

# Start PostgreSQL
docker start postgre
# OR create new:
docker run -d --name postgre -e POSTGRES_USER=posuser -e POSTGRES_PASSWORD=pos_db_password_2025 -e POSTGRES_DB=cursorpos -p 5432:5432 --network cursorpos-network postgres:17

# Start Redis
docker start cursorpos-redis
# OR create new:
docker run -d --name cursorpos-redis -p 6379:6379 --network cursorpos-network redis:7-alpine redis-server --requirepass redis_dev_password_2025

# Start Keycloak (optional)
docker start cursorpos-keycloak
```

### Start Services
```powershell
cd service-pos

# All services at once
.\start-services.ps1

# Or individually:
.\gradlew.bat :identity-service:bootRun    # Port 8081
.\gradlew.bat :product-service:bootRun     # Port 8083
.\gradlew.bat :transaction-service:bootRun # Port 8084
.\gradlew.bat :api-gateway:bootRun         # Port 8080
```

### Build & Test
```powershell
# Clean build
.\gradlew.bat clean build

# Run tests only
.\gradlew.bat test

# Skip tests
.\gradlew.bat build -x test

# Test specific service
.\gradlew.bat :identity-service:test

# Generate coverage report
.\gradlew.bat test jacocoTestReport
```

### Database Operations
```powershell
# Connect to PostgreSQL
docker exec -it postgre psql -U posuser -d cursorpos

# View migration history
SELECT * FROM flyway_schema_history_identity;
SELECT * FROM flyway_schema_history_product;
SELECT * FROM flyway_schema_history_transaction;

# List all tables
\dt

# Quit
\q
```

### Redis Operations
```powershell
# Connect to Redis
docker exec -it cursorpos-redis redis-cli -a redis_dev_password_2025

# Test connection
PING

# View all keys
KEYS *

# Clear all data (careful!)
FLUSHALL

# Quit
exit
```

### Test API Endpoints
```powershell
# Login
$body = @{
    tenantId = "tenant-coffee-001"
    email = "admin@coffee.test"
    password = "Test@123456"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/login -Method POST -Body $body -ContentType "application/json"
$token = $response.data.token

# Get products
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products?page=0&size=10" -Headers @{Authorization = "Bearer $token"}

# Get transactions
Invoke-RestMethod -Uri http://localhost:8080/api/transactions -Headers @{Authorization = "Bearer $token"}
```

### Git Workflow
```bash
# Pull latest changes
git pull origin main

# Create feature branch
git checkout -b feature/your-feature

# Stage changes
git add .

# Commit
git commit -m "feat: your description"

# Push
git push origin feature/your-feature
```

### Troubleshooting
```powershell
# Check port usage
netstat -ano | Select-String ":8080|:8081|:8083|:8084" | Select-String "LISTENING"

# Kill process on port
Stop-Process -Id <PID> -Force

# View service logs
Get-Content service-pos/identity-service/logs/identity-service.log -Tail 50

# Docker container logs
docker logs postgre
docker logs cursorpos-redis

# Restart container
docker restart postgre
docker restart cursorpos-redis
```

## Test Data

### Tenants
- **tenant-coffee-001**: Coffee Shop
- **tenant-restaurant-001**: Restaurant

### Users
**Coffee Shop Admin:**
- Email: `admin@coffee.test`
- Password: `Test@123456`
- Role: ADMIN

**Coffee Shop Manager:**
- Email: `manager@coffee.test`
- Password: `Test@123456`
- Role: MANAGER

**Coffee Shop Cashier:**
- Email: `cashier@coffee.test`
- Password: `Test@123456`
- Role: CASHIER

### Products (Coffee Shop)
- Espresso: $2.50
- Cappuccino: $3.50
- Latte: $4.00
- Croissant: $3.00
- Bagel: $2.50

## Service URLs

- **Identity Service**: http://localhost:8081
- **Product Service**: http://localhost:8083
- **Transaction Service**: http://localhost:8084
- **API Gateway**: http://localhost:8080

## Documentation

- **Developer Setup**: `DEVELOPER_SETUP.md`
- **Implementation Plan**: `IMPLEMENTATION_PLAN.md`
- **API Docs**: `docs/api/`
  - `api-gateway.html`
  - `identity-service-api.html`
  - `product-service-api.html`
  - `transaction-service-api.html`

## VS Code Tips

- **Run Tests**: Click play button in Test Explorer
- **Debug Service**: Use F5 after selecting debug configuration
- **Format Code**: Shift+Alt+F
- **Organize Imports**: Shift+Alt+O
- **Quick Fix**: Ctrl+.
- **Go to Definition**: F12
- **Find References**: Shift+F12

## Code Quality Targets

- **Line Coverage**: 95%+ for business logic
- **Branch Coverage**: 90%+
- **No SonarLint Warnings**: Fix all before commit
- **Test Naming**: `should_<expected>_when_<condition>`
