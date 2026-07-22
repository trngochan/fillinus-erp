-- ============================================================
-- V1: Create roles table
-- FILLINUS ERP — Authentication Module
-- ============================================================
-- NOTE: This script is managed by Flyway.
--       Run order: V1 → V2 → V3 → V4 → V5
--       To deploy on any server: just set env vars and start the app.
--       Flyway will automatically execute all pending migrations.
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

-- Seed default roles
INSERT INTO roles (name, description)
VALUES
    ('ADMIN',   'System Administrator — full access'),
    ('MANAGER', 'Manager — department-level access'),
    ('EMPLOYEE','Standard Employee — limited access')
ON CONFLICT (name) DO NOTHING;
