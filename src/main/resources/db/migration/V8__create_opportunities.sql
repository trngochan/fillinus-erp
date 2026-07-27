-- ============================================================
-- V8: Create opportunities table
-- FILLINUS ERP — Sales Module (SAL-002)
-- ============================================================

CREATE TYPE opportunity_status AS ENUM ('NEW', 'IN_PROGRESS', 'WON', 'LOST');

CREATE TABLE IF NOT EXISTS opportunities (
    id              BIGSERIAL           PRIMARY KEY,
    opportunity_id  VARCHAR(50)         NOT NULL UNIQUE,          -- auto-generated
    lead_id         BIGINT              NOT NULL REFERENCES leads(id),
    lead_name       VARCHAR(255)        NOT NULL,                 -- copied from lead
    company_name    VARCHAR(255),                                 -- copied from lead
    contact_person  VARCHAR(255),                                 -- copied from lead
    phone           VARCHAR(20),                                  -- copied from lead
    email           VARCHAR(255),                                 -- copied from lead
    assigned_to     BIGINT              NOT NULL REFERENCES users(id),  -- saler who converted
    status          opportunity_status  NOT NULL DEFAULT 'NEW',
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX idx_opp_assigned_to  ON opportunities(assigned_to);
CREATE INDEX idx_opp_lead_id      ON opportunities(lead_id);
