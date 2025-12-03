# ============================================================================
# CursorPOS End-to-End Integration Test Script
# ============================================================================
# Tests complete system functionality through API Gateway
# Author: rjnat
# Date: 2025-11-22
# ============================================================================

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"
$identityUrl = "http://localhost:8081"

Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "  CursorPOS Integration Test Suite  " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Test counters
$totalTests = 0
$passedTests = 0
$failedTests = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    $script:totalTests++
    Write-Host "`n[$script:totalTests] Testing: $Name" -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri         = $Url
            Method      = $Method
            Headers     = $Headers
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        if ($response.StatusCode -eq $ExpectedStatus) {
            Write-Host "✓ PASS - Status: $($response.StatusCode)" -ForegroundColor Green
            $script:passedTests++
            return $response
        }
        else {
            Write-Host "✗ FAIL - Expected: $ExpectedStatus, Got: $($response.StatusCode)" -ForegroundColor Red
            $script:failedTests++
            return $null
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "✓ PASS - Status: $statusCode (as expected)" -ForegroundColor Green
            $script:passedTests++
            return $_.Exception.Response
        }
        else {
            Write-Host "✗ FAIL - Error: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "  Expected Status: $ExpectedStatus" -ForegroundColor Red
            if ($statusCode) {
                Write-Host "  Actual Status: $statusCode" -ForegroundColor Red
            }
            $script:failedTests++
            return $null
        }
    }
}

# ============================================================================
# STEP 1: Basic Health Checks
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 1: Basic Health Checks" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

Test-Endpoint `
    -Name "Gateway Health Check" `
    -Method "GET" `
    -Url "$baseUrl/actuator/health"

Test-Endpoint `
    -Name "Identity Service Health" `
    -Method "GET" `
    -Url "$identityUrl/actuator/health"

# ============================================================================
# STEP 2: Authentication Flow
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 2: Authentication Flow" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

