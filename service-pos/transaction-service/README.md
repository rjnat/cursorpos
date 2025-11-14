# Transaction Service

Transaction management service for CursorPOS multi-tenant point-of-sale system.

## Features

### Transaction Management
- Complete sales transaction processing
- Transaction items with quantity, pricing, discounts, and taxes
- Multi-payment support (cash, card, e-wallet, bank transfer)
- Transaction status tracking (pending, completed, cancelled, refunded)
- Transaction types: sale, return, exchange
- Automatic number generation for transactions
- Real-time amount calculations

### Payment Processing
- Multiple payment methods per transaction
- Payment method tracking: CASH, CREDIT_CARD, DEBIT_CARD, E_WALLET, BANK_TRANSFER, CHECK
- Reference number support for digital payments
- Change calculation for overpayment

### Receipt Generation
- Automatic receipt generation from transactions
- Print tracking with count and timestamp
- Text-based receipt format
- Unique receipt numbering

### Multi-Tenancy & Security
- Tenant isolation for all transactions
- Branch-based transaction filtering
- Customer purchase history
- Soft delete support

## API Endpoints

### Transaction Endpoints
- `POST /api/transactions` - Create new transaction
- `GET /api/transactions` - Get all transactions (paginated)
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions/number/{transactionNumber}` - Get by transaction number
- `GET /api/transactions/branch/{branchId}` - Get transactions by branch
- `GET /api/transactions/customer/{customerId}` - Get transactions by customer
- `GET /api/transactions/status/{status}` - Get transactions by status
- `GET /api/transactions/date-range?startDate={date}&endDate={date}` - Get by date range
- `PUT /api/transactions/{id}/cancel` - Cancel transaction

### Receipt Endpoints
- `POST /api/receipts/transaction/{transactionId}` - Generate receipt
- `GET /api/receipts/{id}` - Get receipt by ID
- `GET /api/receipts/transaction/{transactionId}` - Get receipt by transaction
- `PUT /api/receipts/{id}/print` - Mark receipt as printed

### Health Check
- `GET /api/health` - Service health check

## Configuration

### Database
- PostgreSQL database: `cursorpos_transaction`
- Flyway migrations enabled
- Tables: transactions, transaction_items, payments, receipts

### Server
- Port: 8084
- Context path: /

### Dependencies
- Spring Boot 3.5.7
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Flyway
- MapStruct
- Lombok

## Transaction Processing Flow

1. **Create Transaction**
   - Validate items and pricing
   - Calculate subtotals and taxes per item
   - Apply transaction-level discount
   - Calculate total amount
   - Process payments
   - Calculate change if overpaid
   - Set status (PENDING or COMPLETED based on payment)

2. **Generate Receipt**
   - Create receipt record
   - Generate formatted receipt content
   - Track print count

3. **Cancel Transaction**
   - Update status to CANCELLED
   - Maintain audit trail

## Database Schema

### Transactions
- Transaction header with totals and status
- Links to branch, customer, cashier
- Supports multiple items and payments

### Transaction Items
- Individual line items
- Product details snapshot
- Quantity, pricing, discounts, taxes

### Payments
- Payment records per transaction
- Payment method tracking
- Reference numbers for digital payments

### Receipts
- Generated receipt records
- Print tracking
- Text-based content storage

## Author
- rjnat
- Version 1.0.0
- Date: 2025-11-14
