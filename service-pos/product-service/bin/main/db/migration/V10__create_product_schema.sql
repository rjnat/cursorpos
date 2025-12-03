-- Product Service Database Schema
-- Version: 1.0
-- Description: Creates tables for product catalog, inventory, and stock management

-- Categories table (supports hierarchical categories)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    parent_id UUID,
    is_active BOOLEAN DEFAULT true,
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_id) REFERENCES categories(id),
    CONSTRAINT uq_category_code_per_tenant UNIQUE (tenant_id, code)
);

-- Products table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    category_id UUID,
    price DECIMAL(19, 4) NOT NULL CHECK (price >= 0),
    cost DECIMAL(19, 4),
    tax_rate DECIMAL(5, 2) DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
    unit VARCHAR(50),
    barcode VARCHAR(100),
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    is_trackable BOOLEAN DEFAULT true,
    min_stock_level INTEGER,
    max_stock_level INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT uq_product_code_per_tenant UNIQUE (tenant_id, code),
    CONSTRAINT uq_product_sku_per_tenant UNIQUE (tenant_id, sku)
);

-- Inventory table (stock levels per branch)
CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    branch_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity_on_hand INTEGER DEFAULT 0 CHECK (quantity_on_hand >= 0),
    quantity_reserved INTEGER DEFAULT 0 CHECK (quantity_reserved >= 0),
    quantity_available INTEGER DEFAULT 0 CHECK (quantity_available >= 0),
    reorder_point INTEGER,
    reorder_level INTEGER DEFAULT 0,
    reorder_quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_inventory_per_branch UNIQUE (tenant_id, branch_id, product_id),
    CONSTRAINT chk_reserved_not_exceed_quantity CHECK (quantity_reserved <= quantity_on_hand)
);

-- Price history table (tracks product price changes)
CREATE TABLE price_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    product_id UUID NOT NULL,
    old_price DECIMAL(19, 4),
    new_price DECIMAL(19, 4) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    changed_by VARCHAR(100),
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_price_history_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Stock movements table (audit trail for all inventory changes)
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(100) NOT NULL,
    store_id UUID NOT NULL,
    product_id UUID NOT NULL,
    movement_type VARCHAR(20) NOT NULL CHECK (movement_type IN ('SALE', 'RESTOCK', 'ADJUSTMENT', 'TRANSFER_IN', 'TRANSFER_OUT', 'RESERVE', 'RELEASE', 'RETURN')),
    quantity_delta INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_order_id UUID,
    reference_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    
    CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Indexes for performance
CREATE INDEX idx_categories_tenant ON categories(tenant_id);
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_products_tenant ON products(tenant_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(tenant_id, sku);
CREATE INDEX idx_products_barcode ON products(tenant_id, barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_products_name ON products(tenant_id, name);
CREATE INDEX idx_inventory_tenant_branch ON inventory(tenant_id, branch_id);
CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_tenant_branch_product ON inventory(tenant_id, branch_id, product_id);
CREATE INDEX idx_stock_movements_tenant_store ON stock_movements(tenant_id, store_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_reference ON stock_movements(reference_number);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at DESC);

-- Comments
COMMENT ON TABLE categories IS 'Product categories with support for hierarchical structure';
COMMENT ON TABLE products IS 'Product catalog with pricing and basic information';
COMMENT ON TABLE inventory IS 'Stock levels per store with reserved quantity for pending orders';
COMMENT ON TABLE stock_movements IS 'Audit trail for all inventory changes';
COMMENT ON COLUMN products.price IS 'Selling price before any discounts or promotions';
COMMENT ON COLUMN products.tax_rate IS 'Tax rate as percentage (0-100)';
COMMENT ON COLUMN inventory.quantity_reserved IS 'Quantity reserved for pending orders (not yet committed)';
COMMENT ON COLUMN stock_movements.quantity_delta IS 'Change in quantity (positive for increase, negative for decrease)';
COMMENT ON COLUMN stock_movements.quantity_after IS 'Total quantity after this movement';
