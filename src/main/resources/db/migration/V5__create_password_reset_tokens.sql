-- ============================================================
-- V5: Create password_reset_tokens table
-- FILLINUS ERP — Authentication Module
-- Sources:
--   DOC02_AUTH-002_ForgotPassword  → send reset token to email
--   DOC02_AUTH-003_ResetPassword   → validate token, reset password
-- ============================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255)    NOT NULL UNIQUE,
    expires_at  TIMESTAMP       NOT NULL,
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Index for token lookup (used on every reset-password request)
CREATE INDEX IF NOT EXISTS idx_prt_token   ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_prt_user_id ON password_reset_tokens(user_id);

-- ============================================================
-- NOTE FOR DEPLOYMENT TO OTHER SERVERS:
-- All 5 migration scripts (V1–V5) are stored in:
--   src/main/resources/db/migration/
--
-- Flyway runs them automatically in order on app startup.
-- To migrate a new server:
--   1. Set your DATABASE_URL env var
--   2. Start the application
--   3. Flyway will detect which scripts haven't run yet
--      and execute only the pending ones.
--
-- To add a new table/column in the future:
--   Create V6__<description>.sql, V7__<description>.sql, etc.
-- ============================================================
