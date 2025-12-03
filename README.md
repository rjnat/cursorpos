# CursorPOS - Multi-Tenant Point of Sale System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

A modern, cloud-ready Point of Sale system built with microservices architecture, supporting multi-tenancy and designed for scalability.

## 🚀 Quick Start for Developers

```powershell
# 1. Clone the repository
git clone https://github.com/rjnat/cursorpos.git
cd cursorpos

# 2. Open in VS Code
code cursorpos.code-workspace

# 3. Copy environment template
cp service-pos/.env.example service-pos/.env

# 4. Start infrastructure
docker network create cursorpos-network
docker start postgre cursorpos-redis

# 5. Build and start services
cd service-pos
.\gradlew.bat clean build
.\start-services.ps1
```

**👉 For detailed setup instructions, see [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md)**

**👉 For quick commands, see [QUICK_REFERENCE.md](QUICK_REFERENCE.md)**

## 📋 Project Status

### ✅ Completed Backend Services (4/8)
- **Identity Service** (Port 8081) - Authentication, users, roles ✅
- **Product Service** (Port 8083) - Products, inventory, categories ✅
- **Transaction Service** (Port 8084) - Sales, payments, receipts ✅
- **API Gateway** (Port 8080) - Routing, security, rate limiting ✅
- **Admin Service** (Port 8082) - Tenant management 🚧 *In Progress*

### 🧪 Test Coverage
| Service | Line Coverage | Branch Coverage | Tests | Status |
|---------|---------------|-----------------|-------|--------|
| Identity Service | 100% | 100% | 59 | ✅ Excellent |
| Product Service | 100% | 100% | 137 | ✅ Excellent |
| Transaction Service | 97% | 95.8% | 97 | ✅ Excellent |
| API Gateway | 46% | 66.7% | 15 | ⚠️ Needs Work |

**Overall: 85.8% line coverage, 90.6% branch coverage**

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                      │
│  • JWT Authentication  • Rate Limiting  • Circuit Breaker   │
└──────┬──────────────────┬──────────────────┬────────────────┘
       │                  │                  │
   ┌───▼───┐         ┌────▼────┐       ┌────▼────┐
   │Identity│         │ Product │       │Transaction│
   │Service │         │ Service │       │  Service  │
   │ :8081  │         │ :8083   │       │   :8084   │
   └───┬────┘         └────┬────┘       └─────┬─────┘
       │                   │                   │
       └───────────────────┴───────────────────┘
                          │
              ┌───────────▼──────────┐
              │   PostgreSQL + Redis  │
              └──────────────────────┘
```

### Technology Stack
- **Backend**: Spring Boot 3.5.7, Java 21
- **Database**: PostgreSQL 17, Flyway migrations
- **Cache**: Redis 7
- **Auth**: JWT + Keycloak (OAuth2)
- **Messaging**: Apache Kafka (optional)
- **Build**: Gradle 8.5
- **Testing**: JUnit 5, Mockito, TestContainers

## 📂 Project Structure

```
cursorpos/
├── service-pos/              # Backend microservices
│   ├── identity-service/     # Authentication & authorization
│   ├── product-service/      # Product catalog & inventory
│   ├── transaction-service/  # Sales & payments
│   ├── api-gateway/          # API Gateway
│   ├── shared-lib/           # Shared utilities
│   ├── build.gradle          # Root build config
│   ├── settings.gradle       # Multi-project settings
│   ├── .env.example          # Environment template
│   └── start-services.ps1    # Service startup script
├── docs/api/                 # API documentation (HTML)
├── web-pos/                  # Web frontend (React) - Coming soon
├── mobile-pos/               # Mobile app (React Native) - Coming soon
├── DEVELOPER_SETUP.md        # 👈 Start here!
├── QUICK_REFERENCE.md        # Common commands
└── IMPLEMENTATION_PLAN.md    # Roadmap & progress
```

## 🔑 Key Features

### Implemented ✅
- **Multi-tenancy**: Row-level isolation with tenant_id
- **Authentication**: JWT-based auth with refresh tokens
- **Authorization**: Role-based access control (ADMIN, MANAGER, CASHIER)
- **Product Management**: Full CRUD with categories and inventory
- **Transaction Processing**: Sales, payments, receipts
- **API Gateway**: Centralized routing with security
- **Rate Limiting**: Redis-based rate limiting
- **Circuit Breakers**: Resilience4j for fault tolerance
- **Database Migrations**: Flyway with per-service schema history
- **Comprehensive Testing**: 95%+ coverage with unit & integration tests

### Coming Soon 🚧
- **Admin Service**: Tenant & customer management
- **Discount System**: Authorization workflows
- **Offline Sync**: PWA with IndexedDB
- **Loyalty Program**: Points & rewards
- **Web Portal**: React-based admin panel
- **Mobile App**: React Native POS terminal

## 📚 Documentation

- **[DEVELOPER_SETUP.md](DEVELOPER_SETUP.md)** - Complete setup guide for new developers
- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Common commands and workflows
- **[IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)** - Detailed roadmap and progress
- **API Documentation**: `docs/api/*.html` - Interactive API references

## 🧪 Testing

```powershell
# Run all tests
.\gradlew.bat test

# Generate coverage report
.\gradlew.bat test jacocoTestReport

# View coverage report
start service-pos/identity-service/build/reports/jacoco/test/html/index.html
```

## 🔐 Security

- ✅ No hardcoded passwords (environment variables)
- ✅ JWT with RSA-256 signing
- ✅ OAuth2 integration (Keycloak)
- ✅ Rate limiting per tenant
- ✅ SQL injection prevention (JPA)
- ✅ CORS configuration
- ✅ Audit logging

## 🤝 Contributing

1. **Fork & Clone**: Fork the repository and clone to your machine
2. **Setup**: Follow [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md)
3. **Branch**: Create feature branch (`git checkout -b feature/amazing-feature`)
4. **Code**: Write code with tests (95%+ coverage target)
5. **Test**: Run all tests (`.\gradlew.bat test`)
6. **Commit**: Commit changes (`git commit -m 'feat: Add amazing feature'`)
7. **Push**: Push to branch (`git push origin feature/amazing-feature`)
8. **PR**: Open Pull Request

### Commit Convention
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `test:` - Test additions/changes
- `refactor:` - Code refactoring
- `chore:` - Build/dependency updates

## 📊 Test Data

### Default Tenant & Users
**Tenant**: `tenant-coffee-001` (Coffee Shop)

**Admin User**:
- Email: `admin@coffee.test`
- Password: `Test@123456`

**Manager User**:
- Email: `manager@coffee.test`
- Password: `Test@123456`

**Cashier User**:
- Email: `cashier@coffee.test`
- Password: `Test@123456`

### Sample Products
- Espresso: $2.50
- Cappuccino: $3.50
- Latte: $4.00
- Croissant: $3.00

## 🐛 Troubleshooting

**Services won't start?**
```powershell
# Check ports
netstat -ano | Select-String ":8080|:8081|:8083|:8084"

# Check Docker containers
docker ps

# View logs
docker logs postgre
```

**Tests failing?**
```powershell
# Clean build
.\gradlew.bat clean build

# Check database is running
docker exec postgre pg_isready -U posuser
```

**Need help?**
- Check [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) for detailed troubleshooting
- Review test files for usage examples
- Open an issue on GitHub

## 📞 Support

- **Documentation**: See `docs/` folder
- **Issues**: GitHub Issues
- **Questions**: Check existing issues or create new one

## 📄 License

This project is proprietary software. All rights reserved.

---

**Ready to start?** 👉 Open [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) for complete setup instructions!
