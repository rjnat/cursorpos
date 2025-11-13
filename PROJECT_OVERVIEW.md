# CursorPOS Project - Complete Overview

## 📋 Project Summary

**CursorPOS** is a comprehensive, multi-tenant Point of Sale (POS) system designed for small and medium businesses. It features a microservices architecture with offline-first capabilities, advanced pricing/discount management, customer loyalty programs, and event-driven communication.

---

## 🎯 Project Goal

The conversation aimed to design and generate a **production-ready, enterprise-grade POS system** with the following objectives:

### Business Goals
- Support **hierarchical multi-tenancy**: Company → Branches → Stores
- Enable **subscription-based packages** (Package A/B/C with user limits)
- Provide **automated tenant onboarding** (self-service signup)
- Support **offline-first operations** for mobile/web POS (24-hour depth)
- Implement **pluggable payment providers** (Stripe, Midtrans, Xendit)
- Deliver **batch analytics** and reporting at store/branch/company levels

### Technical Goals
- Build a **microservices architecture** with event-driven communication (Kafka)
- Use **Spring Boot 3.5.7** (Java 21) for backend services
- Implement **React 18+** for web applications and **React Native** for mobile
- Leverage **PostgreSQL 15** with row-level multi-tenancy
- Integrate **Keycloak** for authentication (OAuth2/OIDC with JWT)
- Support **internationalization** (English and Indonesian)

---

## 🏗️ Architecture Evolution

### Initial Design (13 Services)
The conversation started with a full microservices architecture:
1. API Gateway
2. Identity Service
3. Tenant Provisioning Service
4. Subscription & Billing Service
5. Directory Service (Company/Branch/Store hierarchy)
6. Catalog Service (Products & Pricing)
7. Inventory Service
8. Sales/Checkout Service
9. Customer Service (with Loyalty)
10. Reporting & Analytics Service
11. Notification Service
12. Configuration Service
13. Audit & Observability Service

### Optimized Design (5 Services) - Final Architecture
After analyzing operational complexity for SMB, we consolidated into **5 core services**:

1. **API Gateway (Port 8080)** - Routing, authentication, rate limiting
2. **Identity Service (Port 8081)** - User management, Keycloak integration, JWT
3. **Admin Service (Port 8082)** - Tenant management, customer management, platform services, analytics
4. **Product Service (Port 8083)** - Catalog, pricing engine, inventory management
5. **Transaction Service (Port 8084)** - Sales, checkout, discounts, cash management

**Rationale for Consolidation:**
- Reduced operational overhead (fewer containers, simpler deployments)
- Lower inter-service communication latency
- Better suited for SMB scale (up to 10k tenants, 100k transactions/day)
- Maintains clean bounded contexts for future splitting if needed

---

## 📊 Key Features

### 1. Multi-Tenancy Model
- **Approach:** Shared schema with row-level tenancy (tenant_id on all tables)
- **Security:** Application-layer filtering + PostgreSQL RLS policies
- **Isolation:** Tenant context propagated via JWT tokens
- **Scalability:** Can migrate to schema-per-tenant or database-per-tenant later

### 2. Advanced Pricing System
- **Base Price:** Product-level default pricing
- **Price Lists:** Versioned, time-bound pricing (e.g., "Summer 2025 Price List")
- **Price Overrides:** Branch-level and store-level custom pricing
- **Promotional Prices:** Time-limited offers (e.g., Black Friday pricing)
- **Volume Pricing:** Quantity-based discounts (e.g., buy 10+, get 20% off)
- **Customer Group Pricing:** Wholesale, Retail, VIP pricing tiers
- **Tax Configuration:** Product-level tax rates

**Price Resolution Hierarchy:**
```
Promotional Price → Volume Price → Store Override → Branch Override → 
Customer Group Price → Active Price List → Base Price
```

### 3. Discount Management
- **Discount Types:**
  - Percentage discount (item/order level)
  - Fixed amount discount
  - (Future: Buy X Get Y, Bundle discounts, Coupon codes)
  
- **Approval Workflow:**
  - Cashiers can apply pre-configured discounts
  - Manual discounts require real-time manager approval (blocks checkout)
  - Authorization levels: Cashier (10%) → Manager (25%) → Admin (unlimited)

