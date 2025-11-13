# Identity Service

Authentication and user management service for CursorPOS.

## Port
- **8081**

## Features

### Authentication
- JWT-based authentication
- Login with email/password
- Token refresh mechanism
- Password encryption with BCrypt
- Account lockout after failed login attempts

### User Management
- User CRUD operations
- Email verification support
- Password change tracking
- User activation/deactivation
- Multi-tenant user isolation

### Role-Based Access Control (RBAC)
- Hierarchical role system
- Permission-based authorization
- Role assignment to users
- System roles (non-modifiable)
- Custom roles per tenant

### Security Features
- Account lockout mechanism
- Failed login attempt tracking
- Password complexity validation
- JWT token expiration
- Refresh token rotation

## Database Schema

### Tables
- **users** - User accounts with credentials
- **roles** - Role definitions
- **permissions** - Permission definitions
- **user_roles** - User-to-role mappings
- **role_permissions** - Role-to-permission mappings

### Default System Roles
- `SUPER_ADMIN` - Full system access
- `TENANT_ADMIN` - Full tenant access
- `STORE_MANAGER` - Store operations management
- `CASHIER` - Transaction processing
- `INVENTORY_MANAGER` - Inventory management

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - User logout

### Users
- `GET /api/v1/users` - List users (tenant-scoped)
- `GET /api/v1/users/{id}` - Get user details
- `POST /api/v1/users` - Create user
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user (soft delete)

### Roles
- `GET /api/v1/roles` - List roles
- `GET /api/v1/roles/{id}` - Get role details
- `POST /api/v1/roles` - Create role
- `PUT /api/v1/roles/{id}` - Update role
- `DELETE /api/v1/roles/{id}` - Delete role

### User Roles
- `POST /api/v1/users/{userId}/roles/{roleId}` - Assign role to user
- `DELETE /api/v1/users/{userId}/roles/{roleId}` - Remove role from user

## Environment Variables

- `DB_HOST` - PostgreSQL host
- `DB_PORT` - PostgreSQL port
- `DB_NAME` - Database name
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret
- `REDIS_HOST` - Redis host for caching
- `REDIS_PORT` - Redis port
- `REDIS_PASSWORD` - Redis password
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka servers

## Running

```bash
cd service-pos
./gradlew :identity-service:bootRun
```

Or with Docker:
```bash
docker-compose up identity-service
```

## Events Published

- `user.created` - When a new user is registered
- `user.updated` - When user details are modified
- `user.deleted` - When a user is deleted
- `user.login` - When a user successfully logs in
