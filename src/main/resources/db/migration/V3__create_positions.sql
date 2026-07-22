-- ============================================================
-- V3: Create positions table
-- FILLINUS ERP — Authentication Module
-- Source: DOC02_AUTH-004_MyProfile (MP-011: Position label)
-- ============================================================

CREATE TABLE IF NOT EXISTS positions (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    code            VARCHAR(50)     UNIQUE,
    department_id   BIGINT          REFERENCES departments(id) ON DELETE SET NULL,
    description     VARCHAR(255),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

-- Seed sample positions
INSERT INTO positions (name, code, department_id)
VALUES
    ('Software Engineer',  'SE',  (SELECT id FROM departments WHERE code = 'IT')),
    ('HR Specialist',      'HRS', (SELECT id FROM departments WHERE code = 'HR')),
    ('Finance Analyst',    'FA',  (SELECT id FROM departments WHERE code = 'FIN')),
    ('Operations Officer', 'OO',  (SELECT id FROM departments WHERE code = 'OPS'))
ON CONFLICT (code) DO NOTHING;
