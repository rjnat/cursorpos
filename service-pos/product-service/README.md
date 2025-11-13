# Product Service

Product Service manages product catalog, categories, inventory, and pricing for the CursorPOS system.

## Features

- **Product Management**: Create, update, and manage products with SKU, barcode, and categorization
- **Category Management**: Hierarchical product categories with parent-child relationships
- **Inventory Management**: Track stock levels per branch with reorder points and reservations
- **Price History**: Automatic tracking of price changes with effective dates
- **Multi-tenant**: Full tenant isolation for all product data
- **Soft Deletes**: Audit trail preservation for deleted products and categories

## Architecture

### Entities

1. **Category**
   - Hierarchical product categorization
   - Parent-child relationships
   - Active/inactive status
   - Display ordering

2. **Product**
   - Full product information (name, SKU, barcode)
   - Pricing with tax rates
   - Cost tracking for profit analysis
   - Stock level thresholds (min/max)
   - Trackable/non-trackable items

3. **Inventory**
   - Branch-level stock tracking
   - Quantity on hand, reserved, and available
   - Reorder points and quantities
   - Automatic availability calculation

4. **PriceHistory**
   - Automatic price change recording
   - Effective date ranges
   - Change reason tracking
   - Historical price queries

### Key Features

- **Stock Adjustments**: Add, subtract, or set inventory levels
- **Stock Reservations**: Reserve and release stock for orders
- **Low Stock Alerts**: Query items below reorder points
- **Product Search**: Search by name, code, or SKU
- **Barcode Lookup**: Fast product lookup by barcode

## API Endpoints

### Categories

- `POST /api/v1/categories` - Create category
- `GET /api/v1/categories/{id}` - Get category by ID
- `GET /api/v1/categories/code/{code}` - Get category by code
- `GET /api/v1/categories` - List all categories (paginated)
- `GET /api/v1/categories/{parentId}/subcategories` - Get subcategories
- `GET /api/v1/categories/active` - Get active categories
- `PUT /api/v1/categories/{id}` - Update category
- `DELETE /api/v1/categories/{id}` - Delete category (soft)

### Products

- `POST /api/v1/products` - Create product
- `GET /api/v1/products/{id}` - Get product by ID
- `GET /api/v1/products/code/{code}` - Get product by code
- `GET /api/v1/products/sku/{sku}` - Get product by SKU
- `GET /api/v1/products/barcode/{barcode}` - Get product by barcode
- `GET /api/v1/products` - List all products (paginated)
- `GET /api/v1/products/category/{categoryId}` - List products by category
- `GET /api/v1/products/active` - Get active products
- `GET /api/v1/products/search?query={query}` - Search products
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product (soft)

### Inventory

- `POST /api/v1/inventory` - Create/update inventory
- `POST /api/v1/inventory/adjust` - Adjust stock levels
- `POST /api/v1/inventory/reserve` - Reserve stock
- `POST /api/v1/inventory/release` - Release reserved stock
- `GET /api/v1/inventory/{id}` - Get inventory by ID
- `GET /api/v1/inventory/product/{productId}/branch/{branchId}` - Get inventory for product at branch
- `GET /api/v1/inventory` - List all inventory (paginated)
- `GET /api/v1/inventory/branch/{branchId}` - List inventory by branch
- `GET /api/v1/inventory/product/{productId}` - List inventory by product (all branches)
- `GET /api/v1/inventory/low-stock` - Get low stock items
- `GET /api/v1/inventory/low-stock/branch/{branchId}` - Get low stock items by branch

### Price History

- `GET /api/v1/price-history/product/{productId}` - Get price history for product
- `GET /api/v1/price-history/product/{productId}/effective?date={date}` - Get effective price at date

### Health

- `GET /api/v1/health` - Service health check

## Configuration

Application properties (`application.yml`):

```yaml
spring:
  application:
    name: product-service
  datasource:
    url: jdbc:postgresql://localhost:5432/cursorpos_product
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

server:
  port: 8083
```

## Database Schema

- **categories**: Product category hierarchy
- **products**: Product catalog
- **inventory**: Stock levels per branch
- **price_history**: Price change tracking

All tables include:
- `tenant_id` for multi-tenancy
- `created_at`, `updated_at` timestamps
- `deleted_at` for soft deletes
- `version` for optimistic locking

## Development

### Build

```bash
./gradlew :product-service:build
```

### Run

```bash
./gradlew :product-service:bootRun
```

### Test

```bash
./gradlew :product-service:test
```

## Dependencies

- Spring Boot 3.5.7
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct (DTO mapping)
- Lombok
- Spring Kafka (event publishing)
- Shared Library (common utilities)

## Author

rjnat

## Version

1.0.0
