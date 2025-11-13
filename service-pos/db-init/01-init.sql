-- ==============================================================================
-- CursorPOS - Database Initialization Script
-- ==============================================================================
-- Generated: 2025-11-13 06:57:25 UTC
-- Author: rjnat
-- Description: Initial database setup for PostgreSQL
-- ==============================================================================

-- Connect to database
\connect cursorpos_db;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'UTC';

-- Grant privileges to cursorpos_user
GRANT ALL PRIVILEGES ON DATABASE cursorpos_db TO cursorpos_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO cursorpos_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cursorpos_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cursorpos_user;

-- Success message
\echo 'Database cursorpos_db initialized successfully!';
