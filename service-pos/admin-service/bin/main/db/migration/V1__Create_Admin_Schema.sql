-- ============================================================================
-- Migration: V1__Create_Admin_Schema.sql
-- Description: Create Admin Service core tables
-- Author: rjnat
-- Date: 2025-12-04
-- ============================================================================

-- ============================================================================
-- SUBSCRIPTION PLANS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    max_users INTEGER,
    max_stores INTEGER,
    max_products INTEGER,
    price_monthly DECIMAL(10, 2) NOT NULL DEFAULT 0,
    price_yearly DECIMAL(10, 2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER NOT NULL DEFAULT 0,
    features JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_subscription_plans_code ON subscription_plans(code);
CREATE INDEX idx_subscription_plans_is_active ON subscription_plans(is_active);

-- Insert default subscription plans
INSERT INTO subscription_plans (code, name, description, max_users, max_stores, max_products, price_monthly, price_yearly, display_order, features) VALUES
('FREE', 'Free', 'Perfect for getting started', 1, 1, 50, 0, 0, 1, '{"support": "email", "analytics": false, "api_access": false}'),
('BASIC', 'Basic', 'For small businesses', 50, 10, 200, 29.99, 299.99, 2, '{"support": "email", "analytics": true, "api_access": false}'),
('PREMIUM', 'Premium', 'For growing businesses', 100, 50, 500, 79.99, 799.99, 3, '{"support": "priority", "analytics": true, "api_access": true}'),
('ENTERPRISE', 'Enterprise', 'For large organizations', NULL, NULL, NULL, 199.99, 1999.99, 4, '{"support": "dedicated", "analytics": true, "api_access": true, "custom_integrations": true}');

-- ============================================================================
-- TENANTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    subdomain VARCHAR(50) UNIQUE,
    business_type VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Subscription fields
    subscription_plan_id UUID REFERENCES subscription_plans(id),
    subscription_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    
    -- Localization
    logo_url VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(3) DEFAULT 'USD',
    locale VARCHAR(10) DEFAULT 'en_US',
    
    -- Loyalty configuration
    loyalty_points_per_currency DECIMAL(5, 2) DEFAULT 1.0,
    loyalty_enabled BOOLEAN NOT NULL DEFAULT true,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_tenants_code ON tenants(code);
CREATE INDEX idx_tenants_tenant_id ON tenants(tenant_id);
CREATE INDEX idx_tenants_subdomain ON tenants(subdomain);
CREATE INDEX idx_tenants_is_active ON tenants(is_active);
CREATE INDEX idx_tenants_subscription_plan_id ON tenants(subscription_plan_id);
CREATE INDEX idx_tenants_subscription_status ON tenants(subscription_status);

-- ============================================================================
-- LOYALTY TIERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS loyalty_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    min_points INTEGER NOT NULL DEFAULT 0,
    discount_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0,
    points_multiplier DECIMAL(5, 2) NOT NULL DEFAULT 1.0,
    color VARCHAR(20),
    icon VARCHAR(50),
    benefits JSONB,
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_loyalty_tiers_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_loyalty_tiers_tenant_id ON loyalty_tiers(tenant_id);
CREATE INDEX idx_loyalty_tiers_min_points ON loyalty_tiers(tenant_id, min_points);

-- ============================================================================
-- BRANCHES TABLE (Parent level in hierarchy)
-- ============================================================================
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    manager_name VARCHAR(100),
    manager_email VARCHAR(255),
    manager_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_branches_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_branches_tenant_id ON branches(tenant_id);
CREATE INDEX idx_branches_is_active ON branches(tenant_id, is_active);

-- ============================================================================
-- STORES TABLE (Child of Branch in hierarchy)
-- ============================================================================
CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    branch_id UUID NOT NULL REFERENCES branches(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    store_type VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_active BOOLEAN NOT NULL DEFAULT true,
    manager_name VARCHAR(100),
    manager_email VARCHAR(255),
    manager_phone VARCHAR(20),
    operating_hours VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(3) DEFAULT 'USD',
    tax_rate DECIMAL(5, 2) DEFAULT 0,
    global_discount_percentage DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_stores_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_stores_tenant_id ON stores(tenant_id);
CREATE INDEX idx_stores_branch_id ON stores(branch_id);
CREATE INDEX idx_stores_is_active ON stores(tenant_id, is_active);

-- ============================================================================
-- STORE PRICE OVERRIDES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS store_price_overrides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    store_id UUID NOT NULL REFERENCES stores(id),
    product_id UUID NOT NULL,
    override_price DECIMAL(15, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2),
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_store_price_override UNIQUE (tenant_id, store_id, product_id, effective_from)
);

CREATE INDEX idx_store_price_overrides_tenant_store ON store_price_overrides(tenant_id, store_id);
CREATE INDEX idx_store_price_overrides_product ON store_price_overrides(tenant_id, product_id);
CREATE INDEX idx_store_price_overrides_effective ON store_price_overrides(tenant_id, store_id, effective_from, effective_to);

-- ============================================================================
-- CUSTOMERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    date_of_birth DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    notes TEXT,
    
    -- Loyalty fields
    loyalty_tier_id UUID REFERENCES loyalty_tiers(id),
    total_points INTEGER NOT NULL DEFAULT 0,
    available_points INTEGER NOT NULL DEFAULT 0,
    lifetime_points INTEGER NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_customers_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_customers_tenant_id ON customers(tenant_id);
CREATE INDEX idx_customers_tenant_email ON customers(tenant_id, email);
CREATE INDEX idx_customers_tenant_phone ON customers(tenant_id, phone);
CREATE INDEX idx_customers_loyalty_tier ON customers(tenant_id, loyalty_tier_id);
CREATE INDEX idx_customers_total_points ON customers(tenant_id, total_points);

-- ============================================================================
-- LOYALTY TRANSACTIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    transaction_type VARCHAR(20) NOT NULL,
    points INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36)
);

CREATE INDEX idx_loyalty_transactions_tenant_customer ON loyalty_transactions(tenant_id, customer_id);
CREATE INDEX idx_loyalty_transactions_type ON loyalty_transactions(tenant_id, transaction_type);
CREATE INDEX idx_loyalty_transactions_reference ON loyalty_transactions(tenant_id, reference_type, reference_id);
CREATE INDEX idx_loyalty_transactions_created_at ON loyalty_transactions(tenant_id, created_at);

-- ============================================================================
-- SETTINGS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    category VARCHAR(50) NOT NULL,
    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description VARCHAR(500),
    is_system BOOLEAN NOT NULL DEFAULT false,
    is_encrypted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_settings_tenant_key UNIQUE (tenant_id, setting_key)
);

CREATE INDEX idx_settings_tenant_id ON settings(tenant_id);
CREATE INDEX idx_settings_category ON settings(tenant_id, category);

-- ============================================================================
-- COMMENTS
-- ============================================================================
COMMENT ON TABLE subscription_plans IS 'Available subscription plans with limits and pricing';
COMMENT ON TABLE tenants IS 'Tenant/organization master data with subscription info';
COMMENT ON TABLE loyalty_tiers IS 'Loyalty program tiers with benefits per tenant';
COMMENT ON TABLE branches IS 'Branch/region groupings within a tenant';
COMMENT ON TABLE stores IS 'Physical store locations under branches';
COMMENT ON TABLE store_price_overrides IS 'Store-specific product price overrides';
COMMENT ON TABLE customers IS 'Customer information with loyalty data';
COMMENT ON TABLE loyalty_transactions IS 'Point earn/redeem history for customers';
COMMENT ON TABLE settings IS 'Tenant-specific configuration settings';
