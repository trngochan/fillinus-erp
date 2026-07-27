-- ============================================================
-- V7: Create leads table
-- FILLINUS ERP — Sales Module (SAL-001)
-- Business Rules: BR-001 to BR-007
-- ============================================================

CREATE TYPE lead_status AS ENUM ('NEW', 'IN_PROGRESS', 'CONVERTED', 'CLOSED');

CREATE TABLE IF NOT EXISTS leads (
    id              BIGSERIAL           PRIMARY KEY,
    lead_id         VARCHAR(50)         NOT NULL UNIQUE,          -- BR-001: auto-generated
    lead_name       VARCHAR(255)        NOT NULL,
    company_name    VARCHAR(255),
    contact_person  VARCHAR(255),
    phone           VARCHAR(20),
    email           VARCHAR(255),
    status          lead_status         NOT NULL DEFAULT 'NEW',   -- BR-002, BR-003
    is_deleted      BOOLEAN             NOT NULL DEFAULT FALSE,   -- BR-005: soft delete
    created_by      BIGINT              REFERENCES users(id),     -- BR-006: audit
    updated_by      BIGINT              REFERENCES users(id),     -- BR-006: audit
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX idx_leads_status     ON leads(status);
CREATE INDEX idx_leads_is_deleted ON leads(is_deleted);
CREATE INDEX idx_leads_lead_id    ON leads(lead_id);
