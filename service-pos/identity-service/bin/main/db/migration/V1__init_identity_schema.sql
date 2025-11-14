-- Identity Service Database Schema
-- Version: 1.0.0
-- Description: Users, roles, permissions tables

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE UNIQUE INDEX idx_users_tenant_email ON users(tenant_id, email) WHERE deleted_at IS NULL;

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_system BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE UNIQUE INDEX idx_roles_tenant_code ON roles(tenant_id, code) WHERE deleted_at IS NULL;

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE UNIQUE INDEX idx_permissions_tenant_code ON permissions(tenant_id, code) WHERE deleted_at IS NULL;

-- User Roles mapping table
CREATE TABLE IF NOT EXISTS user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
CREATE UNIQUE INDEX idx_user_roles_unique ON user_roles(user_id, role_id) WHERE deleted_at IS NULL;

-- Role Permissions mapping table
CREATE TABLE IF NOT EXISTS role_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(36) NOT NULL,
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP,
    updated_by VARCHAR(36),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);
CREATE UNIQUE INDEX idx_role_permissions_unique ON role_permissions(role_id, permission_id) WHERE deleted_at IS NULL;

-- Insert default system roles (for system tenant)
INSERT INTO roles (tenant_id, code, name, description, is_system, is_active, created_by)
VALUES 
    ('SYSTEM', 'SUPER_ADMIN', 'Super Administrator', 'Full system access', true, true, 'SYSTEM'),
    ('SYSTEM', 'TENANT_ADMIN', 'Tenant Administrator', 'Full tenant access', true, true, 'SYSTEM'),
    ('SYSTEM', 'STORE_MANAGER', 'Store Manager', 'Manage store operations', true, true, 'SYSTEM'),
    ('SYSTEM', 'CASHIER', 'Cashier', 'Process transactions', true, true, 'SYSTEM'),
    ('SYSTEM', 'INVENTORY_MANAGER', 'Inventory Manager', 'Manage inventory', true, true, 'SYSTEM')
ON CONFLICT DO NOTHING;
