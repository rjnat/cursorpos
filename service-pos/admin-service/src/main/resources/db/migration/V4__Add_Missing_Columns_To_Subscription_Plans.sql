-- ============================================================================
-- V4: Add Missing BaseEntity Columns to Subscription Plans
-- ============================================================================
-- Add missing columns that BaseEntity requires for JPA compatibility

-- Add tenant_id column (although subscription_plans is a system table, BaseEntity requires it)
ALTER TABLE subscription_plans
ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(100);

-- Add created_by column
ALTER TABLE subscription_plans
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

-- Add updated_by column  
ALTER TABLE subscription_plans
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Add deleted_at column for soft delete support
ALTER TABLE subscription_plans
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Update existing records with default values
UPDATE subscription_plans 
SET tenant_id = 'SYSTEM', 
    created_by = 'SYSTEM'
WHERE tenant_id IS NULL;

-- Make tenant_id NOT NULL after setting default values
ALTER TABLE subscription_plans
ALTER COLUMN tenant_id SET NOT NULL;
