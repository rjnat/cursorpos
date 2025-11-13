# Admin Service

Admin Service manages tenants, customers, stores, branches, and system settings for the CursorPOS system.

## Features

- **Tenant Management**: Create and manage multi-tenant organizations
- **Customer Management**: Maintain customer records with loyalty points
- **Store Management**: Manage physical store locations
- **Branch Management**: Organize stores into branches/departments
- **Settings Management**: Tenant-specific configuration and preferences

## API Endpoints

### Tenants
- `POST /api/v1/admin/tenants` - Create new tenant
- `GET /api/v1/admin/tenants/{id}` - Get tenant by ID
- `GET /api/v1/admin/tenants/code/{code}` - Get tenant by code
- `GET /api/v1/admin/tenants` - List all tenants (paginated)
- `PUT /api/v1/admin/tenants/{id}` - Update tenant
- `DELETE /api/v1/admin/tenants/{id}` - Soft delete tenant
- `POST /api/v1/admin/tenants/{id}/activate` - Activate tenant
- `POST /api/v1/admin/tenants/{id}/deactivate` - Deactivate tenant

### Customers
- `POST /api/v1/admin/customers` - Create new customer
- `GET /api/v1/admin/customers/{id}` - Get customer by ID
- `GET /api/v1/admin/customers/code/{code}` - Get customer by code
- `GET /api/v1/admin/customers` - List all customers (paginated)
- `PUT /api/v1/admin/customers/{id}` - Update customer
- `DELETE /api/v1/admin/customers/{id}` - Soft delete customer
- `POST /api/v1/admin/customers/{id}/loyalty-points?points={amount}` - Add loyalty points

### Stores
- `POST /api/v1/admin/stores` - Create new store
- `GET /api/v1/admin/stores/{id}` - Get store by ID
- `GET /api/v1/admin/stores/code/{code}` - Get store by code
- `GET /api/v1/admin/stores` - List all stores (paginated)
- `PUT /api/v1/admin/stores/{id}` - Update store
- `DELETE /api/v1/admin/stores/{id}` - Soft delete store

### Branches
- `POST /api/v1/admin/branches` - Create new branch
- `GET /api/v1/admin/branches/{id}` - Get branch by ID
- `GET /api/v1/admin/branches/code/{code}` - Get branch by code
- `GET /api/v1/admin/branches` - List all branches (paginated)
- `GET /api/v1/admin/branches/store/{storeId}` - List branches by store (paginated)
- `GET /api/v1/admin/branches/store/{storeId}/active` - List active branches by store
- `PUT /api/v1/admin/branches/{id}` - Update branch
- `DELETE /api/v1/admin/branches/{id}` - Soft delete branch

### Settings
- `POST /api/v1/admin/settings` - Create or update setting
- `GET /api/v1/admin/settings/key/{settingKey}` - Get setting by key
- `GET /api/v1/admin/settings/category/{category}` - List settings by category
- `GET /api/v1/admin/settings` - List all settings (paginated)
- `DELETE /api/v1/admin/settings/{id}` - Soft delete setting

### Health
- `GET /api/v1/admin/health` - Health check endpoint

## Database Schema

### Tables
- `tenants` - Tenant/organization master data
- `customers` - Customer information
- `stores` - Store locations
- `branches` - Store branches/departments
- `settings` - Configuration settings

## Configuration

Key application properties:
- `server.port`: 8082
- `server.servlet.context-path`: /api/v1/admin
- `spring.datasource.*`: PostgreSQL connection settings
- `jwt.*`: JWT authentication configuration

## Dependencies

- Spring Boot 3.5.0
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway (database migrations)
- Kafka (event messaging)
- MapStruct (DTO mapping)
- Lombok

## Running the Service

```bash
# Build
./gradlew :admin-service:build

# Run
./gradlew :admin-service:bootRun

# Run with custom profile
./gradlew :admin-service:bootRun --args='--spring.profiles.active=dev'
```

## Multi-Tenancy

All entities (except tenants) are tenant-isolated using `tenant_id` column. The JWT token carries tenant context which is automatically enforced through `TenantContext` and repository queries.

## Security

All endpoints require authentication and specific permissions:
- `TENANT_*`: Tenant management permissions
- `CUSTOMER_*`: Customer management permissions
- `STORE_*`: Store management permissions
- `BRANCH_*`: Branch management permissions
- `SETTINGS_*`: Settings management permissions

## Event Publishing

Service publishes events to Kafka topics for:
- Tenant lifecycle events
- Customer updates
- Store/branch changes

## Notes

- All delete operations are soft deletes (sets `deleted_at` timestamp)
- Optimistic locking enabled via `@Version` field
- Automatic audit trail via `created_at`, `created_by`, `updated_at`, `updated_by`
