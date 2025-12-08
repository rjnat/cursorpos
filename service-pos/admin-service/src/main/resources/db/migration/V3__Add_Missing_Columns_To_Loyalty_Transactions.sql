-- ============================================================================
-- V3: Add missing audit columns to loyalty_transactions table
-- ============================================================================
-- The loyalty_transactions table was missing the standard BaseEntity audit 
-- columns (updated_at, updated_by, deleted_at, version) which are required
-- for JPA entity mapping.
-- ============================================================================

ALTER TABLE loyalty_transactions
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36),
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Create index for soft delete queries
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_deleted_at 
    ON loyalty_transactions(tenant_id, deleted_at);

COMMENT ON COLUMN loyalty_transactions.updated_at IS 'Timestamp of last update';
COMMENT ON COLUMN loyalty_transactions.updated_by IS 'User who last updated the record';
COMMENT ON COLUMN loyalty_transactions.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN loyalty_transactions.version IS 'Optimistic locking version';
