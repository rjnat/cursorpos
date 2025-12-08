-- ============================================================================
-- Migration: V2__Seed_Admin_Test_Data.sql
-- Description: Insert test data for Admin Service
-- Author: rjnat
-- Date: 2025-12-04
-- ============================================================================

-- ============================================================================
-- SEED TENANT: Coffee Shop (tenant-coffee-001)
-- ============================================================================

-- Get subscription plan ID for BASIC
DO $$
DECLARE
    v_basic_plan_id UUID;
    v_premium_plan_id UUID;
    v_tenant_coffee_id UUID;
    v_tenant_restaurant_id UUID;
    v_branch_downtown_id UUID;
    v_branch_uptown_id UUID;
    v_branch_central_id UUID;
    v_store_main_id UUID;
    v_store_mall_id UUID;
    v_store_plaza_id UUID;
    v_tier_bronze_id UUID;
    v_tier_silver_id UUID;
    v_tier_gold_id UUID;
    v_tier_platinum_id UUID;
BEGIN
    -- Get plan IDs
    SELECT id INTO v_basic_plan_id FROM subscription_plans WHERE code = 'BASIC';
    SELECT id INTO v_premium_plan_id FROM subscription_plans WHERE code = 'PREMIUM';

    -- ========================================================================
    -- TENANT: Coffee Shop
    -- ========================================================================
    INSERT INTO tenants (
        id, tenant_id, code, name, subdomain, business_type, email, phone,
        address, city, state, country, postal_code, tax_id, is_active,
        subscription_plan_id, subscription_status, subscription_start_date, subscription_end_date,
        timezone, currency, locale, loyalty_points_per_currency, loyalty_enabled,
        created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-coffee-001', 'tenant-coffee-001', 'Coffee Shop Inc.',
        'coffeeshop', 'FOOD_BEVERAGE', 'admin@coffee.test', '+1-555-0100',
        '123 Main Street', 'New York', 'NY', 'USA', '10001', 'TAX-001',
        true, v_basic_plan_id, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 year',
        'America/New_York', 'USD', 'en_US', 1.0, true, 'system'
    ) RETURNING id INTO v_tenant_coffee_id;

    -- Loyalty Tiers for Coffee Shop
    INSERT INTO loyalty_tiers (tenant_id, code, name, min_points, discount_percentage, points_multiplier, color, display_order, created_by) VALUES
    ('tenant-coffee-001', 'BRONZE', 'Bronze', 0, 0, 1.0, '#CD7F32', 1, 'system') RETURNING id INTO v_tier_bronze_id;
    
    INSERT INTO loyalty_tiers (tenant_id, code, name, min_points, discount_percentage, points_multiplier, color, display_order, created_by) VALUES
    ('tenant-coffee-001', 'SILVER', 'Silver', 500, 5.0, 1.5, '#C0C0C0', 2, 'system') RETURNING id INTO v_tier_silver_id;
    
    INSERT INTO loyalty_tiers (tenant_id, code, name, min_points, discount_percentage, points_multiplier, color, display_order, created_by) VALUES
    ('tenant-coffee-001', 'GOLD', 'Gold', 2000, 10.0, 2.0, '#FFD700', 3, 'system') RETURNING id INTO v_tier_gold_id;
    
    INSERT INTO loyalty_tiers (tenant_id, code, name, min_points, discount_percentage, points_multiplier, color, display_order, created_by) VALUES
    ('tenant-coffee-001', 'PLATINUM', 'Platinum', 5000, 15.0, 3.0, '#E5E4E2', 4, 'system') RETURNING id INTO v_tier_platinum_id;

    -- Branches for Coffee Shop
    INSERT INTO branches (
        id, tenant_id, code, name, description, address, city, state, country,
        phone, email, is_active, manager_name, created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-coffee-001', 'DOWNTOWN', 'Downtown Region',
        'Downtown area stores', '100 Downtown Ave', 'New York', 'NY', 'USA',
        '+1-555-0101', 'downtown@coffee.test', true, 'John Smith', 'system'
    ) RETURNING id INTO v_branch_downtown_id;

    INSERT INTO branches (
        id, tenant_id, code, name, description, address, city, state, country,
        phone, email, is_active, manager_name, created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-coffee-001', 'UPTOWN', 'Uptown Region',
        'Uptown area stores', '200 Uptown Blvd', 'New York', 'NY', 'USA',
        '+1-555-0102', 'uptown@coffee.test', true, 'Jane Doe', 'system'
    ) RETURNING id INTO v_branch_uptown_id;

    -- Stores for Coffee Shop
    INSERT INTO stores (
        id, tenant_id, branch_id, code, name, description, store_type,
        address, city, state, country, postal_code, phone, email,
        is_active, manager_name, operating_hours, timezone, currency,
        tax_rate, global_discount_percentage, created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-coffee-001', v_branch_downtown_id,
        'MAIN-001', 'Main Street Store', 'Flagship store', 'FLAGSHIP',
        '123 Main Street', 'New York', 'NY', 'USA', '10001',
        '+1-555-0110', 'main@coffee.test', true, 'Alice Johnson',
        '{"mon":"07:00-21:00","tue":"07:00-21:00","wed":"07:00-21:00","thu":"07:00-21:00","fri":"07:00-22:00","sat":"08:00-22:00","sun":"08:00-20:00"}',
        'America/New_York', 'USD', 8.875, 0, 'system'
    ) RETURNING id INTO v_store_main_id;

    INSERT INTO stores (
        id, tenant_id, branch_id, code, name, description, store_type,
        address, city, state, country, postal_code, phone, email,
        is_active, manager_name, operating_hours, timezone, currency,
        tax_rate, global_discount_percentage, created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-coffee-001', v_branch_downtown_id,
        'MALL-001', 'Mall Kiosk', 'Shopping mall location', 'KIOSK',
        '456 Mall Plaza', 'New York', 'NY', 'USA', '10002',
        '+1-555-0111', 'mall@coffee.test', true, 'Bob Williams',
        '{"mon":"10:00-21:00","tue":"10:00-21:00","wed":"10:00-21:00","thu":"10:00-21:00","fri":"10:00-22:00","sat":"10:00-22:00","sun":"11:00-19:00"}',
        'America/New_York', 'USD', 8.875, 5.0, 'system'
    );

    INSERT INTO stores (
        id, tenant_id, branch_id, code, name, description, store_type,
        address, city, state, country, postal_code, phone, email,
        is_active, manager_name, operating_hours, timezone, currency,
        tax_rate, global_discount_percentage, created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-coffee-001', v_branch_uptown_id,
        'PLAZA-001', 'Plaza Store', 'Uptown plaza location', 'STANDARD',
        '789 Uptown Plaza', 'New York', 'NY', 'USA', '10003',
        '+1-555-0112', 'plaza@coffee.test', true, 'Carol Davis',
        '{"mon":"06:00-20:00","tue":"06:00-20:00","wed":"06:00-20:00","thu":"06:00-20:00","fri":"06:00-21:00","sat":"07:00-21:00","sun":"07:00-19:00"}',
        'America/New_York', 'USD', 8.875, 0, 'system'
    ) RETURNING id INTO v_store_plaza_id;

    -- Customers for Coffee Shop
    INSERT INTO customers (
        tenant_id, code, first_name, last_name, email, phone,
        address, city, state, country, postal_code,
        is_active, loyalty_tier_id, total_points, available_points, lifetime_points,
        created_by
    ) VALUES
    ('tenant-coffee-001', 'CUST-001', 'Michael', 'Brown', 'michael.brown@email.com', '+1-555-1001',
     '100 Customer St', 'New York', 'NY', 'USA', '10001',
     true, v_tier_bronze_id, 150, 150, 150, 'system'),
    ('tenant-coffee-001', 'CUST-002', 'Sarah', 'Wilson', 'sarah.wilson@email.com', '+1-555-1002',
     '200 Customer Ave', 'New York', 'NY', 'USA', '10002',
     true, v_tier_silver_id, 750, 600, 1200, 'system'),
    ('tenant-coffee-001', 'CUST-003', 'David', 'Taylor', 'david.taylor@email.com', '+1-555-1003',
     '300 Customer Blvd', 'New York', 'NY', 'USA', '10003',
     true, v_tier_gold_id, 2500, 2100, 3500, 'system'),
    ('tenant-coffee-001', 'CUST-004', 'Emily', 'Anderson', 'emily.anderson@email.com', '+1-555-1004',
     '400 Customer Dr', 'New York', 'NY', 'USA', '10004',
     true, v_tier_platinum_id, 6000, 5500, 8000, 'system');

    -- ========================================================================
    -- TENANT: Restaurant (tenant-restaurant-001)
    -- ========================================================================
    INSERT INTO tenants (
        id, tenant_id, code, name, subdomain, business_type, email, phone,
        address, city, state, country, postal_code, tax_id, is_active,
        subscription_plan_id, subscription_status, subscription_start_date, subscription_end_date,
        timezone, currency, locale, loyalty_points_per_currency, loyalty_enabled,
        created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-restaurant-001', 'tenant-restaurant-001', 'Restaurant Group LLC',
        'restaurant', 'RESTAURANT', 'admin@restaurant.test', '+1-555-0200',
        '500 Food Street', 'Los Angeles', 'CA', 'USA', '90001', 'TAX-002',
        true, v_premium_plan_id, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 year',
        'America/Los_Angeles', 'USD', 'en_US', 2.0, true, 'system'
    ) RETURNING id INTO v_tenant_restaurant_id;

    -- Loyalty Tiers for Restaurant
    INSERT INTO loyalty_tiers (tenant_id, code, name, min_points, discount_percentage, points_multiplier, color, display_order, created_by) VALUES
    ('tenant-restaurant-001', 'BRONZE', 'Bronze', 0, 0, 1.0, '#CD7F32', 1, 'system'),
    ('tenant-restaurant-001', 'SILVER', 'Silver', 500, 5.0, 1.5, '#C0C0C0', 2, 'system'),
    ('tenant-restaurant-001', 'GOLD', 'Gold', 2000, 10.0, 2.0, '#FFD700', 3, 'system'),
    ('tenant-restaurant-001', 'PLATINUM', 'Platinum', 5000, 15.0, 3.0, '#E5E4E2', 4, 'system');

    -- Branch for Restaurant
    INSERT INTO branches (
        id, tenant_id, code, name, description, address, city, state, country,
        phone, email, is_active, manager_name, created_by
    ) VALUES (
        gen_random_uuid(), 'tenant-restaurant-001', 'CENTRAL', 'Central Region',
        'Central LA stores', '500 Central Ave', 'Los Angeles', 'CA', 'USA',
        '+1-555-0201', 'central@restaurant.test', true, 'Tom Manager', 'system'
    ) RETURNING id INTO v_branch_central_id;

    -- Stores for Restaurant
    INSERT INTO stores (
        tenant_id, branch_id, code, name, description, store_type,
        address, city, state, country, postal_code, phone, email,
        is_active, manager_name, operating_hours, timezone, currency,
        tax_rate, global_discount_percentage, created_by
    ) VALUES
    ('tenant-restaurant-001', v_branch_central_id, 'REST-001', 'Downtown Restaurant',
     'Main dining location', 'RESTAURANT', '501 Food Street', 'Los Angeles', 'CA', 'USA', '90001',
     '+1-555-0210', 'downtown@restaurant.test', true, 'Chef Gordon',
     '{"mon":"11:00-22:00","tue":"11:00-22:00","wed":"11:00-22:00","thu":"11:00-22:00","fri":"11:00-23:00","sat":"10:00-23:00","sun":"10:00-21:00"}',
     'America/Los_Angeles', 'USD', 9.5, 0, 'system'),
    ('tenant-restaurant-001', v_branch_central_id, 'REST-002', 'Beach Restaurant',
     'Beachside location', 'RESTAURANT', '502 Beach Blvd', 'Santa Monica', 'CA', 'USA', '90402',
     '+1-555-0211', 'beach@restaurant.test', true, 'Chef Julia',
     '{"mon":"11:00-21:00","tue":"11:00-21:00","wed":"11:00-21:00","thu":"11:00-21:00","fri":"11:00-22:00","sat":"10:00-22:00","sun":"10:00-20:00"}',
     'America/Los_Angeles', 'USD', 9.5, 10.0, 'system');

    -- Customers for Restaurant
    INSERT INTO customers (
        tenant_id, code, first_name, last_name, email, phone,
        address, city, state, country, postal_code, is_active,
        total_points, available_points, lifetime_points, created_by
    ) VALUES
    ('tenant-restaurant-001', 'RCUST-001', 'James', 'Martin', 'james.martin@email.com', '+1-555-2001',
     '100 Diner St', 'Los Angeles', 'CA', 'USA', '90001', true, 200, 200, 200, 'system'),
    ('tenant-restaurant-001', 'RCUST-002', 'Jennifer', 'Garcia', 'jennifer.garcia@email.com', '+1-555-2002',
     '200 Diner Ave', 'Los Angeles', 'CA', 'USA', '90002', true, 1000, 800, 1500, 'system');

    -- Settings for tenants
    INSERT INTO settings (tenant_id, category, setting_key, setting_value, value_type, description, is_system, created_by) VALUES
    ('tenant-coffee-001', 'RECEIPT', 'receipt_header', 'Welcome to Coffee Shop!', 'STRING', 'Receipt header text', false, 'system'),
    ('tenant-coffee-001', 'RECEIPT', 'receipt_footer', 'Thank you for your purchase!', 'STRING', 'Receipt footer text', false, 'system'),
    ('tenant-coffee-001', 'LOYALTY', 'points_expiry_days', '365', 'INTEGER', 'Days until points expire', false, 'system'),
    ('tenant-coffee-001', 'GENERAL', 'auto_logout_minutes', '30', 'INTEGER', 'Auto logout after inactivity', false, 'system'),
    ('tenant-restaurant-001', 'RECEIPT', 'receipt_header', 'Welcome to Restaurant Group!', 'STRING', 'Receipt header text', false, 'system'),
    ('tenant-restaurant-001', 'RECEIPT', 'receipt_footer', 'Please come again!', 'STRING', 'Receipt footer text', false, 'system'),
    ('tenant-restaurant-001', 'LOYALTY', 'points_expiry_days', '180', 'INTEGER', 'Days until points expire', false, 'system');

END $$;
