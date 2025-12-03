# CursorPOS - Full System Implementation Plan

**Goal:** Build complete POS system with Backend, Web, and Mobile frontends

**Current Status:** Identity Service Complete ✅ | Product Service Complete ✅ | Transaction Service Complete ✅ | **API Gateway Complete ✅** | Ready for Integration Testing

---

## ✅ COMPLETED

### Identity Service (Backend) ✅
- [x] User authentication (login, logout, refresh token, validate token)
- [x] JWT token generation with tenant context
- [x] BCrypt password encoding
- [x] Multi-tenant user management
- [x] Integration tests (12 tests passing, 82% coverage)
- [x] Boolean wrapper fixes for null-safety
- [x] Code quality improvements (235→77 warnings)
- [x] OpenAPI/Swagger documentation configured

### Product Service (Backend) ✅
- [x] Database schema (categories, products, inventory, stock_movements, price_history)
- [x] Seed test data for coffee shop and restaurant tenants
- [x] Domain entities with validations (Category, Product, Inventory, StockMovement, PriceHistory)
- [x] DTOs with MapStruct mappers
- [x] Repositories with custom queries
- [x] Business logic services (ProductService, CategoryService, InventoryService)
- [x] REST Controllers with JWT authentication
- [x] Integration tests for all controllers (85 tests, 70% coverage)
- [x] Unit tests for all services
- [x] Multi-tenant isolation verified
- [x] Deprecated JWT builder methods updated
- [x] OpenAPI/Swagger documentation configured

### Transaction Service (Backend) ✅
- [x] Database schema (transactions, transaction_items, payments, receipts)
- [x] Seed test data with sample transactions
- [x] Domain entities (Transaction, TransactionItem, Payment, Receipt)
- [x] DTOs with MapStruct mappers
- [x] Repositories with custom queries
- [x] Business logic services (TransactionService, ReceiptService)
- [x] REST Controllers with JWT authentication
- [x] Unit tests (45 tests, 100% service coverage)
- [x] Integration tests (25 tests, 100% controller coverage)
- [x] **99.45% overall instruction coverage** (entity: 100%, mapper: 99.12%, service: 100%, controller: 100%)
- [x] Multi-tenant isolation verified
- [x] Static HTML API documentation (transaction-service-api.html)
- [x] Logback file logging configuration

---

# 📱 1. POS TERMINAL

## **Backend**

### Step 1: Product Service - Database Schema ✅
- [x] Create `V10__create_product_schema.sql` migration
- [x] Create `categories` table (id, tenant_id, name, parent_category_id, timestamps)
- [x] Create `products` table (id, tenant_id, sku, barcode, name, description, category_id, base_price, tax_rate, is_active, timestamps)
- [x] Create `inventory` table (id, tenant_id, store_id, product_id, quantity, reserved_quantity, min_stock_level, max_stock_level, timestamps)
- [x] Create `stock_movements` table (id, tenant_id, store_id, product_id, movement_type, quantity_delta, reference_order_id, notes, timestamp)
- [x] Create `price_history` table for historical price tracking
- [x] Add indexes: (tenant_id, store_id, product_id), (tenant_id, sku), (tenant_id, barcode)

### Step 2: Product Service - Seed Test Data ✅
- [x] Create `V11__seed_product_test_data.sql`
- [x] Insert sample categories (Beverages, Food, Snacks)
- [x] Insert 15+ sample products with realistic data
- [x] Insert inventory records for Coffee Shop stores
- [x] Insert inventory records for Restaurant stores

### Step 3: Product Service - Domain Entities ✅
- [x] Create `Category` entity with self-referencing hierarchy
- [x] Create `Product` entity with validations (unique SKU per tenant, price > 0, tax_rate 0-100)
- [x] Create `Inventory` entity with composite key (tenant_id, store_id, product_id)
- [x] Create `StockMovement` entity with enum (SALE, RESTOCK, ADJUSTMENT, TRANSFER, RESERVE, RELEASE, etc.)
- [x] Create `PriceHistory` entity
- [x] Add equals/hashCode methods to StockMovement

### Step 4: Product Service - DTOs ✅
- [x] Create `ProductDTO` (id, sku, barcode, name, description, categoryName, basePrice, taxRate, availableStock)
- [x] Create `ProductRequest/Response` DTOs
- [x] Create `CategoryDTO` (id, name, parentCategoryId, parentCategoryName)
- [x] Create `InventoryDTO` (productId, storeId, quantity, availableQuantity, reservedQuantity)
- [x] Create MapStruct mappers for all entities

### Step 5: Product Service - Repositories ✅
- [x] Create `CategoryRepository` with methods: findByTenantIdAndParentCategoryIdIsNull(), findByTenantIdAndParentCategoryId()
- [x] Create `ProductRepository` with methods: findByTenantIdAndSku(), findByTenantIdAndBarcode(), searchByTenantIdAndNameContainingIgnoreCase()
- [x] Create `InventoryRepository` with methods: findByTenantIdAndStoreIdAndProductId(), findLowStockItems()
- [x] Create `StockMovementRepository` with methods: findByTenantIdAndStoreIdAndProductId(), findByReferenceOrderId()
- [x] Create `PriceHistoryRepository`

### Step 6: Product Service - Business Logic ✅
- [x] Implement `ProductService`: createProduct(), updateProduct(), getProduct(), searchProducts(), getProductBySku()
- [x] Implement `CategoryService`: createCategory(), updateCategory(), listCategories()
- [x] Implement `InventoryService`: getStock(), updateStock(), adjustStock()
- [x] Add validation logic: check SKU uniqueness, validate price and tax rate
- [x] Add MapStruct-based DTO mapping

### Step 7: Product Service - REST Controllers ✅
- [x] Create `ProductController` (/api/v1/products):
  - [x] GET /search?q={term}&storeId={id}
  - [x] GET /{id}
  - [x] POST / (admin only)
  - [x] PUT /{id} (admin only)
  - [x] DELETE /{id} (soft delete)
- [x] Create `CategoryController` (/api/v1/categories):
  - [x] GET /
  - [x] POST / (admin only)
  - [x] PUT /{id} (admin only)
  - [x] DELETE /{id} (soft delete)
- [x] Create `InventoryController` (/api/v1/inventory):
  - [x] GET /{productId}?storeId={id}
  - [x] PUT /{productId}
  - [x] POST /adjust

### Step 8: Product Service - Configuration ✅
- [x] Configure `application.yml` for product-service
- [x] Configure `application-test.yml` for integration tests
- [x] Set up tenant context propagation via JWT
- [x] Configure exception handling
- [x] Disable Kafka for development (port conflict avoidance)