### 4. Customer Loyalty Program
- **Customer Groups:** Wholesale, Retail, VIP
- **Loyalty Points:** Earn on every purchase (configurable rate)
- **Redemption:** Points can be redeemed for discounts
- **Tiered Benefits:** Bronze → Silver → Gold (auto-promotion based on points/spend)
- **Purchase History:** Track customer buying patterns

### 5. Offline-First Capabilities
- **Web POS:** Progressive Web App (PWA) with Service Worker and IndexedDB
- **Mobile POS:** React Native with SQLite local storage
- **Offline Depth:** 24-hour transaction queue
- **Sync Strategy:**
  - Triggered by network availability, app resume, or manual sync
  - Per-store FIFO ordering
  - Idempotent ingestion (duplicate detection via client_id)
- **Conflict Resolution:**
  - Inventory insufficient: Accept order, create negative stock, alert supervisor
  - Duplicate transactions: Deduplicate by client_order_id
  - Price changes: Honor offline price (audit variance)

### 6. Event-Driven Architecture
- **Message Broker:** Apache Kafka with Zookeeper, Schema Registry, Kafka Connect
- **Topics:**
  ```
  pos.tenant.created
  pos.subscription.changed
  pos.product.created
  pos.inventory.adjusted
  pos.order.created
  pos.payment.completed
  pos.notification.requested
  ```
- **Partitioning:** By tenant_id (primary) and store_id (secondary for ordering)
- **Delivery Semantics:** At-least-once with idempotency via event_id
- **Outbox Pattern:** Transactional consistency between DB writes and Kafka publishes

---

## 🛠️ Technology Stack

### Backend
| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.5.7 |
| Language | Java (OpenJDK) | 21 |
| Build Tool | Gradle | 9.1.0 |
| Database | PostgreSQL | 15 |
| Cache | Redis | 7.2 |
| Message Broker | Apache Kafka | 3.6+ |
| Authentication | Keycloak | 23.0.7 |
| API Documentation | Springdoc OpenAPI | 2.1.0 |
| Migrations | Flyway | 10.4.1 |

### Frontend
| Component | Technology | Version |
|-----------|-----------|---------|
| Web Framework | React | 18.3.1 |
| Build Tool | Vite | 5.x |
| Mobile Framework | React Native CLI | 0.76.0 |
| State Management | Redux Toolkit | Latest |
| Internationalization | react-i18next | Latest |
| Offline Storage (Web) | IndexedDB | Native |
| Offline Storage (Mobile) | SQLite | react-native-sqlite-storage |

### Infrastructure
| Service | Technology | Port |
|---------|-----------|------|
| API Gateway | Spring Cloud Gateway | 8080 |
| PostgreSQL | PostgreSQL 15 Alpine | 5432 |
| Kafka | Confluent Kafka | 9092 |
| Zookeeper | Confluent Zookeeper | 2181 |
| Schema Registry | Confluent Schema Registry | 8081 |
| Kafka Connect | Confluent Kafka Connect | 8083 |
| Redis | Redis 7.2 Alpine | 6379 |
| Keycloak | Keycloak | 8180 |
| MailHog (Dev) | MailHog | 8025 (UI), 1025 (SMTP) |

---

## 📁 Project Structure

