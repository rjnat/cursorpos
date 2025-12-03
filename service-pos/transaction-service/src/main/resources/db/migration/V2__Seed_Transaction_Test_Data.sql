-- ==============================================================================
-- Transaction Service - Seed Test Data
-- ==============================================================================
-- Author: rjnat
-- Version: 1.0.0
-- Date: 2025-11-20
-- Description: Insert sample transactions, items, payments, and receipts for testing
-- ==============================================================================

-- Sample tenant IDs
-- tenant-coffee-001: Coffee Shop tenant
-- tenant-restaurant-001: Restaurant tenant

-- Sample branch IDs (using UUIDs from other services)
-- Branch 1 (Coffee Shop HQ): 11111111-1111-1111-1111-111111111111
-- Branch 2 (Coffee Shop Downtown): 22222222-2222-2222-2222-222222222222
-- Branch 3 (Restaurant HQ): 33333333-3333-3333-3333-333333333333

-- Sample customer IDs
-- Customer 1: 10000000-0000-0000-0000-000000000001
-- Customer 2: 10000000-0000-0000-0000-000000000002

-- Sample product IDs (from Product Service)
-- Espresso: 20000000-0000-0000-0000-000000000001
-- Cappuccino: 20000000-0000-0000-0000-000000000002
-- Croissant: 20000000-0000-0000-0000-000000000003

-- Cashier IDs (from Identity Service)
-- Cashier John: 30000000-0000-0000-0000-000000000001
-- Cashier Jane: 30000000-0000-0000-0000-000000000002

-- ==============================================================================
-- COFFEE SHOP TRANSACTIONS
-- ==============================================================================

-- Transaction 1: Coffee Shop - Completed Sale
INSERT INTO transactions (
    id, tenant_id, transaction_number, branch_id, customer_id,
    transaction_date, status, transaction_type,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, change_amount,
    cashier_id, cashier_name,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '40000000-0000-0000-0000-000000000001',
    'tenant-coffee-001',
    'TRX-20251120-090000-ABC12345',
    '11111111-1111-1111-1111-111111111111',
    '10000000-0000-0000-0000-000000000001',
    '2025-11-20 09:00:00',
    'COMPLETED',
    'SALE',
    50.00,
    5.00,
    0.00,
    55.00,
    60.00,
    5.00,
    '30000000-0000-0000-0000-000000000001',
    'John Doe',
    '2025-11-20 09:00:00',
    'system',
    '2025-11-20 09:00:00',
    'system',
    0
);

-- Transaction 1 Items
INSERT INTO transaction_items (
    id, tenant_id, transaction_id, product_id,
    product_code, product_name, quantity, unit_price,
    discount_amount, tax_rate, tax_amount, subtotal, total_amount,
    created_at, created_by, updated_at, updated_by, version
) VALUES
(
    '41000000-0000-0000-0000-000000000001',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'ESP-001',
    'Espresso',
    2,
    15.00,
    0.00,
    10.00,
    3.00,
    30.00,
    33.00,
    '2025-11-20 09:00:00',
    'system',
    '2025-11-20 09:00:00',
    'system',
    0
),
(
    '41000000-0000-0000-0000-000000000002',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    'CAP-001',
    'Cappuccino',
    1,
    20.00,
    0.00,
    10.00,
    2.00,
    20.00,
    22.00,
    '2025-11-20 09:00:00',
    'system',
    '2025-11-20 09:00:00',
    'system',
    0
);

-- Transaction 1 Payment
INSERT INTO payments (
    id, tenant_id, transaction_id, payment_method,
    amount, payment_date, reference_number,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '42000000-0000-0000-0000-000000000001',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000001',
    'CASH',
    60.00,
    '2025-11-20 09:00:00',
    NULL,
    '2025-11-20 09:00:00',
    'system',
    '2025-11-20 09:00:00',
    'system',
    0
);

