-- ============================================================
-- V10: Lead V1.1 — add new lead_status enum values
-- FILLINUS ERP — Sales Module (SAL-001 V1.1)
--
-- Postgres does not allow ALTER TYPE ... ADD VALUE and then USING that
-- value in the same transaction. Flyway runs each migration script in its
-- own transaction, so the new values are added here in V10 and only
-- referenced starting from V11 (a separate migration/transaction).
--
-- Old values (CONVERTED, CLOSED) are kept in the enum — Postgres does not
-- support removing enum values. The application simply stops using them.
-- ============================================================

ALTER TYPE lead_status ADD VALUE IF NOT EXISTS 'QUALIFIED';
ALTER TYPE lead_status ADD VALUE IF NOT EXISTS 'REJECTED';