```
cursorpos/                                 # Repository root
├── .env                                   # Environment variables
├── .gitignore                            # Root gitignore
├── docker-compose.yml                    # Infrastructure setup
├── README.md                             # Project documentation
│
├── service-pos/                          # Backend (Java/Spring Boot)
│   ├── build.gradle                      # Root Gradle build
│   ├── settings.gradle                   # Module definitions
│   ├── gradle.properties                 # Gradle configuration
│   ├── db-init/                          # Database initialization
│   │   └── 01-init.sql
│   │
│   ├── shared-lib/                       # Common utilities
│   │   └── src/main/java/com/cursorpos/shared/
│   │       ├── entity/                   # Base entities (BaseEntity)
│   │       ├── security/                 # JWT, TenantContext, filters
│   │       ├── dto/                      # Common DTOs (ApiResponse, PagedResponse)
│   │       ├── exception/                # Exception handling
│   │       ├── event/                    # Kafka events
│   │       └── config/                   # Kafka, Redis configs
│   │
│   ├── api-gateway/                      # Port 8080
│   │   └── src/main/java/com/cursorpos/gateway/
│   │       ├── config/                   # Gateway routes, filters
│   │       └── filter/                   # Auth filter, tenant extraction
│   │
│   ├── identity-service/                 # Port 8081
│   │   └── src/main/java/com/cursorpos/identity/
│   │       ├── api/                      # REST controllers
│   │       ├── domain/                   # User, Role, Permission entities
│   │       ├── repository/               # JPA repositories
│   │       ├── service/                  # Business logic
│   │       ├── config/                   # Keycloak integration
│   │       └── resources/db/migration/   # Flyway migrations
│   │
│   ├── admin-service/                    # Port 8082
│   │   └── src/main/java/com/cursorpos/admin/
│   │       ├── tenant/                   # Tenant management
│   │       ├── customer/                 # Customer & loyalty
│   │       ├── platform/                 # Config, notifications
│   │       ├── analytics/                # Reporting, batch jobs
│   │       └── resources/db/migration/
│   │
│   ├── product-service/                  # Port 8083
│   │   └── src/main/java/com/cursorpos/product/
│   │       ├── catalog/                  # Products, categories
│   │       ├── pricing/                  # Pricing engine
│   │       ├── inventory/                # Stock management
│   │       └── resources/db/migration/
│   │
│   └── transaction-service/              # Port 8084
│       └── src/main/java/com/cursorpos/transaction/
│           ├── sales/                    # Orders, checkout
│           ├── discount/                 # Discount engine
│           ├── payment/                  # Payment adapters (mock)
│           ├── sync/                     # Offline sync ingestion
│           └── resources/db/migration/
│
├── web-pos/                              # Frontend (React + Vite)
│   ├── public-signup/                    # Port 3000
│   │   ├── package.json
│   │   ├── vite.config.js
│   │   └── src/
│   │       ├── pages/                    # Landing, Signup, ThankYou
│   │       ├── components/               # Forms, buttons
│   │       ├── services/                 # API clients
│   │       └── i18n/                     # EN, ID translations
│   │
│   ├── admin-portal/                     # Port 3001
│   │   └── src/
│   │       ├── pages/
│   │       │   ├── dashboard/            # Overview
│   │       │   ├── tenants/              # Tenant config
│   │       │   ├── customers/            # Customer management
│   │       │   ├── catalog/              # Product management
│   │       │   ├── pricing/              # Price lists, overrides
│   │       │   ├── discounts/            # Discount management
│   │       │   ├── inventory/            # Stock management
│   │       │   └── reports/              # Analytics
│   │       ├── store/                    # Redux Toolkit
│   │       └── i18n/
│   │
│   └── pos-terminal/                     # Port 3002 (PWA)
│       ├── public/
│       │   ├── manifest.json             # PWA manifest
│       │   └── service-worker.js         # Offline caching
│       └── src/
│           ├── pages/                    # Checkout, Login
│           ├── components/               # Cart, Payment, Discount
│           ├── services/
│           │   ├── api.js
│           │   ├── offline.js            # IndexedDB wrapper
│           │   └── sync.js               # Sync logic
│           └── store/                    # Redux Persist
│
└── mobile-pos/                           # Mobile (React Native)
    └── CursorPos/
        ├── package.json
        ├── metro.config.js
        └── src/
            ├── screens/                  # Login, Checkout, Sync
            ├── components/               # ProductSearch, Cart, Payment
            ├── services/
            │   ├── api.ts
            │   ├── sqlite.ts             # SQLite wrapper
            │   ├── sync.ts               # Sync logic
            │   └── queue.ts              # Offline queue
            ├── store/                    # Redux Toolkit
            ├── navigation/               # React Navigation
            └── i18n/                     # EN, ID
```

---

## 🔄 Database Schema Design

### Tenancy Model: Hybrid Approach
- **Single Shared Schema (`public`)** with row-level tenancy
- All tables include `tenant_id` column
- Service-owned migrations (organized by service, applied to single schema)
- No cross-service foreign keys (API validation instead)