-- Transaction 1 Receipt
INSERT INTO receipts (
    id, tenant_id, transaction_id, receipt_number,
    issued_date, receipt_type, content, print_count, last_printed_at,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '43000000-0000-0000-0000-000000000001',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000001',
    'RCP-20251120-090000-ABC12345',
    '2025-11-20 09:00:00',
    'SALE',
    '========================================
           SALES RECEIPT
========================================
Transaction: TRX-20251120-090000-ABC12345
Date: 2025-11-20 09:00:00
Cashier: John Doe
========================================

ITEMS:
Espresso             x2
  @ 15.00 = 30.00
Cappuccino           x1
  @ 20.00 = 20.00

========================================
Subtotal:     50.00
Tax:          5.00
Discount:     0.00
TOTAL:        55.00
Paid:         60.00
Change:       5.00
========================================

    Thank you for your purchase!
========================================',
    1,
    '2025-11-20 09:00:00',
    '2025-11-20 09:00:00',
    'system',
    '2025-11-20 09:00:00',
    'system',
    0
);

-- Transaction 2: Coffee Shop - Pending (Partial Payment)
INSERT INTO transactions (
    id, tenant_id, transaction_number, branch_id, customer_id,
    transaction_date, status, transaction_type,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, change_amount,
    cashier_id, cashier_name,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '40000000-0000-0000-0000-000000000002',
    'tenant-coffee-001',
    'TRX-20251120-100000-DEF67890',
    '22222222-2222-2222-2222-222222222222',
    '10000000-0000-0000-0000-000000000002',
    '2025-11-20 10:00:00',
    'PENDING',
    'SALE',
    100.00,
    10.00,
    5.00,
    105.00,
    50.00,
    0.00,
    '30000000-0000-0000-0000-000000000002',
    'Jane Smith',
    '2025-11-20 10:00:00',
    'system',
    '2025-11-20 10:00:00',
    'system',
    0
);

-- Transaction 2 Items
INSERT INTO transaction_items (
    id, tenant_id, transaction_id, product_id,
    product_code, product_name, quantity, unit_price,
    discount_amount, tax_rate, tax_amount, subtotal, total_amount,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '41000000-0000-0000-0000-000000000003',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000003',
    'CRO-001',
    'Croissant',
    5,
    20.00,
    5.00,
    10.00,
    9.50,
    95.00,
    104.50,
    '2025-11-20 10:00:00',
    'system',
    '2025-11-20 10:00:00',
    'system',
    0
);

-- Transaction 2 Payment (Partial)
INSERT INTO payments (
    id, tenant_id, transaction_id, payment_method,
    amount, payment_date, reference_number,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '42000000-0000-0000-0000-000000000002',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000002',
    'CREDIT_CARD',
    50.00,
    '2025-11-20 10:00:00',
    'CC-2025112010000001',
    '2025-11-20 10:00:00',
    'system',
    '2025-11-20 10:00:00',
    'system',
    0
);

-- Transaction 3: Coffee Shop - Cancelled
INSERT INTO transactions (
    id, tenant_id, transaction_number, branch_id, customer_id,
    transaction_date, status, transaction_type,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, change_amount,
    cashier_id, cashier_name, notes,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '40000000-0000-0000-0000-000000000003',
    'tenant-coffee-001',
    'TRX-20251120-110000-GHI11111',
    '11111111-1111-1111-1111-111111111111',
    NULL,
    '2025-11-20 11:00:00',
    'CANCELLED',
    'SALE',
    30.00,
    3.00,
    0.00,
    33.00,
    0.00,
    0.00,
    '30000000-0000-0000-0000-000000000001',
    'John Doe',
    'Customer changed mind',
    '2025-11-20 11:00:00',
    'system',
    '2025-11-20 11:00:00',
    'system',
    0
);

