# ============================================================================
# CursorPOS - Start All Services with Environment Variables
# ============================================================================
# This script starts all microservices with the correct environment variables

Write-Host "=== Starting CursorPOS Services ===" -ForegroundColor Cyan

# Load environment variables from .env file
Write-Host "`nLoading environment variables from .env file..." -ForegroundColor Yellow
$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            Write-Host "  $key = $value" -ForegroundColor Gray
        }
    }
    Write-Host "✓ Environment variables loaded" -ForegroundColor Green
}
else {
    Write-Host "✗ .env file not found!" -ForegroundColor Red
    exit 1
}

# Build environment variable string for Start-Process
$envVars = @(
    "DB_HOST='localhost'",
    "DB_PORT='5432'",
    "DB_NAME='cursorpos'",
    "DB_USER='posuser'",
    "DB_PASSWORD='pos_db_password_2025'",
    "REDIS_HOST='localhost'",
    "REDIS_PORT='6379'",
    "REDIS_PASSWORD='redis_dev_password_2025'",
    "JWT_SECRET='cursorpos-jwt-secret-key-2025-minimum-256-bits-required-for-hs256-algorithm-security'",
    "KAFKA_BOOTSTRAP_SERVERS='localhost:9092'"
)
$envSetCommands = ($envVars | ForEach-Object { "`$env:$_" }) -join "; "

# Check prerequisites
Write-Host "`nChecking prerequisites..." -ForegroundColor Yellow

# Check PostgreSQL
docker exec postgre pg_isready -U posuser 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ PostgreSQL is ready" -ForegroundColor Green
}
else {
    Write-Host "✗ PostgreSQL is not ready" -ForegroundColor Red
    exit 1
}

# Check Redis
docker exec cursorpos-redis redis-cli ping 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Redis is ready" -ForegroundColor Green
}
else {
    Write-Host "✗ Redis is not ready" -ForegroundColor Red
    exit 1
}

# Check if ports are available
Write-Host "`nChecking ports..." -ForegroundColor Yellow
$ports = @(8080, 8081, 8083, 8084)
foreach ($port in $ports) {
    $inUse = netstat -ano | Select-String ":$port " | Select-String "LISTENING"
    if ($inUse) {
        Write-Host "✗ Port $port is already in use" -ForegroundColor Red
        exit 1
    }
    else {
        Write-Host "✓ Port $port is available" -ForegroundColor Green
    }
}

Write-Host "`n=== Starting Services ===" -ForegroundColor Cyan

# Start Identity Service (8081)
Write-Host "`nStarting Identity Service on port 8081..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd d:\workspace\cursorpos\service-pos; $envSetCommands; .\gradlew :identity-service:bootRun"

Start-Sleep -Seconds 3

# Start Product Service (8083)
Write-Host "Starting Product Service on port 8083..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd d:\workspace\cursorpos\service-pos; $envSetCommands; .\gradlew :product-service:bootRun"

Start-Sleep -Seconds 3

# Start Transaction Service (8084)
Write-Host "Starting Transaction Service on port 8084..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd d:\workspace\cursorpos\service-pos; $envSetCommands; .\gradlew :transaction-service:bootRun"

Start-Sleep -Seconds 3

# Start API Gateway (8080)
Write-Host "Starting API Gateway on port 8080..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd d:\workspace\cursorpos\service-pos; $envSetCommands; .\gradlew :api-gateway:bootRun"

Write-Host "`n=== All services started in separate windows ===" -ForegroundColor Green
Write-Host "`nPlease monitor each terminal window for startup status." -ForegroundColor Yellow
Write-Host "Services should be ready in 30-60 seconds." -ForegroundColor Yellow
Write-Host "`nService URLs:" -ForegroundColor Cyan
Write-Host "  Identity Service:  http://localhost:8081" -ForegroundColor White
Write-Host "  Product Service:   http://localhost:8083" -ForegroundColor White
Write-Host "  Transaction Service: http://localhost:8084" -ForegroundColor White
Write-Host "  API Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "`nTo test the setup, run: .\test-integration.ps1" -ForegroundColor Cyan
