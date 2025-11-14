-- ==============================================================================
-- Transaction Service - Database Schema
-- ==============================================================================
-- Author: rjnat
-- Version: 1.0.0
-- Date: 2025-11-14
-- ==============================================================================

-- Create transactions table
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    branch_id UUID NOT NULL,
    customer_id UUID,
    transaction_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(19, 4) DEFAULT 0,
    total_amount DECIMAL(19, 4) NOT NULL,
    paid_amount DECIMAL(19, 4) DEFAULT 0,
    change_amount DECIMAL(19, 4) DEFAULT 0,
    notes VARCHAR(500),
    cashier_id UUID,
    cashier_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE transactions IS 'Sales transactions';
COMMENT ON COLUMN transactions.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN transactions.transaction_number IS 'Unique transaction number';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, COMPLETED, CANCELLED, REFUNDED';
COMMENT ON COLUMN transactions.transaction_type IS 'Transaction type: SALE, RETURN, EXCHANGE';

-- Create transaction_items table
CREATE TABLE transaction_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    transaction_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    discount_amount DECIMAL(19, 4) DEFAULT 0,
    tax_rate DECIMAL(5, 2) DEFAULT 0,
    tax_amount DECIMAL(19, 4) DEFAULT 0,
    subtotal DECIMAL(19, 4) NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_transaction_items_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transactions(id) ON DELETE CASCADE
);

COMMENT ON TABLE transaction_items IS 'Items in a transaction';
COMMENT ON COLUMN transaction_items.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN transaction_items.transaction_id IS 'Reference to parent transaction';

-- Create payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    transaction_id UUID NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    reference_number VARCHAR(100),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_payments_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transactions(id) ON DELETE CASCADE
);

COMMENT ON TABLE payments IS 'Payment records for transactions';
COMMENT ON COLUMN payments.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN payments.payment_method IS 'Payment method: CASH, CREDIT_CARD, DEBIT_CARD, E_WALLET, BANK_TRANSFER, CHECK';

-- Create receipts table
CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    transaction_id UUID NOT NULL,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    issued_date TIMESTAMP NOT NULL,
    receipt_type VARCHAR(20),
    content TEXT,
    print_count INTEGER DEFAULT 0,
    last_printed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE receipts IS 'Generated receipts for transactions';
COMMENT ON COLUMN receipts.tenant_id IS 'Tenant identifier for multi-tenancy';
COMMENT ON COLUMN receipts.receipt_number IS 'Unique receipt number';

-- Create indexes for transactions
CREATE INDEX idx_transaction_tenant ON transactions(tenant_id);
CREATE INDEX idx_transaction_branch ON transactions(tenant_id, branch_id);
CREATE INDEX idx_transaction_customer ON transactions(tenant_id, customer_id);
CREATE INDEX idx_transaction_date ON transactions(tenant_id, transaction_date);
CREATE INDEX idx_transaction_status ON transactions(tenant_id, status);

-- Create indexes for transaction_items
CREATE INDEX idx_transaction_item_tenant ON transaction_items(tenant_id);
CREATE INDEX idx_transaction_item_transaction ON transaction_items(tenant_id, transaction_id);
CREATE INDEX idx_transaction_item_product ON transaction_items(tenant_id, product_id);

-- Create indexes for payments
CREATE INDEX idx_payment_tenant ON payments(tenant_id);
CREATE INDEX idx_payment_transaction ON payments(tenant_id, transaction_id);
CREATE INDEX idx_payment_date ON payments(tenant_id, payment_date);
CREATE INDEX idx_payment_method ON payments(tenant_id, payment_method);

-- Create indexes for receipts
CREATE INDEX idx_receipt_tenant ON receipts(tenant_id);
CREATE INDEX idx_receipt_transaction ON receipts(tenant_id, transaction_id);
CREATE INDEX idx_receipt_number ON receipts(tenant_id, receipt_number);
