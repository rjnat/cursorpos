-- Identity Service Seed Data
-- Version: 2.0.0
-- Description: Test data for POS Terminal testing (tenants, users, roles, permissions)

-- =====================================================
-- Test Tenants (tenant_id will be used across all services)
-- =====================================================
-- Note: Tenants are managed by Admin Service, but we need tenant_id for users
-- These are just tenant IDs that will exist in admin-service tenants table

-- =====================================================
-- Roles for Test Tenants
-- =====================================================
-- Tenant 1: Coffee Shop
INSERT INTO roles (id, tenant_id, code, name, description, is_system, is_active, created_by)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'tenant-coffee-001', 'ADMIN', 'Administrator', 'Full tenant access', false, true, 'SYSTEM'),
    ('10000000-0000-0000-0000-000000000002', 'tenant-coffee-001', 'MANAGER', 'Manager', 'Store management and approvals', false, true, 'SYSTEM'),
    ('10000000-0000-0000-0000-000000000003', 'tenant-coffee-001', 'CASHIER', 'Cashier', 'Process sales transactions', false, true, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- Tenant 2: Restaurant
INSERT INTO roles (id, tenant_id, code, name, description, is_system, is_active, created_by)
VALUES 
    ('20000000-0000-0000-0000-000000000001', 'tenant-restaurant-001', 'ADMIN', 'Administrator', 'Full tenant access', false, true, 'SYSTEM'),
    ('20000000-0000-0000-0000-000000000002', 'tenant-restaurant-001', 'MANAGER', 'Manager', 'Store management and approvals', false, true, 'SYSTEM'),
    ('20000000-0000-0000-0000-000000000003', 'tenant-restaurant-001', 'CASHIER', 'Cashier', 'Process sales transactions', false, true, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- Permissions
-- =====================================================
INSERT INTO permissions (id, tenant_id, code, name, description, resource, action, is_system, created_by)
VALUES 
    -- Tenant 1 Permissions
    ('11000000-0000-0000-0000-000000000001', 'tenant-coffee-001', 'PRODUCTS_READ', 'Read Products', 'View product information', 'products', 'read', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000002', 'tenant-coffee-001', 'PRODUCTS_WRITE', 'Write Products', 'Create/update products', 'products', 'write', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000003', 'tenant-coffee-001', 'TRANSACTIONS_READ', 'Read Transactions', 'View transactions', 'transactions', 'read', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000004', 'tenant-coffee-001', 'TRANSACTIONS_CREATE', 'Create Transactions', 'Process sales', 'transactions', 'create', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000005', 'tenant-coffee-001', 'TRANSACTIONS_VOID', 'Void Transactions', 'Cancel transactions', 'transactions', 'void', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000006', 'tenant-coffee-001', 'DISCOUNTS_APPLY', 'Apply Discounts', 'Apply pre-configured discounts', 'discounts', 'apply', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000007', 'tenant-coffee-001', 'DISCOUNTS_APPROVE', 'Approve Discounts', 'Approve manual discounts', 'discounts', 'approve', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000008', 'tenant-coffee-001', 'CASH_DRAWER_MANAGE', 'Manage Cash Drawer', 'Open/close cash drawer', 'cash_drawer', 'manage', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000009', 'tenant-coffee-001', 'CUSTOMERS_READ', 'Read Customers', 'View customer info', 'customers', 'read', false, 'SYSTEM'),
    ('11000000-0000-0000-0000-000000000010', 'tenant-coffee-001', 'CUSTOMERS_WRITE', 'Write Customers', 'Create/update customers', 'customers', 'write', false, 'SYSTEM'),
    
    -- Tenant 2 Permissions
    ('21000000-0000-0000-0000-000000000001', 'tenant-restaurant-001', 'PRODUCTS_READ', 'Read Products', 'View product information', 'products', 'read', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000002', 'tenant-restaurant-001', 'PRODUCTS_WRITE', 'Write Products', 'Create/update products', 'products', 'write', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000003', 'tenant-restaurant-001', 'TRANSACTIONS_READ', 'Read Transactions', 'View transactions', 'transactions', 'read', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000004', 'tenant-restaurant-001', 'TRANSACTIONS_CREATE', 'Create Transactions', 'Process sales', 'transactions', 'create', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000005', 'tenant-restaurant-001', 'TRANSACTIONS_VOID', 'Void Transactions', 'Cancel transactions', 'transactions', 'void', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000006', 'tenant-restaurant-001', 'DISCOUNTS_APPLY', 'Apply Discounts', 'Apply pre-configured discounts', 'discounts', 'apply', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000007', 'tenant-restaurant-001', 'DISCOUNTS_APPROVE', 'Approve Discounts', 'Approve manual discounts', 'discounts', 'approve', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000008', 'tenant-restaurant-001', 'CASH_DRAWER_MANAGE', 'Manage Cash Drawer', 'Open/close cash drawer', 'cash_drawer', 'manage', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000009', 'tenant-restaurant-001', 'CUSTOMERS_READ', 'Read Customers', 'View customer info', 'customers', 'read', false, 'SYSTEM'),
    ('21000000-0000-0000-0000-000000000010', 'tenant-restaurant-001', 'CUSTOMERS_WRITE', 'Write Customers', 'Create/update customers', 'customers', 'write', false, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- Role-Permission Mappings
-- =====================================================
-- Tenant 1: Coffee Shop
-- ADMIN role - all permissions
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_by)
SELECT 'tenant-coffee-001', '10000000-0000-0000-0000-000000000001', id, 'SYSTEM'
FROM permissions WHERE tenant_id = 'tenant-coffee-001'
ON CONFLICT DO NOTHING;

-- MANAGER role - read products, create/read/void transactions, approve discounts, manage cash drawer, read customers
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_by)
VALUES 
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000001', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000003', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000004', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000005', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000006', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000007', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000008', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000009', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- CASHIER role - read products, create transactions, apply discounts, manage cash drawer, read customers
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_by)
VALUES 
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000001', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000003', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000004', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000006', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000008', 'SYSTEM'),
    ('tenant-coffee-001', '10000000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000009', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- Tenant 2: Restaurant (same permission structure)
-- ADMIN role - all permissions
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_by)
SELECT 'tenant-restaurant-001', '20000000-0000-0000-0000-000000000001', id, 'SYSTEM'
FROM permissions WHERE tenant_id = 'tenant-restaurant-001'
ON CONFLICT DO NOTHING;

