-- ============================================================
-- V11: Lead V1.1 — Source, Sales Rep, Remark + status migration + unique Phone/Email
-- FILLINUS ERP — Sales Module (SAL-001 V1.1)
-- ============================================================

CREATE TYPE lead_source AS ENUM ('New Client', 'Existing Client', 'Referral', 'Digital Lead');

ALTER TABLE leads
    ADD COLUMN IF NOT EXISTS source lead_source,
    ADD COLUMN IF NOT EXISTS sales_rep_id BIGINT REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS remark TEXT;

-- Migrate existing data to the new status set (BR: Convert -> Qualified, replaces old Convert -> Converted)
UPDATE leads SET status = 'QUALIFIED' WHERE status = 'CONVERTED';
UPDATE leads SET status = 'REJECTED'  WHERE status = 'CLOSED';

-- Default existing rows' Sales Rep to whoever created them (best-effort backfill for pre-V1.1 data)
UPDATE leads SET sales_rep_id = created_by WHERE sales_rep_id IS NULL;

-- De-duplicate any pre-existing test data before enforcing uniqueness (partial index, active rows only)
UPDATE leads a SET phone = a.phone || '-dup' || a.id
    WHERE a.is_deleted = false AND a.phone IS NOT NULL AND EXISTS (
        SELECT 1 FROM leads b WHERE b.phone = a.phone AND b.is_deleted = false AND b.id < a.id
    );
UPDATE leads a SET email = a.email || '.dup' || a.id
    WHERE a.is_deleted = false AND a.email IS NOT NULL AND EXISTS (
        SELECT 1 FROM leads b WHERE b.email = a.email AND b.is_deleted = false AND b.id < a.id
    );

-- Phone/Email must be unique among active (non-soft-deleted) leads — BR: no duplicate Phone/Email
CREATE UNIQUE INDEX IF NOT EXISTS uq_leads_phone_active ON leads(phone) WHERE is_deleted = false;
CREATE UNIQUE INDEX IF NOT EXISTS uq_leads_email_active ON leads(email) WHERE is_deleted = false;