-- Transaction 3 Items
INSERT INTO transaction_items (
    id, tenant_id, transaction_id, product_id,
    product_code, product_name, quantity, unit_price,
    discount_amount, tax_rate, tax_amount, subtotal, total_amount,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '41000000-0000-0000-0000-000000000004',
    'tenant-coffee-001',
    '40000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000001',
    'ESP-001',
    'Espresso',
    2,
    15.00,
    0.00,
    10.00,
    3.00,
    30.00,
    33.00,
    '2025-11-20 11:00:00',
    'system',
    '2025-11-20 11:00:00',
    'system',
    0
);

-- ==============================================================================
-- RESTAURANT TRANSACTIONS
-- ==============================================================================

-- Transaction 4: Restaurant - Completed Sale with E-Wallet Payment
INSERT INTO transactions (
    id, tenant_id, transaction_number, branch_id, customer_id,
    transaction_date, status, transaction_type,
    subtotal, tax_amount, discount_amount, total_amount, paid_amount, change_amount,
    cashier_id, cashier_name,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '40000000-0000-0000-0000-000000000004',
    'tenant-restaurant-001',
    'TRX-20251120-120000-JKL22222',
    '33333333-3333-3333-3333-333333333333',
    '10000000-0000-0000-0000-000000000001',
    '2025-11-20 12:00:00',
    'COMPLETED',
    'SALE',
    200.00,
    20.00,
    10.00,
    210.00,
    210.00,
    0.00,
    '30000000-0000-0000-0000-000000000001',
    'John Doe',
    '2025-11-20 12:00:00',
    'system',
    '2025-11-20 12:00:00',
    'system',
    0
);

-- Transaction 4 Items
INSERT INTO transaction_items (
    id, tenant_id, transaction_id, product_id,
    product_code, product_name, quantity, unit_price,
    discount_amount, tax_rate, tax_amount, subtotal, total_amount,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '41000000-0000-0000-0000-000000000005',
    'tenant-restaurant-001',
    '40000000-0000-0000-0000-000000000004',
    '20000000-0000-0000-0000-000000000001',
    'PASTA-001',
    'Pasta Carbonara',
    2,
    100.00,
    10.00,
    10.00,
    19.00,
    190.00,
    209.00,
    '2025-11-20 12:00:00',
    'system',
    '2025-11-20 12:00:00',
    'system',
    0
);

-- Transaction 4 Payment
INSERT INTO payments (
    id, tenant_id, transaction_id, payment_method,
    amount, payment_date, reference_number,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '42000000-0000-0000-0000-000000000003',
    'tenant-restaurant-001',
    '40000000-0000-0000-0000-000000000004',
    'E_WALLET',
    210.00,
    '2025-11-20 12:00:00',
    'EW-2025112012000001',
    '2025-11-20 12:00:00',
    'system',
    '2025-11-20 12:00:00',
    'system',
    0
);

-- Transaction 4 Receipt
INSERT INTO receipts (
    id, tenant_id, transaction_id, receipt_number,
    issued_date, receipt_type, content, print_count,
    created_at, created_by, updated_at, updated_by, version
) VALUES (
    '43000000-0000-0000-0000-000000000002',
    'tenant-restaurant-001',
    '40000000-0000-0000-0000-000000000004',
    'RCP-20251120-120000-JKL22222',
    '2025-11-20 12:00:00',
    'SALE',
    '========================================
           SALES RECEIPT
========================================
Transaction: TRX-20251120-120000-JKL22222
Date: 2025-11-20 12:00:00
Cashier: John Doe
========================================

ITEMS:
Pasta Carbonara      x2
  @ 100.00 = 200.00

========================================
Subtotal:     200.00
Tax:          20.00
Discount:     10.00
TOTAL:        210.00
Paid:         210.00
Change:       0.00
========================================

    Thank you for your purchase!
========================================',
    0,
    '2025-11-20 12:00:00',
    'system',
    '2025-11-20 12:00:00',
    'system',
    0
);

-- ==============================================================================
-- END OF SEED DATA
-- ==============================================================================