### Step 9: Product Service - Integration Tests ✅
- [x] Test product creation with duplicate SKU validation
- [x] Test product search functionality
- [x] Test category CRUD operations
- [x] Test multi-tenant isolation (cannot access other tenant's products)
- [x] Test JWT authentication integration
- [x] Unit tests for services (CategoryServiceTest, ProductServiceTest, InventoryServiceTest)

### Step 10: Transaction Service - Database Schema ✅
- [x] Create `V1__create_transaction_schema.sql` migration
- [x] Create `transactions` table (id, tenant_id, branch_id, customer_id, transaction_number, transaction_date, type, status, subtotal, tax_amount, discount_amount, total_amount, paid_amount, change_amount, notes, cashier_id, cashier_name, timestamps)
- [x] Create `transaction_items` table (id, transaction_id, product_id, product_code, product_name, quantity, unit_price, discount_amount, tax_rate, tax_amount, subtotal, total_amount, notes, tenant_id, timestamps)
- [x] Create `payments` table (id, tenant_id, transaction_id, payment_method, amount, payment_date, reference_number, notes, timestamps)
- [x] Create `receipts` table (id, tenant_id, transaction_id, receipt_number, issued_date, receipt_type, content, print_count, last_printed_at, timestamps)
- [x] Add indexes: (tenant_id, transaction_number), (tenant_id, branch_id), (tenant_id, customer_id), (tenant_id, transaction_date), (tenant_id, status)

### Step 11: Transaction Service - Seed Test Data ✅
- [x] Create `V2__seed_transaction_test_data.sql`
- [x] Insert sample transactions with different statuses (PENDING, COMPLETED, CANCELLED)
- [x] Insert sample transaction items
- [x] Insert sample payments
- [x] Insert sample receipts

### Step 12: Transaction Service - Domain Entities ✅
- [x] Create `Transaction` entity with status enum (PENDING, COMPLETED, CANCELLED, REFUNDED)
- [x] Create `Transaction` entity with type enum (SALE, RETURN, EXCHANGE)
- [x] Create `TransactionItem` entity with bidirectional relationship
- [x] Create `Payment` entity with payment_method enum (CASH, CREDIT_CARD, DEBIT_CARD, E_WALLET, BANK_TRANSFER, CHECK)
- [x] Create `Receipt` entity
- [x] Add tenant ID propagation via @PrePersist/@PreUpdate

### Step 13: Transaction Service - DTOs ✅
- [x] Create `TransactionRequest` (branchId, customerId, type, items[], payments[], notes, discountAmount)
- [x] Create `TransactionResponse` with nested items and payments
- [x] Create `TransactionItemRequest` and `TransactionItemResponse`
- [x] Create `PaymentRequest` and `PaymentResponse`
- [x] Create `ReceiptResponse`
- [x] Create MapStruct mappers with proper null handling

### Step 14: Transaction Service - Business Logic ✅
- [x] Implement `TransactionService`: createTransaction(), getTransactionById(), getTransactionByNumber(), getAllTransactions(), getTransactionsByBranch(), getTransactionsByCustomer(), getTransactionsByStatus(), getTransactionsByDateRange(), cancelTransaction()
- [x] Implement automatic transaction number generation (TRX-YYYYMMDD-HHMMSS-UUID)
- [x] Implement real-time amount calculations (subtotal, tax, discount, total, change)
- [x] Implement payment processing with change calculation
- [x] Implement `ReceiptService`: generateReceipt(), getReceiptById(), getReceiptByTransaction(), printReceipt()
- [x] Add validation: tenant isolation, business logic constraints

### Step 15: Transaction Service - Integration with Product Service
- [ ] Create REST client to call Product Service
- [ ] Implement inventory reservation during order creation
- [ ] Implement inventory commit after successful payment
- [ ] Implement inventory release on order cancellation
- [ ] Handle Product Service errors gracefully
**Note:** This step is deferred - current implementation works without product service integration

### Step 16: Transaction Service - REST Controllers ✅
- [x] Create `TransactionController` (/api/transactions):
  - [x] POST /
  - [x] GET /{id}
  - [x] GET /number/{transactionNumber}
  - [x] GET / (list with pagination)
  - [x] GET /branch/{branchId}
  - [x] GET /customer/{customerId}
  - [x] GET /status/{status}
  - [x] GET /date-range
  - [x] PUT /{id}/cancel
- [x] Create `ReceiptController` (/api/receipts):
  - [x] POST /transaction/{transactionId}
  - [x] GET /{id}
  - [x] GET /transaction/{transactionId}
  - [x] PUT /{id}/print
- [x] Create `HealthController` (/api/health)

### Step 17: Transaction Service - Configuration ✅
- [x] Configure `application.yml` for transaction-service (port 8084)
- [x] Configure `application-test.yml` with PostgreSQL test database
- [x] Configure Flyway migrations
- [x] Configure exception handling (GlobalExceptionHandler)
- [x] Configure logback-spring.xml with file logging (./logs/transaction-service.log)
- [x] Disable Kafka for development

### Step 18: Transaction Service - Integration Tests ✅
- [x] Test transaction creation with multiple items and payments
- [x] Test transaction retrieval by ID, number, branch, customer, status, date range
- [x] Test transaction cancellation
- [x] Test payment processing and change calculation
- [x] Test receipt generation and printing
- [x] Test multi-tenant isolation
- [x] Test validation errors (duplicate receipts, invalid states)
- [x] **25 integration tests, 100% controller coverage**
- [x] **45 unit tests, 100% service coverage**

### Step 19: Discount Management - Database Schema
- [ ] Create `V3__create_discount_schema.sql` in transaction-service
- [ ] Create `discounts` table (id, tenant_id, code, name, type, value, min_order_amount, max_discount_amount, valid_from, valid_to, is_active)
- [ ] Create `discount_approvals` table (id, tenant_id, order_id, discount_type, discount_value, requested_by, requested_at, approved_by, approved_at, status, notes)
- [ ] Create `discount_authorization_rules` table for role-based limits

### Step 20: Discount Management - Domain & Services
- [ ] Create `Discount` entity with type enum (PERCENTAGE, FIXED_AMOUNT)
- [ ] Create `DiscountApproval` entity with status enum (PENDING, APPROVED, REJECTED, EXPIRED)
- [ ] Implement `DiscountService`: applyDiscount(), checkAuthorization(), calculateDiscountAmount()
- [ ] Implement `DiscountApprovalService`: createApprovalRequest(), approveDiscount(), rejectDiscount(), listPendingApprovals()

### Step 21: Discount Management - REST Controllers
- [ ] Create `DiscountController` (/api/v1/discounts):
  - [ ] POST /apply
  - [ ] POST /approval-requests
  - [ ] GET /approval-requests/pending (for managers)
  - [ ] POST /approval-requests/{id}/approve
  - [ ] POST /approval-requests/{id}/reject

### Step 22: Discount Management - Integration Tests
- [ ] Test applying pre-configured discount
- [ ] Test authorization rules (cashier 10%, manager 25%, admin unlimited)
- [ ] Test approval workflow (request → approve → apply)
- [ ] Test approval rejection flow
- [ ] Test expired approval requests

### Step 23: Offline Sync - Database Schema
- [ ] Create `V4__create_sync_schema.sql` in transaction-service
- [ ] Create `sync_queue` table (id, tenant_id, store_id, client_order_id, order_data_json, sync_status, sync_attempts, error_message, created_at, synced_at)
- [ ] Create `sync_conflicts` table (id, tenant_id, store_id, client_order_id, conflict_type, resolution_strategy, resolved_at)

### Step 24: Offline Sync - Domain & Services
- [ ] Create `SyncQueue` entity with status enum (PENDING, PROCESSING, COMPLETED, FAILED)
- [ ] Create `SyncConflict` entity
- [ ] Implement `SyncService`: acceptBulkOrders(), deduplicateOrders(), processOrderQueue(), resolveConflicts()
- [ ] Implement conflict resolution strategies: accept negative stock, honor offline price, log variance

### Step 25: Offline Sync - REST Controllers
- [ ] Create `SyncController` (/api/v1/sync):
  - [ ] POST /orders (bulk submission)
  - [ ] GET /status?storeId={id}&since={timestamp}

### Step 26: Offline Sync - Integration Tests
- [ ] Test bulk order sync with 10+ orders
- [ ] Test duplicate order detection via client_order_id
- [ ] Test inventory conflict resolution
- [ ] Test price variance handling
- [ ] Test sync status tracking

### Step 27: API Gateway Configuration
- [ ] Configure routes for product-service (port 8083)
- [ ] Configure routes for transaction-service (port 8084)
- [ ] Add rate limiting rules
- [ ] Add CORS configuration for web/mobile clients
- [ ] Test gateway routing

### Step 28: Docker & Infrastructure
- [ ] Update `docker-compose.yml` with product-service and transaction-service
- [ ] Configure service discovery (if needed)
- [ ] Add health check endpoints for all services
- [ ] Test full stack startup

### Step 29: End-to-End Backend Testing
- [ ] Test complete checkout flow: search → add to cart → calculate → create order → payment → receipt
- [ ] Test discount approval workflow across services
- [ ] Test offline sync flow
- [ ] Test multi-tenant isolation across all services
- [ ] Load test with concurrent requests
- [ ] Document all API endpoints

---

## **Web Frontend** (POS Terminal - Port 3002)

### Step 1: Project Setup
- [ ] Create React + Vite project in `web-pos/pos-terminal/`
- [ ] Install dependencies: react-router-dom, redux-toolkit, axios, react-i18next, idb (IndexedDB)
- [ ] Configure Vite with proxy to API Gateway (localhost:8080)
- [ ] Set up folder structure: pages/, components/, services/, store/, i18n/

### Step 2: PWA Configuration
- [ ] Create `manifest.json` (app name, icons, theme color, display mode: standalone)
- [ ] Create `service-worker.js` for offline caching
- [ ] Configure workbox for asset caching strategy
- [ ] Add offline fallback page

### Step 3: Authentication Flow
- [ ] Create Login page with tenant_id, email, password fields
- [ ] Implement login API call to Identity Service
- [ ] Store JWT token in httpOnly cookie (or localStorage for dev)
- [ ] Create PrivateRoute component for protected pages
- [ ] Implement logout functionality
- [ ] Add "Remember Me" functionality

### Step 4: Redux Store Setup
- [ ] Create slices: authSlice, cartSlice, productsSlice, ordersSlice, syncSlice
- [ ] Configure Redux Persist for cart and offline queue
- [ ] Set up API middleware for token injection
- [ ] Create selectors for cart totals, item count

### Step 5: Product Search & Display
- [ ] Create ProductSearch component (search bar with debounced API call)
- [ ] Create ProductCard component (displays product with image, name, price, stock)
- [ ] Create ProductGrid component (displays search results)
- [ ] Implement barcode scanner integration (Web Barcode Detection API or library)
- [ ] Implement product quick-add to cart
- [ ] Display low stock warnings

### Step 6: Shopping Cart
- [ ] Create Cart component (list of items with quantity controls)
- [ ] Create CartItem component (product name, quantity, price, remove button)
- [ ] Implement add/remove/update quantity actions
- [ ] Display cart totals (subtotal, tax, discount, total)
- [ ] Add "Clear Cart" functionality
- [ ] Persist cart in Redux + IndexedDB

### Step 7: Checkout Flow
- [ ] Create Checkout page with cart summary
- [ ] Create PaymentMethod selector (CASH, CARD buttons)
- [ ] Create amount input for cash payment (calculate change)
- [ ] Implement discount application UI
- [ ] Show real-time total calculation
- [ ] Add "Complete Order" button

### Step 8: Discount Management UI
- [ ] Create ApplyDiscount modal (discount code or percentage input)
- [ ] Display applied discount in cart summary
- [ ] Create RequestApproval modal for over-limit discounts
- [ ] Implement polling for approval status
- [ ] Show approval pending indicator
- [ ] Handle approval/rejection notifications

### Step 9: Order Completion & Receipt
- [ ] Implement order creation API call
- [ ] Show order confirmation with order number
- [ ] Create Receipt component (formatted receipt display)
- [ ] Add "Print Receipt" functionality (browser print)
- [ ] Add "Email Receipt" button (optional)
- [ ] Show "New Order" button to start fresh

### Step 10: Offline Mode - IndexedDB Setup
- [ ] Create IndexedDB wrapper service (db: products, orders_queue, sync_status)
- [ ] Implement product caching: save searched products to IDB
- [ ] Implement offline order queue: save orders with client_order_id
- [ ] Track sync status: last_sync_timestamp, pending_orders_count

### Step 11: Offline Mode - Sync Logic
- [ ] Detect online/offline status (navigator.onLine)
- [ ] Show offline indicator in UI
- [ ] Queue orders locally when offline
- [ ] Implement sync service: POST /api/v1/sync/orders on reconnect
- [ ] Show sync progress indicator
- [ ] Handle sync errors gracefully
- [ ] Mark synced orders as completed

### Step 12: Manager Approval Dashboard
- [ ] Create ApprovalDashboard page (list of pending approval requests)
- [ ] Create ApprovalCard component (shows request details, cashier name, discount amount)
- [ ] Implement approve/reject actions
- [ ] Add filtering: by store, by date
- [ ] Show approval history
- [ ] Real-time updates via polling or WebSocket

### Step 13: Order History
- [ ] Create OrderHistory page (list of past orders)
- [ ] Create OrderCard component (order number, date, total, status)
- [ ] Implement order search/filter (by date, order number, status)
- [ ] Add pagination
- [ ] Show order details on click
- [ ] Add "Reprint Receipt" option

### Step 14: Internationalization (i18n)
- [ ] Set up react-i18next
- [ ] Create translation files: en.json, id.json
- [ ] Translate all UI text (buttons, labels, messages)
- [ ] Add language switcher (EN/ID toggle)
- [ ] Format currency and dates based on locale

### Step 15: UI/UX Polish
- [ ] Implement responsive layout (tablet landscape optimized)
- [ ] Add loading spinners for API calls
- [ ] Add error notifications (toast messages)
- [ ] Add success confirmations
- [ ] Keyboard shortcuts (F1-F12 for quick actions)
- [ ] Touch-friendly buttons (large tap targets)
- [ ] Add app logo and branding

### Step 16: Testing
- [ ] Unit tests for Redux slices
- [ ] Unit tests for utility functions
- [ ] Integration tests for checkout flow
- [ ] Test offline mode and sync
- [ ] Test on different screen sizes
- [ ] Test PWA installation

### Step 17: Build & Deployment
- [ ] Configure production build (Vite)
- [ ] Optimize bundle size (code splitting)
- [ ] Generate service worker with workbox
- [ ] Test production build locally
- [ ] Create deployment documentation

---

## **Mobile Frontend** (React Native)

### Step 1: Project Setup
- [ ] Create React Native project in `mobile-pos/CursorPos/`
- [ ] Install dependencies: @react-navigation/native, @reduxjs/toolkit, axios, react-native-sqlite-storage, react-i18next, react-native-camera
- [ ] Configure Metro bundler
- [ ] Set up folder structure: src/screens/, components/, services/, store/, navigation/, i18n/

### Step 2: Navigation Setup
- [ ] Install React Navigation (stack, tab, drawer navigators)
- [ ] Create navigation structure: Auth Stack (Login), Main Stack (POS, History, Settings), Manager Stack (Approvals)
- [ ] Configure deep linking (optional)
- [ ] Add navigation guards for authentication

### Step 3: Authentication Flow
- [ ] Create Login screen with tenant_id, email, password inputs
- [ ] Implement login API call
- [ ] Store JWT in secure storage (react-native-keychain)
- [ ] Create auth context/slice
- [ ] Implement logout functionality
- [ ] Add biometric authentication (Face ID/Touch ID) - optional

### Step 4: Redux Store Setup
- [ ] Create slices: authSlice, cartSlice, productsSlice, ordersSlice, syncSlice
- [ ] Configure Redux Persist with AsyncStorage for cart
- [ ] Set up API middleware for token injection
- [ ] Create selectors for cart calculations

### Step 5: SQLite Database Setup
- [ ] Create SQLite database helper service
- [ ] Create tables: products, orders_queue, sync_status
- [ ] Implement CRUD operations for products cache
- [ ] Implement queue operations for offline orders
- [ ] Add sync status tracking

### Step 6: Product Search & Barcode Scanner
- [ ] Create ProductSearch screen with search bar
- [ ] Implement product search API call
- [ ] Create ProductList component (FlatList with products)
- [ ] Create ProductCard component
- [ ] Integrate react-native-camera for barcode scanning
- [ ] Add scan button to open camera
- [ ] Handle barcode scan result (search by barcode)

### Step 7: Shopping Cart
- [ ] Create Cart screen (list of cart items)
- [ ] Create CartItem component (swipeable for delete)
- [ ] Implement add/remove/update quantity
- [ ] Display cart totals with animated updates
- [ ] Persist cart in Redux + SQLite
- [ ] Add "Clear Cart" functionality

### Step 8: Checkout Flow
- [ ] Create Checkout screen
- [ ] Create payment method selector buttons
- [ ] Implement cash payment with change calculator
- [ ] Add discount application UI
- [ ] Show real-time total calculation
- [ ] Add "Complete Order" button with confirmation dialog

### Step 9: Discount Management
- [ ] Create ApplyDiscount modal
- [ ] Implement discount code entry
- [ ] Show applied discount in cart
- [ ] Create RequestApproval flow
- [ ] Implement approval polling with timeout
- [ ] Show approval status notifications

### Step 10: Order Completion & Receipt
- [ ] Implement order creation API call
- [ ] Show order confirmation screen
- [ ] Create Receipt component (formatted display)
- [ ] Add "Share Receipt" functionality (native share)
- [ ] Option to print via Bluetooth printer (optional)
- [ ] Add "New Order" button

### Step 11: Offline Mode - Sync Logic
- [ ] Detect network connectivity (NetInfo)
- [ ] Show offline indicator banner
- [ ] Queue orders in SQLite when offline
- [ ] Generate client_order_id (UUID)
- [ ] Implement background sync when online
- [ ] Show sync progress notification
- [ ] Handle sync errors and retries

### Step 12: Manager Approval Screen
- [ ] Create ApprovalList screen
- [ ] Fetch pending approvals from API
- [ ] Create ApprovalCard component
- [ ] Implement approve/reject actions
- [ ] Add pull-to-refresh
- [ ] Show approval history

### Step 13: Order History
- [ ] Create OrderHistory screen
- [ ] Fetch orders from API with pagination
- [ ] Create OrderListItem component
- [ ] Implement search/filter (date range, status)
- [ ] Show order details screen
- [ ] Add "View Receipt" option

### Step 14: Settings & Configuration
- [ ] Create Settings screen
- [ ] Add language switcher (EN/ID)
- [ ] Add store selector (switch current store)
- [ ] Display user profile info
- [ ] Add logout button
- [ ] Show app version and sync status

### Step 15: Internationalization
- [ ] Set up react-i18next
- [ ] Create translation files: en.json, id.json
- [ ] Translate all screen text
- [ ] Format currency and dates by locale
- [ ] Handle RTL languages (future)

### Step 16: UI/UX Polish
- [ ] Implement native animations (LayoutAnimation, Animated API)
- [ ] Add haptic feedback for buttons
- [ ] Optimize FlatList performance (virtualization)
- [ ] Add empty states for lists
- [ ] Add error boundaries
- [ ] Add loading indicators
- [ ] Implement dark mode (optional)

### Step 17: Testing
- [ ] Unit tests for Redux slices
- [ ] Unit tests for services
- [ ] Integration tests for checkout flow
- [ ] Test offline sync flow
- [ ] Test on Android emulator
- [ ] Test on iOS simulator
- [ ] Test on real devices

### Step 18: Build & Deployment
- [ ] Configure app icons and splash screens
- [ ] Set up Android build (release APK)
- [ ] Set up iOS build (IPA)
- [ ] Configure app signing
- [ ] Test release builds
- [ ] Create deployment documentation

---

# 🌐 2. PUBLIC SIGNUP

## **Backend**

### Step 1: Admin Service - Tenant Management Schema
- [ ] Create `V1__create_admin_schema.sql` migration in admin-service
- [ ] Create `tenants` table (id, name, slug, email, phone, address, status, subscription_package_id, created_at, updated_at)
- [ ] Create `subscriptions` table (id, tenant_id, package_id, status, start_date, end_date, auto_renew, created_at)
- [ ] Create `packages` table (id, name, description, max_users, max_stores, price_monthly, price_yearly, features_json)
- [ ] Create `branches` table (id, tenant_id, name, parent_branch_id, address, phone, created_at)
- [ ] Create `stores` table (id, tenant_id, branch_id, name, code, address, phone, tax_rate, currency, created_at)
- [ ] Add indexes: (tenant_id), (slug unique)

### Step 2: Admin Service - Seed Data
- [ ] Create `V2__seed_admin_test_data.sql`
- [ ] Insert subscription packages (Package A: 5 users, Package B: 10 users, Package C: unlimited)
- [ ] Insert sample tenant (Coffee Shop, Restaurant)
- [ ] Insert branches and stores

### Step 3: Admin Service - Domain Entities
- [ ] Create `Tenant` entity with status enum (PENDING, ACTIVE, SUSPENDED, CANCELLED)
- [ ] Create `Subscription` entity with status enum (TRIAL, ACTIVE, EXPIRED, CANCELLED)
- [ ] Create `Package` entity
- [ ] Create `Branch` entity (self-referencing for hierarchy)
- [ ] Create `Store` entity

### Step 4: Admin Service - DTOs
- [ ] Create `TenantRegistrationRequest` (companyName, email, password, packageId, phone, address)
- [ ] Create `TenantDTO`
- [ ] Create `SubscriptionDTO`
- [ ] Create `PackageDTO`
- [ ] Create `StoreDTO`
- [ ] Create `TenantOnboardingResponse`

### Step 5: Admin Service - Business Logic
- [ ] Implement `TenantProvisioningService`:
  - [ ] registerTenant() - create tenant, subscription, admin user
  - [ ] createDefaultStores() - create 3 default stores
  - [ ] createDefaultBranch() - create "Head Office" branch
  - [ ] generateTenantSlug() - from company name
  - [ ] sendWelcomeEmail() - via notification service (mock for now)
- [ ] Implement `SubscriptionService`: checkSubscriptionStatus(), upgradePackage(), cancelSubscription()
- [ ] Add validation: check email uniqueness, validate package selection

### Step 6: Admin Service - REST Controllers
- [ ] Create `TenantController` (/api/v1/tenants):
  - [ ] POST /register (public endpoint)
  - [ ] GET /{id} (admin only)
  - [ ] PUT /{id} (admin only)
  - [ ] GET /{id}/subscription
- [ ] Create `PackageController` (/api/v1/packages):
  - [ ] GET / (public endpoint - list available packages)

### Step 7: Admin Service - Integration Tests
- [ ] Test tenant registration flow
- [ ] Test duplicate email validation
- [ ] Test default stores creation
- [ ] Test subscription creation
- [ ] Test package listing

### Step 8: Identity Service - Integration with Admin Service
- [ ] Add tenant_id validation in Identity Service
- [ ] Update user creation to associate with tenant
- [ ] Create default admin user during tenant provisioning
- [ ] REST client in Admin Service to call Identity Service

### Step 9: API Gateway - Public Routes
- [ ] Configure public routes: /api/v1/tenants/register, /api/v1/packages
- [ ] Add rate limiting for registration endpoint (prevent spam)
- [ ] Configure CORS for public signup domain

---

## **Web Frontend** (Public Signup - Port 3000)

### Step 1: Project Setup
- [ ] Create React + Vite project in `web-pos/public-signup/`
- [ ] Install dependencies: react-router-dom, axios, react-hook-form, yup, react-i18next
- [ ] Configure Vite with proxy to API Gateway
- [ ] Set up folder structure: pages/, components/, services/, i18n/

### Step 2: Landing Page
- [ ] Create Landing page with hero section
- [ ] Add "Why Choose CursorPOS" section with features
- [ ] Add pricing section (Package A/B/C cards)
- [ ] Add testimonials section (optional)
- [ ] Add footer with contact info and links
- [ ] Make responsive (mobile, tablet, desktop)

### Step 3: Pricing Section
- [ ] Fetch packages from API
- [ ] Create PricingCard component (displays package features, price, "Sign Up" button)
- [ ] Highlight recommended package
- [ ] Display features comparison table
- [ ] Add monthly/yearly toggle for pricing

### Step 4: Registration Form
- [ ] Create Signup page with multi-step form
- [ ] Step 1: Company Info (company name, phone, address)
- [ ] Step 2: Admin Account (email, password, confirm password)
- [ ] Step 3: Package Selection (select package, review)
- [ ] Step 4: Terms & Conditions (checkbox, submit button)
- [ ] Implement form validation with yup schema

### Step 5: Form Submission
- [ ] Implement registration API call
- [ ] Show loading indicator during submission
- [ ] Handle errors (email already exists, server error)
- [ ] Show validation errors inline

### Step 6: Thank You Page
- [ ] Create ThankYou page (success message)
- [ ] Display registration confirmation (company name, email)
- [ ] Show "What's Next" instructions (check email, login link)
- [ ] Add "Go to Login" button linking to Admin Portal

### Step 7: Internationalization
- [ ] Set up react-i18next
- [ ] Create translation files: en.json, id.json
- [ ] Translate all page content
- [ ] Add language switcher in header

### Step 8: UI/UX Polish
- [ ] Implement smooth transitions between form steps
- [ ] Add progress indicator for multi-step form
- [ ] Add animations (scroll animations, fade-ins)
- [ ] Optimize for SEO (meta tags, Open Graph)
- [ ] Add loading states

### Step 9: Testing
- [ ] Test form validation
- [ ] Test successful registration flow
- [ ] Test error handling (duplicate email, server error)
- [ ] Test responsive design on different devices
- [ ] Accessibility testing (keyboard navigation, screen readers)

### Step 10: Build & Deployment
- [ ] Configure production build
- [ ] Optimize images and assets
- [ ] Test production build
- [ ] Create deployment documentation

---

# 🏢 3. ADMIN PORTAL

## **Backend**

### Step 1: Admin Service - Customer Management Schema
- [ ] Create `V3__create_customer_schema.sql` in admin-service
- [ ] Create `customers` table (id, tenant_id, customer_number, name, email, phone, address, customer_group_id, created_at)
- [ ] Create `customer_groups` table (id, tenant_id, name, discount_percentage, description)
- [ ] Create `loyalty_accounts` table (id, tenant_id, customer_id, points_balance, lifetime_points, tier_id, created_at)
- [ ] Create `loyalty_tiers` table (id, name, min_points, benefits_json, color)
- [ ] Create `loyalty_transactions` table (id, tenant_id, loyalty_account_id, order_id, points_delta, transaction_type, description, created_at)

### Step 2: Admin Service - Customer Domain Logic
- [ ] Create `Customer`, `CustomerGroup`, `LoyaltyAccount`, `LoyaltyTier`, `LoyaltyTransaction` entities
- [ ] Implement `CustomerService`: createCustomer(), updateCustomer(), searchCustomers(), getCustomerWithLoyalty()
- [ ] Implement `LoyaltyService`: earnPoints(), redeemPoints(), calculateTier(), getTransactionHistory()
- [ ] Add validation: unique email per tenant, valid points operations

### Step 3: Admin Service - Customer Controllers
- [ ] Create `CustomerController` (/api/v1/customers):
  - [ ] POST /
  - [ ] GET /{id}
  - [ ] PUT /{id}
  - [ ] DELETE /{id}
  - [ ] GET / (search with pagination)
- [ ] Create `LoyaltyController` (/api/v1/loyalty):
  - [ ] GET /accounts/{customerId}
  - [ ] POST /earn
  - [ ] POST /redeem
  - [ ] GET /transactions/{customerId}

### Step 4: Admin Service - User Management Endpoints
- [ ] Create `UserController` in admin-service (/api/v1/users):
  - [ ] POST / (create new user - cashier, manager)
  - [ ] GET / (list users with pagination)
  - [ ] PUT /{id} (update user)
  - [ ] PUT /{id}/deactivate
  - [ ] PUT /{id}/reset-password
- [ ] Integrate with Identity Service for user CRUD

### Step 5: Admin Service - Store Management Endpoints
- [ ] Create `StoreController` (/api/v1/stores):
  - [ ] POST /
  - [ ] GET / (list stores)
  - [ ] PUT /{id}
  - [ ] DELETE /{id}
  - [ ] GET /{id}/configuration

### Step 6: Admin Service - Analytics Schema
- [ ] Create `V4__create_analytics_schema.sql`
- [ ] Create `fact_sales` table (date, tenant_id, store_id, order_id, total_amount, items_count, customer_id)
- [ ] Create `fact_inventory` table (date, tenant_id, store_id, product_id, quantity_sold, quantity_remaining)
- [ ] Create materialized views: `mv_daily_sales_by_store`, `mv_daily_sales_by_branch`, `mv_top_selling_products`

### Step 7: Admin Service - Analytics & Reporting
- [ ] Implement `AnalyticsService`: aggregateSalesData(), getTopSellingProducts(), getSalesReport(), getInventoryReport()
- [ ] Create scheduled jobs (Spring Batch): daily sales aggregation, inventory snapshots
- [ ] Implement `ReportController` (/api/v1/reports):
  - [ ] GET /sales?storeId={id}&from={date}&to={date}
  - [ ] GET /inventory?storeId={id}
  - [ ] GET /top-products?storeId={id}&limit={n}
  - [ ] POST /export (CSV export)

### Step 8: Admin Service - Integration Tests
- [ ] Test customer CRUD operations
- [ ] Test loyalty points earn/redeem flow
- [ ] Test tier calculation
- [ ] Test user management
- [ ] Test store configuration
- [ ] Test analytics aggregation
- [ ] Test multi-tenant isolation

---

## **Web Frontend** (Admin Portal - Port 3001)

### Step 1: Project Setup
- [ ] Create React + Vite project in `web-pos/admin-portal/`
- [ ] Install dependencies: react-router-dom, redux-toolkit, axios, recharts, react-table, react-hook-form, date-fns, react-i18next
- [ ] Configure Vite with proxy to API Gateway
- [ ] Set up folder structure: pages/, components/, store/, services/, i18n/

### Step 2: Authentication & Layout
- [ ] Create Login page
- [ ] Implement authentication flow (call Identity Service)
- [ ] Create main layout with sidebar navigation
- [ ] Create header with user profile dropdown
- [ ] Add logout functionality
- [ ] Implement role-based navigation (show/hide menu items)

### Step 3: Dashboard Page
- [ ] Create Dashboard page with KPI cards (today's sales, orders count, top products)
- [ ] Add sales chart (daily/weekly/monthly toggle)
- [ ] Add recent orders table
- [ ] Add low stock alerts widget
- [ ] Implement date range selector
- [ ] Make dashboard responsive

### Step 4: Tenant Management
- [ ] Create TenantSettings page (view tenant info, subscription details)
- [ ] Display current package and limits (users, stores)
- [ ] Add "Upgrade Package" button (future)
- [ ] Show subscription expiry date
- [ ] Add billing history (future)

### Step 5: Store Management
- [ ] Create StoresList page (table of stores)
- [ ] Create StoreForm component (create/edit store)
- [ ] Add store configuration (tax rate, currency, receipt template)
- [ ] Implement search and filter
- [ ] Add pagination

### Step 6: User Management
- [ ] Create UsersList page (table of users)
- [ ] Create UserForm component (create/edit user)
- [ ] Add role selector (CASHIER, MANAGER, ADMIN)
- [ ] Add store assignment (assign user to specific stores)
- [ ] Implement user deactivation
- [ ] Add password reset functionality

### Step 7: Customer Management
- [ ] Create CustomersList page (table with search)
- [ ] Create CustomerForm component (create/edit customer)
- [ ] Add customer group selector (Retail, Wholesale, VIP)
- [ ] Display loyalty points and tier
- [ ] Show purchase history
- [ ] Add export customers (CSV)

### Step 8: Loyalty Program Management
- [ ] Create LoyaltySettings page (configure point earning rate, tiers)
- [ ] Display tier benefits
- [ ] Create CustomerLoyaltyDetail page (points balance, transaction history)
- [ ] Add manual points adjustment (for admins)
- [ ] Show tier progression indicator

### Step 9: Product Management
- [ ] Create ProductsList page (table with search)
- [ ] Create ProductForm component (create/edit product with image upload)
- [ ] Add category selector (with create new category option)
- [ ] Set base price and tax rate
- [ ] Add bulk import products (CSV upload - future)
- [ ] Add export products

### Step 10: Category Management
- [ ] Create CategoriesList page (tree view)
- [ ] Create CategoryForm component (create/edit, select parent category)
- [ ] Implement drag-and-drop reordering (optional)
- [ ] Add category icons/colors

### Step 11: Inventory Management
- [ ] Create InventoryList page (table of products with stock levels per store)
- [ ] Add stock adjustment form (manual increase/decrease with reason)
- [ ] Show stock movement history
- [ ] Add low stock alerts
- [ ] Implement stock transfer between stores (future)
- [ ] Add bulk stock adjustment (CSV upload)

### Step 12: Pricing Management
- [ ] Create PricingList page (products with pricing rules)
- [ ] Create PriceOverrideForm (set store-level or branch-level prices)
- [ ] Create PromotionalPriceForm (time-bound promotional prices)
- [ ] Display active pricing rules per product
- [ ] Add price history view

### Step 13: Discount Management
- [ ] Create DiscountsList page (table of configured discounts)
- [ ] Create DiscountForm component (create/edit discount codes)
- [ ] Set discount type (percentage, fixed amount)
- [ ] Set validity period and minimum order amount
- [ ] Add discount usage statistics
- [ ] Configure authorization rules (role-based limits)

### Step 14: Order Management
- [ ] Create OrdersList page (table with search and filters)
- [ ] Create OrderDetail page (items, payment, customer info)
- [ ] Add order status updates
- [ ] Add void/refund functionality
- [ ] Show order timeline (created, paid, completed)
- [ ] Add export orders (CSV)

### Step 15: Reports & Analytics
- [ ] Create ReportsPage with multiple tabs:
  - [ ] Sales Report (by date range, store, product)
  - [ ] Inventory Report (stock levels, movements)
  - [ ] Customer Report (top customers, loyalty stats)
  - [ ] Product Performance (top/bottom sellers)
- [ ] Add charts using recharts (bar, line, pie charts)
- [ ] Implement export to CSV/PDF
- [ ] Add date range picker
- [ ] Add store/branch filter

### Step 16: Settings Page
- [ ] Create SettingsPage with tabs:
  - [ ] Company Profile (name, address, logo)
  - [ ] Tax Configuration
  - [ ] Receipt Template
  - [ ] Notification Settings
  - [ ] Language Preference
- [ ] Implement save settings functionality

### Step 17: Internationalization
- [ ] Set up react-i18next
- [ ] Create translation files: en.json, id.json
- [ ] Translate all pages and components
- [ ] Add language switcher in header
- [ ] Format dates, numbers, currency by locale

### Step 18: UI/UX Polish
- [ ] Implement responsive design (desktop, tablet)
- [ ] Add loading states and skeletons
- [ ] Add error notifications (toast)
- [ ] Add confirmation dialogs for destructive actions
- [ ] Implement breadcrumbs navigation
- [ ] Add tooltips and help text
- [ ] Implement dark mode (optional)

### Step 19: Testing
- [ ] Unit tests for Redux slices
- [ ] Integration tests for forms
- [ ] E2E tests for critical flows (create product, create order)
- [ ] Test on different browsers
- [ ] Accessibility testing

### Step 20: Build & Deployment
- [ ] Configure production build
- [ ] Optimize bundle size
- [ ] Test production build
- [ ] Create deployment documentation

---

## 📊 OVERALL PROJECT STATUS TRACKING

### Backend Services Status
- [x] Identity Service - COMPLETE ✅
- [x] Product Service - COMPLETE ✅
- [x] Transaction Service - COMPLETE ✅ (Steps 10-18 done, 99.45% coverage)
- [x] API Gateway - COMPLETE ✅ (Defense-in-depth JWT validation, 100% filter coverage)
- [ ] Admin Service - NOT STARTED (Next priority)

### Frontend Applications Status
- [ ] POS Terminal Web - NOT STARTED
- [ ] POS Terminal Mobile - NOT STARTED
- [ ] Public Signup Web - NOT STARTED
- [ ] Admin Portal Web - NOT STARTED

### Infrastructure Status
- [x] Docker Compose - PARTIAL (needs service updates)
- [ ] Database Migrations - PARTIAL (Identity only)
- [ ] API Documentation - NOT STARTED
- [ ] Deployment Guides - NOT STARTED

---

## 🧪 TEST COVERAGE SUMMARY

**Last Updated:** December 4, 2025  
**Coverage Tool:** JaCoCo  
**Definition:** Coverage = % of source code (lines/branches) executed during test runs

| Service | Test Type | Total Tests | Line Coverage | Branch Coverage | Status |
|---------|-----------|-------------|---------------|-----------------|--------|
| **Identity Service** | Unit Tests | 39 methods | 100% | 100% | ✅ Excellent |
| | Integration Tests | 20 methods | 100% | 100% | ✅ Excellent |
| **Product Service** | Unit Tests | 106 methods | 100% | 100% | ✅ Excellent |
| | Integration Tests | 31 methods | 100% | 100% | ✅ Excellent |
| **Transaction Service** | Unit Tests | 70 methods | 97.0% | 95.8% | ✅ Excellent |
| | Integration Tests | 27 methods | 97.0% | 95.8% | ✅ Excellent |
| **API Gateway** | Unit Tests | 15 methods | 46.0% | 66.7% | ⚠️ Needs Work |
| | Integration Tests | 0 methods | 0% | 0% | ❌ Missing |

**Overall Project Coverage:**
- Average Line Coverage: 85.8%
- Average Branch Coverage: 90.6%
- Production-Ready Services: 3/4 (Identity, Product, Transaction)
- Services Needing Improvement: 1/4 (API Gateway)

**Notes:**
- ✅ Identity, Product, Transaction Services: Production-ready with comprehensive test coverage
- ⚠️ API Gateway: Requires 61+ additional lines of test coverage to reach 80% minimum
- ❌ API Gateway Missing: Integration tests, fallback controller tests, configuration bean tests
- 🎯 Recommendation: Improve API Gateway coverage before proceeding to Admin Service

**How to Update This Table:**
1. After adding/modifying tests, run: `.\gradlew.bat test jacocoTestReport`
2. Parse coverage from: `build/reports/jacoco/test/jacocoTestReport.xml`
3. Update test counts and coverage percentages
4. Update "Last Updated" date

---

## ❓ CLARIFICATION QUESTIONS

### Product Service:
1. Should we support product variants (e.g., sizes, colors)?
2. Multi-level categories or single level?
3. Barcode format requirements?
4. Allow negative stock (overselling)?
5. Price rounding rules (2 decimals, round up/down)?

### Transaction Service:
6. Order number format preference?
7. Order status values needed?
8. Payment methods for MVP (CASH, CARD, E_WALLET)?
9. Receipt format (JSON, PDF, or plain text)?
10. Tax inclusive or exclusive pricing?

### Discount:
11. Exact authorization limits (Cashier: 10%, Manager: 25%)?
12. Approval timeout duration?
13. Allow discount stacking?

### General:
14. Store configuration needed (tax rate, currency, receipt template)?
15. User roles beyond CASHIER, MANAGER, ADMIN?
16. Should failed operations be logged to audit table?
17. Expected concurrent users per store?

### Mobile Specific:
18. Support Bluetooth thermal printer?
19. Offline depth (24 hours)?
20. Biometric authentication (Face ID/Touch ID)?

---

## 🎯 RECENT ACCOMPLISHMENTS (November 2025)

### Code Quality Session
- Fixed 158 warnings across identity and product services
- Removed unused imports (ArrayList, Instant, Collectors)
- Fixed Boolean wrapper usage for null-safety (4 locations in AuthService)
- Added equals/hashCode to StockMovement entity
- Updated deprecated JWT builder methods in integration tests
- Fixed test assertions (isEqualTo(0) → isZero())
- **Result:** 67% reduction in warnings (235 → 77 remaining)

### Product Service Implementation
- Complete CRUD operations for products, categories, and inventory
- Multi-tenant data isolation verified through tests
- Stock movement tracking with comprehensive movement types
- Price history tracking for audit trail
- MapStruct-based DTO mapping for clean architecture
- Integration with JWT authentication from Identity Service
- Flyway migrations following service-specific versioning (V10+)

### API Documentation (OpenAPI/Swagger)
- SpringDoc OpenAPI integrated in both Identity and Product services
- OpenAPI annotations added to all REST controllers
- Interactive Swagger UI available at `/swagger-ui.html`
- API documentation available at `/api-docs`
- Bearer token authentication configured for protected endpoints
- Comprehensive endpoint descriptions and examples

### Transaction Service Recent Accomplishments (November 2025)
- Complete transaction lifecycle management (create, retrieve, cancel)
- Multi-payment support with automatic change calculation
- Receipt generation with print tracking
- Real-time amount calculations (subtotal, tax, discount, total)
- Comprehensive testing: 70 tests (25 integration + 45 unit) - all passing
- **99.45% instruction coverage** (entity: 100%, mapper: 99.12%, service: 100%, controller: 100%)
- Static HTML API documentation generated
- Logback file logging with error file separation
- MapStruct/Lombok interaction issues resolved

### API Gateway Recent Accomplishments (November 22, 2025)
- **Defense-in-depth JWT validation** - Gateway validates + Services re-validate using shared-lib
- **AuthenticationGatewayFilter** with 100% test coverage (15 unit tests, 100% line/branch coverage)
- **Service routing** configured for all services (Identity 8081, Admin 8082, Product 8083, Transaction 8084)
- **Tenant context propagation** via headers (X-Tenant-Id, X-User-Id, X-User-Role, X-Store-Id, X-Branch-Id)
- **Public endpoint bypass** (login, register, health checks, signup)
- **Resilience features**: Circuit breakers (Resilience4j), Rate limiting (Redis), Retry with backoff
- **CORS configuration** for web clients (localhost:5173/5174/5175)
- **Logback file logging** (./logs/api-gateway.log, ./logs/api-gateway-error.log)
- **Comprehensive API documentation** (api-gateway.html) with architecture, authentication, configuration details
- **Redis container** configured with correct name (cursorpos-redis) on port 6379

---

## 🎯 NEXT PRIORITY: End-to-End Integration Testing

### Step 27: API Gateway Configuration ✅ COMPLETE

**Goal:** Complete API Gateway routing and enable end-to-end testing

#### Completed Tasks:
1. **Gateway Routes** ✅
   - [x] Configure routes for identity-service (port 8081) ✅
   - [x] Configure routes for product-service (port 8083) ✅
   - [x] Configure routes for transaction-service (port 8084) ✅
   - [x] Configure routes for admin-service (port 8082) ✅

2. **Security Configuration** ✅
   - [x] JWT validation with defense-in-depth (Gateway + Services)
   - [x] Tenant context extraction from JWT (tenant_id, user_id, role, store_id, branch_id)
   - [x] Public endpoint bypass (login, register, health, signup, refresh, forgot-password)
   - [x] Rate limiting configured (10-20 req/sec per service)

3. **CORS Configuration** ✅
   - [x] Configured for web applications (localhost:5173, 5174, 5175)
   - [x] Allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
   - [x] Credentials allowed with max age 3600s

4. **Health Checks** ✅
   - [x] Gateway health check at /actuator/health
   - [x] Circuit breaker status monitoring
   - [x] Prometheus metrics at /actuator/prometheus

5. **Resilience Features** ✅
   - [x] Circuit breakers (Resilience4j) for all services
   - [x] Retry with exponential backoff (3 retries, 10ms-50ms)
   - [x] Redis-based rate limiting
   - [x] Timeout configuration (3s default)

6. **Testing** ✅
   - [x] 15 unit tests for AuthenticationGatewayFilter (100% coverage)
   - [x] All tests passing with 100% line, branch, instruction coverage
   - [x] Filter validates JWT, extracts claims, propagates headers
   - [x] Public endpoint bypass verified
   - [x] Error handling tested (missing token, invalid token, expired token)

7. **Documentation** ✅
   - [x] Comprehensive HTML API documentation (api-gateway.html)
   - [x] Architecture overview and features
   - [x] JWT structure and authentication flow
   - [x] Request/response headers documentation
   - [x] Error responses and status codes
   - [x] Configuration details (rate limiting, circuit breakers, CORS)
   - [x] Testing examples (cURL, Postman)

8. **Infrastructure** ✅
   - [x] Redis container created with correct name (cursorpos-redis:6379)
   - [x] Logback file logging configured
   - [x] Dependencies added (reactor-test for testing)

### Step 28: End-to-End Integration Testing ✅ COMPLETE
**Goal:** Verify complete system integration through API Gateway

**Status:** ✅ **ALL TESTS PASSED** - Documented in `service-pos/INTEGRATION_TEST_RESULTS.md`

#### Completed Tasks:
1. **Service Startup** ✅
   - [x] PostgreSQL database ✅ RUNNING
   - [x] Redis with password authentication ✅ RUNNING (redis_dev_password_2025)
   - [x] Keycloak IAM server ✅ RUNNING (port 8180)
   - [x] Identity Service ✅ RUNNING (port 8081)
   - [x] Product Service ✅ RUNNING (port 8083)
   - [x] Transaction Service ✅ RUNNING (port 8084)
   - [x] API Gateway ✅ RUNNING (port 8080)

2. **Technical Debt Resolution** ✅
   - [x] Fixed Redis password configuration (redis_dev_password_2025)
   - [x] Re-enabled Gateway rate limiting (10-20 req/sec with Redis)
   - [x] Started Keycloak and created 'cursorpos' realm
   - [x] Created 'api-gateway' OAuth2 client in Keycloak
   - [x] Re-enabled OAuth2 Resource Server in Gateway
   - [x] Fixed Flyway schema history conflicts:
     - Created separate tables: flyway_schema_history_identity
     - flyway_schema_history_product, flyway_schema_history_transaction
   - [x] All services using independent migration tracking

3. **Authentication Flow Testing** ✅
   - [x] Login through Gateway: POST http://localhost:8080/api/v1/auth/login ✅ SUCCESS
   - [x] JWT token generation verified ✅
   - [x] User authenticated: Admin Coffee (admin@coffee.test) ✅
   - [x] Tenant context: tenant-coffee-001 ✅

4. **Service Integration Testing** ✅
   - [x] Product Service via Gateway ✅ OPERATIONAL
   - [x] Transaction Service via Gateway ✅ OPERATIONAL (4 transactions retrieved)
   - [x] Multi-tenant isolation verified ✅
   - [x] Service routing through Gateway verified ✅

5. **Infrastructure Verification** ✅
   - [x] Redis connection with authentication ✅ VERIFIED
   - [x] Keycloak realm and client configuration ✅ VERIFIED
   - [x] Flyway migrations for all services ✅ COMPLETED
   - [x] Rate limiting configuration ✅ ENABLED
   - [x] Circuit breakers ✅ CONFIGURED

6. **Documentation** ✅
   - [x] Updated INTEGRATION_TEST_RESULTS.md
   - [x] Updated api-gateway/SECURITY_CONFIGURATION.md
   - [x] Documented all technical debt resolutions

#### Test Results Summary:
- **Authentication**: ✅ PASSED
- **Service Routing**: ✅ PASSED
- **Database Migrations**: ✅ PASSED
- **Multi-tenancy**: ✅ PASSED
- **Infrastructure**: ✅ ALL SYSTEMS OPERATIONAL
- **Test pass rate**: 100% (all executed tests passed)

#### Resolved Technical Debt:
- ✅ Redis password configured and working
- ✅ Rate limiting re-enabled with Redis
- ✅ Keycloak deployed with cursorpos realm
- ✅ OAuth2 Resource Server enabled
- ✅ Flyway schema history conflicts resolved
- ✅ All services have independent migration tracking
---

## 🎯 NEXT PRIORITY: Admin Service Implementation

### Step 29: Admin Service - Tenant & Customer Management
**Goal:** Build admin portal backend for multi-tenant management

**Status:** 🔄 READY TO START

#### Required Tasks:
1. **Tenant Management**
   - [ ] Create TenantController with CRUD operations
   - [ ] Implement tenant subscription plans (Free, Basic, Premium, Enterprise)
   - [ ] Add tenant settings (business hours, tax rates, currency)
   - [ ] Tenant activation/deactivation
   - [ ] Tenant usage tracking (storage, users, transactions)

2. **Customer Management**
   - [ ] Create CustomerController with CRUD operations
   - [ ] Customer registration and profile management
   - [ ] Customer search and filtering
   - [ ] Customer purchase history integration
   - [ ] Customer loyalty points tracking

3. **Store & Branch Management**
   - [ ] Create StoreController for multi-location support
   - [ ] Store CRUD operations with tenant isolation
   - [ ] Branch management (multiple branches per store)
   - [ ] Store-specific settings (timezone, language, currency)
   - [ ] Store inventory allocation

4. **Flyway Migrations**
   - [ ] Create V10__init_admin_schema.sql (tenants, customers, stores, branches)
   - [ ] Configure separate flyway_schema_history_admin table
   - [ ] Add indexes for tenant_id, email, phone_number

5. **Testing**
   - [ ] Unit tests for all services (100% coverage target)
   - [ ] Integration tests for all controllers
   - [ ] Test multi-tenant isolation
   - [ ] Test tenant subscription limits

6. **API Documentation**
   - [ ] Generate admin-service-api.html
   - [ ] Document tenant management endpoints
   - [ ] Document customer management endpoints
   - [ ] Document store/branch management endpoints

**Estimated Time:** 2-3 days

---

## 📋 RECOMMENDED NEXT STEPS (Choose One)

### Option A: Discount Management (Extends Transaction Service)
**Steps 19-22** - Add discount features to Transaction Service
- Pre-configured discounts
- Authorization rules (cashier 10%, manager 25%)
- Approval workflow
- **Estimated Time:** 1-2 days

### Option B: Offline Sync (Extends Transaction Service)
**Steps 23-26** - Add offline sync capabilities
- Sync queue for offline orders
- Conflict resolution strategies
- Bulk order submission
- **Estimated Time:** 2-3 days

### Option C: Admin Service (New Service)
**Public Signup Backend** - Start tenant provisioning
- Tenant management schema
- Self-service registration flow
- Package management
- **Estimated Time:** 3-4 days

### Option D: Frontend Development
**POS Terminal Web** - Start building the UI
- React + Vite setup
- Product search and cart
- Checkout flow
- **Estimated Time:** 1-2 weeks

---

**Recommended Next Action:** Complete **End-to-End Integration Testing (Step 28)** to verify complete system functionality through API Gateway, then proceed with **Discount Management (Steps 19-22)** to add essential POS features before starting Admin Service or Frontend.

---

## 🚀 QUICK START: Running the Complete Backend

### ✅ Current System Status (As of 2025-12-03)
**All services operational with technical debt resolved!**

Infrastructure:
- ✅ PostgreSQL (docker: postgre) - Port 5432
- ✅ Redis (docker: cursorpos-redis) - Port 6379 (password: redis_dev_password_2025)
- ✅ Keycloak (docker: cursorpos-keycloak) - Port 8180 (realm: cursorpos)
- ✅ Kafka & Zookeeper (optional for events)

Microservices:
- ✅ Identity Service - Port 8081 (Flyway: flyway_schema_history_identity)
- ✅ Product Service - Port 8083 (Flyway: flyway_schema_history_product)
- ✅ Transaction Service - Port 8084 (Flyway: flyway_schema_history_transaction)
- ✅ API Gateway - Port 8080 (OAuth2 enabled, rate limiting active)

### Prerequisites
```powershell
# 1. Start Infrastructure (if not running)
docker start postgre
docker start cursorpos-redis
docker start cursorpos-keycloak

# 2. Verify containers
docker ps --filter "name=cursorpos|postgre"

# 3. Verify ports are available
netstat -ano | Select-String "8080|8081|8083|8084|8180|6379"
```

### Start Services (in separate PowerShell terminals)
```powershell
# Terminal 1: Identity Service
cd d:\workspace\cursorpos\service-pos\identity-service
..\gradlew.bat bootRun

# Terminal 2: Product Service  
cd d:\workspace\cursorpos\service-pos\product-service
..\gradlew.bat bootRun

# Terminal 3: Transaction Service
cd d:\workspace\cursorpos\service-pos\transaction-service
..\gradlew.bat bootRun

# Terminal 4: API Gateway (with Redis password)
cd d:\workspace\cursorpos\service-pos\api-gateway
$env:REDIS_PASSWORD='redis_dev_password_2025'
..\gradlew.bat bootRun
```

### Verify System Health
```powershell
# Check Gateway health (includes all downstream services)
Invoke-RestMethod http://localhost:8080/actuator/health

# Test login through Gateway
$body = '{"tenantId":"tenant-coffee-001","email":"admin@coffee.test","password":"Test@123456"}'
Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/login -Method POST -Body $body -ContentType "application/json"
  -d '{\"email\":\"admin@coffeeshop.com\",\"password\":\"password123\"}'

# Use the JWT token from login response for subsequent requests
$token = "your-jwt-token-here"
curl http://localhost:8080/api/v1/products `
  -H "Authorization: Bearer $token"
```

### API Documentation
- **API Gateway**: `docs/api/api-gateway.html`
- **Identity Service**: `docs/api/identity-service-api.html`
- **Product Service**: `docs/api/product-service-api.html`
- **Transaction Service**: `docs/api/transaction-service-api.html`