-- MANAGER role
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_by)
VALUES 
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000001', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000003', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000004', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000005', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000006', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000007', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000008', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000009', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- CASHIER role
INSERT INTO role_permissions (tenant_id, role_id, permission_id, created_by)
VALUES 
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000001', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000003', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000004', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000006', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000008', 'SYSTEM'),
    ('tenant-restaurant-001', '20000000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000009', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- =====================================================
-- Test Users
-- =====================================================
-- Password for all test users: Test@123456 (BCrypt hash with strength 10)
-- Generated using: BCryptPasswordEncoder(10).encode("Test@123456")

-- Tenant 1: Coffee Shop Users
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, phone, is_active, email_verified, created_by)
VALUES 
    -- Admin
    ('10100000-0000-0000-0000-000000000001', 'tenant-coffee-001', 'admin@coffee.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Admin', 'Coffee', '+62811111111', true, true, 'SYSTEM'),
    -- Manager
    ('10100000-0000-0000-0000-000000000002', 'tenant-coffee-001', 'manager@coffee.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Manager', 'Coffee', '+62811111112', true, true, 'SYSTEM'),
    -- Cashier 1
    ('10100000-0000-0000-0000-000000000003', 'tenant-coffee-001', 'cashier1@coffee.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Cashier', 'One', '+62811111113', true, true, 'SYSTEM'),
    -- Cashier 2
    ('10100000-0000-0000-0000-000000000004', 'tenant-coffee-001', 'cashier2@coffee.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Cashier', 'Two', '+62811111114', true, true, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- Tenant 2: Restaurant Users
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, phone, is_active, email_verified, created_by)
VALUES 
    -- Admin
    ('20100000-0000-0000-0000-000000000001', 'tenant-restaurant-001', 'admin@restaurant.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Admin', 'Restaurant', '+62822222221', true, true, 'SYSTEM'),
    -- Manager
    ('20100000-0000-0000-0000-000000000002', 'tenant-restaurant-001', 'manager@restaurant.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Manager', 'Restaurant', '+62822222222', true, true, 'SYSTEM'),
    -- Cashier 1
    ('20100000-0000-0000-0000-000000000003', 'tenant-restaurant-001', 'cashier1@restaurant.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Cashier', 'One', '+62822222223', true, true, 'SYSTEM'),
    -- Cashier 2
    ('20100000-0000-0000-0000-000000000004', 'tenant-restaurant-001', 'cashier2@restaurant.test', '$2a$10$lN6er8mNeu4zzyklw6Ak0.mqnVpZ8UYfZ90bB9ConieQIfr5LWwvG', 'Cashier', 'Two', '+62822222224', true, true, 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- User-Role Mappings
-- =====================================================
-- Tenant 1: Coffee Shop
INSERT INTO user_roles (tenant_id, user_id, role_id, created_by)
VALUES 
    -- Admin user -> ADMIN role
    ('tenant-coffee-001', '10100000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'SYSTEM'),
    -- Manager user -> MANAGER role
    ('tenant-coffee-001', '10100000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'SYSTEM'),
    -- Cashier 1 -> CASHIER role
    ('tenant-coffee-001', '10100000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 'SYSTEM'),
    -- Cashier 2 -> CASHIER role
    ('tenant-coffee-001', '10100000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', 'SYSTEM')
ON CONFLICT DO NOTHING;

-- Tenant 2: Restaurant
INSERT INTO user_roles (tenant_id, user_id, role_id, created_by)
VALUES 
    -- Admin user -> ADMIN role
    ('tenant-restaurant-001', '20100000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'SYSTEM'),
    -- Manager user -> MANAGER role
    ('tenant-restaurant-001', '20100000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'SYSTEM'),
    -- Cashier 1 -> CASHIER role
    ('tenant-restaurant-001', '20100000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003', 'SYSTEM'),
    -- Cashier 2 -> CASHIER role
    ('tenant-restaurant-001', '20100000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000003', 'SYSTEM')
ON CONFLICT DO NOTHING;
