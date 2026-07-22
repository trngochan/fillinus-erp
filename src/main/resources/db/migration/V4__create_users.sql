-- ============================================================
-- V4: Create users table
-- FILLINUS ERP — Authentication Module
-- Sources:
--   DOC02_AUTH-001_Login       → username, password, remember_me
--   DOC02_AUTH-004_MyProfile   → full_name, email, phone, avatar_url,
--                                 date_of_birth, gender, role_id,
--                                 department_id, position_id
--   DOC02_AUTH-005_ChangePassword → password (bcrypt hashed)
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL       PRIMARY KEY,

    -- AUTH-001 Login fields
    username        VARCHAR(100)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,   -- BCrypt hashed; max length per AUTH-005
    remember_me     BOOLEAN         NOT NULL DEFAULT FALSE,

    -- AUTH-004 My Profile fields
    full_name       VARCHAR(150)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    phone           VARCHAR(15),
    avatar_url      VARCHAR(255),
    date_of_birth   DATE,
    gender          VARCHAR(10)     CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),

    -- Relationships (read-only in profile screen — FK references)
    role_id         BIGINT          REFERENCES roles(id) ON DELETE SET NULL,
    department_id   BIGINT          REFERENCES departments(id) ON DELETE SET NULL,
    position_id     BIGINT          REFERENCES positions(id) ON DELETE SET NULL,

    -- Account status
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_attempts INT             NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP,

    -- Audit
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    last_login_at   TIMESTAMP
);

-- Index for login lookup performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);

-- Seed default admin user
-- Password: Admin@123456  (BCrypt hashed — change in production!)
INSERT INTO users (username, password, full_name, email, role_id, is_active)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCKKKKfKkiaqcBAAAAAAAAA',
    'System Administrator',
    'admin@fillinus.com',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    TRUE
)
ON CONFLICT (username) DO NOTHING;
