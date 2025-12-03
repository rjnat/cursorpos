-- Product Service Test Data
-- Simplified seed data for testing

-- Categories
INSERT INTO categories (id, tenant_id, code, name, description, parent_id, created_by) VALUES
('10000001-0001-0000-0000-000000000001', 'tenant-coffee-001', 'HOT-DRINKS', 'Hot Drinks', 'Hot beverages', NULL, 'SYSTEM'),
('10000001-0001-0000-0000-000000000002', 'tenant-coffee-001', 'COLD-DRINKS', 'Cold Drinks', 'Cold beverages', NULL, 'SYSTEM'),
('10000001-0001-0000-0000-000000000003', 'tenant-coffee-001', 'FOOD', 'Food', 'Food items', NULL, 'SYSTEM');

-- Products
INSERT INTO products (id, tenant_id, code, sku, name, description, category_id, price, tax_rate, cost, created_by) VALUES
('20000001-0001-0000-0000-000000000001', 'tenant-coffee-001', 'PROD-ESP-001', 'COFFEE-ESP-001', 'Espresso', 'Single shot espresso', '10000001-0001-0000-0000-000000000001', 25000, 10, 8000, 'SYSTEM'),
('20000001-0001-0000-0000-000000000002', 'tenant-coffee-001', 'PROD-LAT-001', 'COFFEE-LAT-001', 'Latte', 'Espresso with steamed milk', '10000001-0001-0000-0000-000000000001', 35000, 10, 12000, 'SYSTEM'),
('20000001-0001-0000-0000-000000000003', 'tenant-coffee-001', 'PROD-ICE-001', 'COFFEE-ICE-001', 'Iced Coffee', 'Iced coffee', '10000001-0001-0000-0000-000000000002', 30000, 10, 10000, 'SYSTEM');

-- Inventory
INSERT INTO inventory (tenant_id, branch_id, product_id, quantity_on_hand, quantity_reserved, quantity_available, reorder_point, reorder_level, reorder_quantity) VALUES
('tenant-coffee-001', '30000001-0001-0000-0000-000000000001', '20000001-0001-0000-0000-000000000001', 100, 0, 100, 20, 20, 50),
('tenant-coffee-001', '30000001-0001-0000-0000-000000000001', '20000001-0001-0000-0000-000000000002', 80, 0, 80, 15, 15, 40),
('tenant-coffee-001', '30000001-0001-0000-0000-000000000001', '20000001-0001-0000-0000-000000000003', 60, 0, 60, 15, 15, 40);