# Test login with Coffee Shop tenant
$loginBody = @{
    email    = "admin@coffeeshop.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Test-Endpoint `
    -Name "Login - Coffee Shop Admin" `
    -Method "POST" `
    -Url "$baseUrl/api/v1/auth/login" `
    -Body $loginBody

if ($loginResponse) {
    $loginData = $loginResponse.Content | ConvertFrom-Json
    $token = $loginData.data.token
    Write-Host "  Token received: $($token.Substring(0, 50))..." -ForegroundColor Gray
    
    # Store token for subsequent tests
    $script:authToken = $token
    $script:authHeaders = @{
        "Authorization" = "Bearer $token"
        "Content-Type"  = "application/json"
    }
}

# Test invalid login
$invalidLoginBody = @{
    email    = "admin@coffeeshop.com"
    password = "wrongpassword"
} | ConvertTo-Json

Test-Endpoint `
    -Name "Login - Invalid Password" `
    -Method "POST" `
    -Url "$baseUrl/api/v1/auth/login" `
    -Body $invalidLoginBody `
    -ExpectedStatus 401

# ============================================================================
# STEP 3: Protected Endpoint Without Token
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 3: Protected Endpoint Access Control" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

Test-Endpoint `
    -Name "Products Endpoint - No Token" `
    -Method "GET" `
    -Url "$baseUrl/api/v1/products" `
    -ExpectedStatus 401

# ============================================================================
# STEP 4: Product Service Integration
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 4: Product Service Integration" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

if ($script:authHeaders) {
    # Search for products
    $searchResponse = Test-Endpoint `
        -Name "Search Products - 'Coffee'" `
        -Method "GET" `
        -Url "$baseUrl/api/v1/products/search?q=coffee" `
        -Headers $script:authHeaders
    
    if ($searchResponse) {
        $products = ($searchResponse.Content | ConvertFrom-Json).data
        Write-Host "  Found $($products.Count) products" -ForegroundColor Gray
        if ($products.Count -gt 0) {
            $script:testProductId = $products[0].id
            Write-Host "  Test Product: $($products[0].name) (ID: $script:testProductId)" -ForegroundColor Gray
        }
    }
    
    # List categories
    $categoriesResponse = Test-Endpoint `
        -Name "List Categories" `
        -Method "GET" `
        -Url "$baseUrl/api/v1/categories" `
        -Headers $script:authHeaders
    
    if ($categoriesResponse) {
        $categories = ($categoriesResponse.Content | ConvertFrom-Json).data
        Write-Host "  Found $($categories.Count) categories" -ForegroundColor Gray
    }
    
    # Get specific product (if we have one)
    if ($script:testProductId) {
        Test-Endpoint `
            -Name "Get Product By ID" `
            -Method "GET" `
            -Url "$baseUrl/api/v1/products/$script:testProductId" `
            -Headers $script:authHeaders
    }
}

# ============================================================================
# STEP 5: Transaction Service Integration
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 5: Transaction Service Integration" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

if ($script:authHeaders -and $script:testProductId) {
    # Create a test transaction
    $transactionBody = @{
        branchId       = "branch-001"
        customerId     = $null
        type           = "SALE"
        items          = @(
            @{
                productId   = $script:testProductId
                productCode = "PROD-001"
                productName = "Test Product"
                quantity    = 2
                unitPrice   = 10.00
                taxRate     = 10.0
            }
        )
        payments       = @(
            @{
                paymentMethod = "CASH"
                amount        = 25.00
            }
        )
        discountAmount = 0.0
        notes          = "Integration test order"
    } | ConvertTo-Json -Depth 10
    
    $transactionResponse = Test-Endpoint `
        -Name "Create Transaction" `
        -Method "POST" `
        -Url "$baseUrl/api/v1/transactions" `
        -Headers $script:authHeaders `
        -Body $transactionBody `
        -ExpectedStatus 201
    
    if ($transactionResponse) {
        $transaction = ($transactionResponse.Content | ConvertFrom-Json).data
        $script:testTransactionId = $transaction.id
        Write-Host "  Transaction created: $($transaction.transactionNumber)" -ForegroundColor Gray
        Write-Host "  Total: $($transaction.totalAmount), Change: $($transaction.changeAmount)" -ForegroundColor Gray
        
        # Get transaction by ID
        Test-Endpoint `
            -Name "Get Transaction By ID" `
            -Method "GET" `
            -Url "$baseUrl/api/v1/transactions/$script:testTransactionId" `
            -Headers $script:authHeaders
        
        # Get transaction by number
        Test-Endpoint `
            -Name "Get Transaction By Number" `
            -Method "GET" `
            -Url "$baseUrl/api/v1/transactions/number/$($transaction.transactionNumber)" `
            -Headers $script:authHeaders
        
        # Generate receipt
        $receiptResponse = Test-Endpoint `
            -Name "Generate Receipt" `
            -Method "POST" `
            -Url "$baseUrl/api/v1/receipts/transaction/$script:testTransactionId" `
            -Headers $script:authHeaders `
            -ExpectedStatus 201
        
        if ($receiptResponse) {
            $receipt = ($receiptResponse.Content | ConvertFrom-Json).data
            Write-Host "  Receipt generated: $($receipt.receiptNumber)" -ForegroundColor Gray
        }
    }
    
    # List transactions
    Test-Endpoint `
        -Name "List All Transactions" `
        -Method "GET" `
        -Url "$baseUrl/api/v1/transactions?page=0&size=10" `
        -Headers $script:authHeaders
}

# ============================================================================
# STEP 6: Tenant Isolation Testing
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 6: Tenant Isolation Testing" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

# Login as Restaurant tenant
$restaurantLoginBody = @{
    email    = "admin@restaurant.com"
    password = "password123"
} | ConvertTo-Json

$restaurantLoginResponse = Test-Endpoint `
    -Name "Login - Restaurant Admin" `
    -Method "POST" `
    -Url "$baseUrl/api/v1/auth/login" `
    -Body $restaurantLoginBody

if ($restaurantLoginResponse) {
    $restaurantData = $restaurantLoginResponse.Content | ConvertFrom-Json
    $restaurantToken = $restaurantData.data.token
    $restaurantHeaders = @{
        "Authorization" = "Bearer $restaurantToken"
        "Content-Type"  = "application/json"
    }
    
    # Try to search products (should only see restaurant products)
    $restaurantProductsResponse = Test-Endpoint `
        -Name "Restaurant - Search Products" `
        -Method "GET" `
        -Url "$baseUrl/api/v1/products/search?q=food" `
        -Headers $restaurantHeaders
    
    if ($restaurantProductsResponse) {
        $restaurantProducts = ($restaurantProductsResponse.Content | ConvertFrom-Json).data
        Write-Host "  Restaurant found $($restaurantProducts.Count) products" -ForegroundColor Gray
    }
    
    # Try to access coffee shop's transaction (should fail or return empty)
    if ($script:testTransactionId) {
        Test-Endpoint `
            -Name "Restaurant - Try Access Coffee Shop Transaction" `
            -Method "GET" `
            -Url "$baseUrl/api/v1/transactions/$script:testTransactionId" `
            -Headers $restaurantHeaders `
            -ExpectedStatus 404
    }
}

# ============================================================================
# STEP 7: Error Handling Testing
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  STEP 7: Error Handling Testing" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

if ($script:authHeaders) {
    # Test 404 - Non-existent product
    Test-Endpoint `
        -Name "Get Non-Existent Product" `
        -Method "GET" `
        -Url "$baseUrl/api/v1/products/99999999-9999-9999-9999-999999999999" `
        -Headers $script:authHeaders `
        -ExpectedStatus 404
    
    # Test 404 - Non-existent transaction
    Test-Endpoint `
        -Name "Get Non-Existent Transaction" `
        -Method "GET" `
        -Url "$baseUrl/api/v1/transactions/99999999-9999-9999-9999-999999999999" `
        -Headers $script:authHeaders `
        -ExpectedStatus 404
    
    # Test 400 - Invalid transaction (missing required fields)
    $invalidTransactionBody = @{
        type  = "SALE"
        items = @()
    } | ConvertTo-Json
    
    Test-Endpoint `
        -Name "Create Invalid Transaction" `
        -Method "POST" `
        -Url "$baseUrl/api/v1/transactions" `
        -Headers $script:authHeaders `
        -Body $invalidTransactionBody `
        -ExpectedStatus 400
}

# Test invalid token
$invalidHeaders = @{
    "Authorization" = "Bearer invalid.token.here"
    "Content-Type"  = "application/json"
}

Test-Endpoint `
    -Name "Access with Invalid Token" `
    -Method "GET" `
    -Url "$baseUrl/api/v1/products" `
    -Headers $invalidHeaders `
    -ExpectedStatus 401

# ============================================================================
# Test Summary
# ============================================================================
Write-Host "`n===========================================" -ForegroundColor Cyan
Write-Host "  TEST SUMMARY" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "Total Tests:  $totalTests" -ForegroundColor White
Write-Host "Passed:       $passedTests" -ForegroundColor Green
Write-Host "Failed:       $failedTests" -ForegroundColor Red

$successRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })

Write-Host "`n===========================================" -ForegroundColor Cyan
if ($failedTests -eq 0) {
    Write-Host "  ✓ ALL TESTS PASSED!" -ForegroundColor Green
}
else {
    Write-Host "  ⚠ SOME TESTS FAILED" -ForegroundColor Yellow
}
Write-Host "===========================================" -ForegroundColor Cyan

# Return exit code
exit $failedTests
