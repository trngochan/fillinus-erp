-- ============================================================
-- V2: Create departments table
-- FILLINUS ERP — Authentication Module
-- Source: DOC02_AUTH-004_MyProfile (MP-010: Department label)
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    code        VARCHAR(50)     UNIQUE,
    description VARCHAR(255),
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

-- Seed sample departments
INSERT INTO departments (name, code, description)
VALUES
    ('Information Technology', 'IT',  'IT Department'),
    ('Human Resources',        'HR',  'HR Department'),
    ('Finance',                'FIN', 'Finance Department'),
    ('Operations',             'OPS', 'Operations Department')
ON CONFLICT (code) DO NOTHING;