### Key Tables

#### Identity Service
- `users` (tenant_id, email, password_hash, role)
- `roles` (tenant_id, name)
- `permissions` (tenant_id, role_id, resource, action)

#### Admin Service - Tenant Domain
- `tenants` (id, name, status, subscription_package_id)
- `subscriptions` (tenant_id, package_id, status, expires_at)
- `packages` (name, max_users, price_monthly)
- `branches` (tenant_id, name, parent_branch_id)
- `stores` (tenant_id, branch_id, name, address)

#### Admin Service - Customer Domain
- `customers` (tenant_id, name, email, customer_group_id)
- `customer_groups` (tenant_id, name, discount_percentage) # Wholesale/Retail/VIP
- `loyalty_accounts` (tenant_id, customer_id, points, tier)
- `loyalty_tiers` (name, min_points, benefits) # Bronze/Silver/Gold
- `loyalty_transactions` (customer_id, points_delta, reason)

#### Product Service - Catalog
- `products` (tenant_id, sku, name, base_price, category_id, tax_rate)
- `categories` (tenant_id, name, parent_category_id)
- `price_lists` (tenant_id, name, version, effective_from, effective_to)
- `price_list_items` (price_list_id, product_id, price)
- `price_overrides` (tenant_id, product_id, branch_id, store_id, override_price)
- `promotional_prices` (tenant_id, product_id, promo_price, valid_from, valid_to)
- `volume_prices` (tenant_id, product_id, min_quantity, unit_price)

#### Product Service - Inventory
- `inventory` (tenant_id, store_id, product_id, quantity, reserved_quantity)
- `stock_movements` (tenant_id, store_id, product_id, type, qty_delta, timestamp)
- `transfer_requests` (tenant_id, from_store_id, to_store_id, product_id, status)
- `sync_conflicts` (tenant_id, store_id, client_id, conflict_type, resolution)

#### Transaction Service
- `orders` (tenant_id, store_id, order_number, total, status, timestamp)
- `order_items` (order_id, product_id, quantity, unit_price, discount)
- `payments` (tenant_id, order_id, method, amount, provider_txn_id, status)
- `receipts` (tenant_id, order_id, receipt_number, pdf_url)
- `discounts` (tenant_id, code, name, type, value, valid_from, valid_to)
- `discount_redemptions` (discount_id, order_id, customer_id, discount_amount)
- `discount_approvals` (order_id, discount_amount, requested_by, approved_by, status)
- `cash_drawers` (tenant_id, store_id, register_id, opened_at, closed_at, variance)

#### Admin Service - Analytics
- `fact_sales` (date, tenant_id, store_id, order_id, total, items_count)
- `fact_inventory` (date, tenant_id, store_id, product_id, quantity)
- `mv_daily_sales_by_store` (materialized view)
- `mv_daily_sales_by_branch` (materialized view)
- `audit_logs` (tenant_id, user_id, action, resource, timestamp, changes)

---

## 🚀 Workflow: Tenant Onboarding

### Step 1: Self-Service Signup (Public Website)
1. Business owner visits `http://localhost:3000` (public-signup)
2. Fills registration form:
   - Company name
   - Admin email
   - Password
   - Subscription package selection (A/B/C)
3. Submits → `POST /api/v1/tenants` (Provisioning Service)

### Step 2: Automated Provisioning
Admin Service creates:
- `tenants` record (status: PENDING)
- `subscriptions` record
- `branches` record (name: "Head Office")
- 3 `stores` records (names: "Store 1", "Store 2", "Store 3")
- 4 `users` records:
  - 1 Admin user
  - 3 Store Users (default passwords)
- Emits Kafka event: `pos.tenant.provisioned`

### Step 3: Welcome Email
- Notification Service consumes event
- Sends email to admin with login credentials
- Includes link to Admin Portal

### Step 4: Admin Configuration
1. Admin logs in at `http://localhost:3001` (admin-portal)
2. Completes profile
3. Configures:
   - Company details
   - Branch/store names
   - User roles/permissions
   - Tax rates, currency
