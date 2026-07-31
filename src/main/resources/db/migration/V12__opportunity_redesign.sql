-- ============================================================
-- V12: Opportunity redesign (SAL-002) — Header + Detail line items
-- FILLINUS ERP — Sales Module
--
-- Replaces the old simple "copied from Lead" shape with the new spec:
-- Opportunity Header (name, customer, sales rep, stage, expected revenue,
-- description, lifecycle status) + Opportunity Detail (service/product line
-- items). Customer / Service-Product / Unit are temporary free-text fields —
-- no Customer/Product/Unit master data exists yet (SAL-006/007/009 not built).
--
-- Test data only in this table so far — old columns are dropped rather than
-- carefully migrated; a couple of UPDATEs backfill required new columns from
-- the old ones before the old ones are dropped.
-- ============================================================

CREATE TYPE opportunity_stage AS ENUM (
    'PROSPECTING', 'QUALIFICATION', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'
);
CREATE TYPE opportunity_lifecycle_status AS ENUM ('OPEN', 'CONVERTED', 'CLOSED');

ALTER TABLE opportunities
    ADD COLUMN IF NOT EXISTS opportunity_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS customer VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stage opportunity_stage,
    ADD COLUMN IF NOT EXISTS expected_revenue DECIMAL(18,2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS lifecycle_status opportunity_lifecycle_status;

-- Backfill required new columns from the old ones before dropping them
UPDATE opportunities SET opportunity_name = COALESCE(lead_name, opportunity_id) WHERE opportunity_name IS NULL;
UPDATE opportunities SET customer = COALESCE(NULLIF(TRIM(COALESCE(company_name,'') || ' / ' || COALESCE(contact_person,'')), ' /'), 'Unknown') WHERE customer IS NULL;
UPDATE opportunities SET stage = 'PROSPECTING' WHERE stage IS NULL;
UPDATE opportunities SET lifecycle_status = 'OPEN' WHERE lifecycle_status IS NULL;

ALTER TABLE opportunities
    ALTER COLUMN opportunity_name SET NOT NULL,
    ALTER COLUMN customer SET NOT NULL,
    ALTER COLUMN stage SET NOT NULL,
    ALTER COLUMN lifecycle_status SET NOT NULL;

-- lead_id becomes optional — Opportunity can now be created directly (Business-02)
ALTER TABLE opportunities ALTER COLUMN lead_id DROP NOT NULL;

-- rename assigned_to -> sales_rep_id (same meaning, new naming to match Lead.sales_rep_id)
ALTER TABLE opportunities RENAME COLUMN assigned_to TO sales_rep_id;

-- Drop fields no longer part of the Opportunity shape (contact info now lives on Customer,
-- which is a temp free-text field until SAL-007 Existing Client exists)
ALTER TABLE opportunities
    DROP COLUMN IF EXISTS lead_name,
    DROP COLUMN IF EXISTS company_name,
    DROP COLUMN IF EXISTS contact_person,
    DROP COLUMN IF EXISTS phone,
    DROP COLUMN IF EXISTS email,
    DROP COLUMN IF EXISTS status;

CREATE TABLE IF NOT EXISTS opportunity_details (
    id              BIGSERIAL       PRIMARY KEY,
    opportunity_id  BIGINT          NOT NULL REFERENCES opportunities(id) ON DELETE CASCADE,
    service_product VARCHAR(255)    NOT NULL,   -- temp free-text — no Product/Service master yet (SAL-009)
    quantity        DECIMAL(18,2)   NOT NULL DEFAULT 1,
    unit            VARCHAR(50),                -- temp free-text — no Unit master yet
    remark          VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_opportunity_details_opportunity_id ON opportunity_details(opportunity_id);
