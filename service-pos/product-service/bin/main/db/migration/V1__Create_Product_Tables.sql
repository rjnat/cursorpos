-- Product Service Database Schema
-- Version: 1.0.0
-- Author: rjnat
-- Date: 2025-11-13

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    parent_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    category_id UUID,
    price DECIMAL(19,4) NOT NULL,
    cost DECIMAL(19,4),
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    unit VARCHAR(50),
    barcode VARCHAR(100),
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_trackable BOOLEAN NOT NULL DEFAULT true,
    min_stock_level INTEGER,
    max_stock_level INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    product_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    quantity_on_hand INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    quantity_available INTEGER NOT NULL DEFAULT 0,
    reorder_point INTEGER,
    reorder_quantity INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Price History table
CREATE TABLE IF NOT EXISTS price_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    product_id UUID NOT NULL,
    old_price DECIMAL(19,4),
    new_price DECIMAL(19,4) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    changed_by VARCHAR(100),
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_price_history_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Indexes for categories
CREATE INDEX IF NOT EXISTS idx_category_tenant ON categories(tenant_id);
CREATE INDEX IF NOT EXISTS idx_category_code ON categories(tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_category_parent ON categories(tenant_id, parent_id);
CREATE INDEX IF NOT EXISTS idx_category_active ON categories(tenant_id, is_active, deleted_at);

-- Indexes for products
CREATE INDEX IF NOT EXISTS idx_product_tenant ON products(tenant_id);
CREATE INDEX IF NOT EXISTS idx_product_code ON products(tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_product_sku ON products(tenant_id, sku);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(tenant_id, category_id);
CREATE INDEX IF NOT EXISTS idx_product_barcode ON products(tenant_id, barcode);
CREATE INDEX IF NOT EXISTS idx_product_active ON products(tenant_id, is_active, deleted_at);
CREATE INDEX IF NOT EXISTS idx_product_search ON products(tenant_id, deleted_at) WHERE deleted_at IS NULL;

-- Indexes for inventory
CREATE INDEX IF NOT EXISTS idx_inventory_tenant ON inventory(tenant_id);
CREATE INDEX IF NOT EXISTS idx_inventory_product ON inventory(tenant_id, product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_branch ON inventory(tenant_id, branch_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_inventory_product_branch ON inventory(tenant_id, product_id, branch_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory(tenant_id, deleted_at) WHERE deleted_at IS NULL AND quantity_available < reorder_point;

-- Indexes for price_history
CREATE INDEX IF NOT EXISTS idx_price_history_tenant ON price_history(tenant_id);
CREATE INDEX IF NOT EXISTS idx_price_history_product ON price_history(tenant_id, product_id);
CREATE INDEX IF NOT EXISTS idx_price_history_effective ON price_history(tenant_id, product_id, effective_from);
CREATE INDEX IF NOT EXISTS idx_price_history_current ON price_history(tenant_id, product_id, effective_from, effective_to) WHERE effective_to IS NULL;

-- Comments
COMMENT ON TABLE categories IS 'Product categories for organizing products hierarchically';
COMMENT ON TABLE products IS 'Product catalog with pricing and inventory settings';
COMMENT ON TABLE inventory IS 'Product inventory levels per branch location';
COMMENT ON TABLE price_history IS 'Historical record of product price changes';
