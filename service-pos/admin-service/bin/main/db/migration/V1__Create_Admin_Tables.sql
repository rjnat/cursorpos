-- Migration: Create Admin Service tables
-- Author: rjnat
-- Date: 2025-11-13

-- Tenants table
CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    subdomain VARCHAR(50) UNIQUE,
    business_type VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    subscription_plan VARCHAR(50),
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    max_users INTEGER,
    max_stores INTEGER,
    max_branches INTEGER,
    logo_url VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(3) DEFAULT 'USD',
    locale VARCHAR(10) DEFAULT 'en_US',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_tenants_code ON tenants(code);
CREATE INDEX idx_tenants_subdomain ON tenants(subdomain);
CREATE INDEX idx_tenants_is_active ON tenants(is_active);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    customer_type VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    company_name VARCHAR(200),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    loyalty_points INTEGER DEFAULT 0,
    notes TEXT,
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
CREATE INDEX idx_customers_tenant_code ON customers(tenant_id, code);

-- Stores table
CREATE TABLE IF NOT EXISTS stores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_stores_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_stores_tenant_id ON stores(tenant_id);
CREATE INDEX idx_stores_tenant_code ON stores(tenant_id, code);

-- Branches table
CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    store_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    branch_type VARCHAR(50),
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
    CONSTRAINT uk_branches_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT fk_branches_store FOREIGN KEY (store_id) REFERENCES stores(id)
);

CREATE INDEX idx_branches_tenant_id ON branches(tenant_id);
CREATE INDEX idx_branches_store_id ON branches(store_id);
CREATE INDEX idx_branches_tenant_code ON branches(tenant_id, code);

-- Settings table
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
CREATE INDEX idx_settings_category ON settings(category);
CREATE INDEX idx_settings_tenant_key ON settings(tenant_id, setting_key);

-- Comments for documentation
COMMENT ON TABLE tenants IS 'Tenant/organization master data';
COMMENT ON TABLE customers IS 'Customer/client information for each tenant';
COMMENT ON TABLE stores IS 'Physical store locations for each tenant';
COMMENT ON TABLE branches IS 'Branches/departments within stores';
COMMENT ON TABLE settings IS 'Tenant-specific configuration settings';