4. Updates tenant status to `ACTIVE`
5. Emits event: `pos.tenant.activated`

---

## 🔐 Authentication Flow

### JWT Token Structure
```json
{
  "sub": "user-uuid",
  "tenant_id": "tenant-123",
  "branch_id": "branch-456",
  "store_id": "store-789",
  "role": "STORE_USER",
  "permissions": ["READ_PRODUCTS", "CREATE_ORDERS"],
  "exp": 1700000000
}
```

### Token Lifecycle
1. User logs in → `POST /api/v1/auth/login`
2. Identity Service validates credentials (via Keycloak)
3. Issues JWT with tenant context
4. Web: Stores in `httpOnly` cookie
5. Mobile: Stores in secure storage (Keychain/Keystore)
6. All API requests include JWT in `Authorization: Bearer <token>` header
7. API Gateway validates token, extracts tenant context
8. Downstream services access tenant via `TenantContext.getTenantId()`

---

## 💳 POS Checkout Flow

### Step-by-Step Process
1. **Cashier scans product** → `GET /api/v1/products/by-sku/{sku}?store_id={id}`
   - Product Service returns: `{product, price: $10, stock: 50}`
   
2. **Cashier adds to cart** (local state in POS Terminal)

3. **Cashier applies discount** → `POST /api/v1/checkout/calculate`
   ```json
   {
     "items": [{"product_id": "p1", "qty": 2, "price": 10}],
     "discount": {"type": "PERCENTAGE", "value": 10}
   }
   ```
   - Transaction Service calculates: `{subtotal: 20, discount: 2, tax: 1.62, total: 19.62}`

4. **Cashier completes payment** → `POST /api/v1/orders`
   ```json
   {
     "store_id": "store-123",
     "items": [...],
     "payment_method": "CASH",
     "amount": 19.62
   }
   ```

5. **Transaction Service processes:**
   - Creates `orders` record
   - Calls Product Service: `POST /inventory/decrement` (stock: 50 → 49)
   - Generates `receipts` record
   - Emits Kafka event: `pos.order.created`
   - Returns: `{order_id, receipt_url}`

6. **POS Terminal displays/prints receipt**

### Offline Scenario
- If offline, order is queued in SQLite/IndexedDB with `client_order_id`
- When online, `POST /api/v1/orders/sync` sends batch
- Transaction Service deduplicates, processes orders, resolves conflicts

---

## 📊 Analytics & Reporting

### Batch Aggregation Pipeline
1. **Event Ingestion:**
   - Admin Service Kafka consumers read `pos.order.created`, `pos.inventory.adjusted`
   - Write to `fact_sales`, `fact_inventory` tables

2. **Scheduled Jobs (Spring Batch):**
   - **Hourly:** Near-real-time rollups (15-min delay)
   - **Daily (2 AM):** Aggregate store → branch → company levels
     ```sql
     INSERT INTO mv_daily_sales_by_store
     SELECT date, store_id, SUM(total), COUNT(*)
     FROM fact_sales
     WHERE date = CURRENT_DATE - INTERVAL '1 day'
     GROUP BY date, store_id;
     ```

3. **Materialized Views:**
   - `mv_daily_sales_by_store`
   - `mv_daily_sales_by_branch`
   - `mv_daily_sales_by_tenant`

4. **API Endpoints:**
   - `GET /api/v1/reports/sales?store_id={id}&from={date}&to={date}`
   - `GET /api/v1/reports/inventory?store_id={id}`
   - `POST /api/v1/reports/export` (CSV/Parquet)

---

## 🧪 Testing Strategy

### Backend Testing
- **Unit Tests:** JUnit 5, Mockito (service layer, domain logic)
- **Integration Tests:** Testcontainers (PostgreSQL, Kafka)
- **Negative Tests:** 
  - Invalid tenant access (cross-tenant data breach attempts)
  - Duplicate sync requests (idempotency validation)
  - Insufficient inventory (conflict resolution)
  - Expired JWT tokens
  - Payment provider failures

### Frontend Testing
- **Web:** Jest + React Testing Library
- **Mobile:** Jest + React Native Testing Library
- **E2E:** 
  - Full checkout flow
  - Offline → sync → online validation
  - Discount approval workflow

---

## 🚦 Current Status

### ✅ Completed
- Root configuration files (`.env`, `docker-compose.yml`, `.gitignore`)
- Backend Gradle configuration
- Database initialization script
- Project folder structure

### 🚧 In Progress (Next Batch)
- Shared Library (complete implementation)
- API Gateway (complete implementation)
- Identity Service (complete implementation with Keycloak)

### 📋 Remaining
- Admin Service
- Product Service
- Transaction Service
- Web applications (3 React apps)
- Mobile application (React Native)

---

## 🎓 Key Design Decisions & Rationale

### 1. Why 5 Services Instead of 13?
**Decision:** Consolidate from 13 to 5 microservices

**Rationale:**
- **Operational simplicity:** Fewer containers to deploy, monitor, debug
- **Cost-effective:** Lower infrastructure overhead (memory, CPU, networking)
- **SMB-appropriate:** Right-sized for <10k tenants, <100k transactions/day
- **Faster development:** Fewer inter-service calls, simpler integration tests
- **Still maintainable:** Clear bounded contexts allow future splitting

**Trade-off:** Admin Service is larger (multiple domains), but low traffic justifies consolidation

### 2. Why Shared Schema vs Schema-per-Service?
**Decision:** Single shared schema (`public`) with row-level tenancy

**Rationale:**
- **Analytics-friendly:** Simple cross-domain queries critical for POS reporting
- **Simpler setup:** Single connection pool, no schema juggling
- **Tenant-scoped anyway:** All tables have `tenant_id`, already isolated at row level
- **Future-proof:** Organized migrations + no cross-service FKs = easy to split later

**Mitigation:** 
- PostgreSQL RLS policies for defense-in-depth
- Separate Flyway history per service
- No cross-service foreign keys

### 3. Why Keycloak for Identity?
**Decision:** Use Keycloak instead of custom OAuth2 server

**Rationale:**
- **Battle-tested:** Production-grade OAuth2/OIDC implementation
- **Feature-rich:** MFA, social login, user federation out-of-the-box
- **Admin UI:** User management without building custom interface
- **Compliance:** GDPR, HIPAA-ready

**Trade-off:** Additional infrastructure component, but worth it for security

### 4. Why PWA for Web POS?
**Decision:** Implement POS Terminal as Progressive Web App

**Rationale:**
- **Offline capability:** Service Workers + IndexedDB for 24-hour offline depth
- **No installation:** Works in browser, no app store approval
- **Cross-platform:** Same code for desktop/tablet browsers
- **Auto-updates:** No manual updates like native apps

### 5. Why Kafka Instead of REST-only?
**Decision:** Event-driven architecture with Kafka

**Rationale:**
- **Decoupling:** Services don't need to know about consumers
- **Scalability:** Async processing, handles traffic spikes
- **Audit trail:** Events are immutable log of all actions
- **Analytics:** Easy to add new consumers without changing producers

**Trade-off:** More complex infrastructure, but essential for POS scale

---

## 🔮 Future Enhancements (Post-MVP)

### Phase 2 Features
- Real-time inventory sync (instead of batch)
- Multi-currency support
- Gift cards / vouchers
- Advanced reporting (predictive analytics, ML-based recommendations)
- CRM features (customer segmentation, targeted campaigns)
- Supplier management & purchase orders
- Employee scheduling & time tracking

### Phase 3 Features
- Multi-warehouse management
- B2B portal (wholesale ordering)
- E-commerce integration (sync with online store)
- Third-party integrations (accounting software, delivery services)
- Advanced security (anomaly detection, fraud prevention)

---

## 📞 Support & Documentation

- **Repository:** https://github.com/rjnat/cursorpos
- **Author:** rjnat
- **Generated:** 2025-11-13
- **License:** MIT (to be confirmed)

---

## 🙏 Acknowledgments

This project architecture was designed through an iterative conversation with GitHub Copilot, covering:
- Microservices design patterns
- Multi-tenant data modeling
- Event-driven architecture
- Offline-first mobile/web strategies
- POS domain-specific requirements

The design balances enterprise-grade architecture with practical SMB constraints.

---

**End of Overview Document**
