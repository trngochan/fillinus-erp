-- ============================================================
-- V13: Quotation (SAL-003) — new module
-- FILLINUS ERP — Sales Module
--
-- Quotation is only created FROM an Opportunity (BR-001) — no standalone
-- "create" endpoint exists. "product_service"/"unit"/"standard_price" are
-- temp free-text/manual fields — no Product Info master data yet (SAL-009).
--
-- Status values follow the Screen Item enum (Draft/Sent/Accepted/Rejected/
-- Expired); "Deleted" from the spec's Search dropdown is represented by the
-- existing is_deleted soft-delete flag, not a status value (see AGENTS.md /
-- session decision — Business Rule's "Negotiating" trigger maps to "Sent").
-- ============================================================

CREATE TYPE quotation_status AS ENUM ('DRAFT', 'SENT', 'ACCEPTED', 'REJECTED', 'EXPIRED');
CREATE TYPE quotation_currency AS ENUM ('VND', 'USD');

CREATE TABLE IF NOT EXISTS quotations (
    id              BIGSERIAL           PRIMARY KEY,
    quotation_no    VARCHAR(50)         NOT NULL UNIQUE,
    opportunity_id  BIGINT              NOT NULL REFERENCES opportunities(id),
    customer        VARCHAR(255)        NOT NULL,   -- inherited from Opportunity at creation (BR-002)
    sales_rep_id    BIGINT              NOT NULL REFERENCES users(id),
    quotation_date  DATE                NOT NULL DEFAULT CURRENT_DATE,
    expired_date    DATE                NOT NULL,
    currency        quotation_currency  NOT NULL DEFAULT 'VND',
    status          quotation_status    NOT NULL DEFAULT 'DRAFT',
    remark          VARCHAR(500),
    total_amount    DECIMAL(18,2)       NOT NULL DEFAULT 0,
    is_deleted      BOOLEAN             NOT NULL DEFAULT FALSE,
    created_by      BIGINT              REFERENCES users(id),
    updated_by      BIGINT              REFERENCES users(id),
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quotations_opportunity_id ON quotations(opportunity_id);
CREATE INDEX IF NOT EXISTS idx_quotations_sales_rep_id   ON quotations(sales_rep_id);
CREATE INDEX IF NOT EXISTS idx_quotations_is_deleted     ON quotations(is_deleted);

CREATE TABLE IF NOT EXISTS quotation_details (
    id              BIGSERIAL       PRIMARY KEY,
    quotation_id    BIGINT          NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    product_service VARCHAR(255)    NOT NULL,   -- temp free-text — no Product Info master yet (SAL-009)
    quantity        DECIMAL(18,2)   NOT NULL DEFAULT 1,
    unit            VARCHAR(50),
    standard_price  DECIMAL(18,2)   NOT NULL DEFAULT 0,   -- manual entry — normally loaded from Product Info
    unit_price      DECIMAL(18,2)   NOT NULL DEFAULT 0,
    amount          DECIMAL(18,2)   NOT NULL DEFAULT 0,   -- quantity * unit_price, recalculated on save
    remark          VARCHAR(500),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quotation_details_quotation_id ON quotation_details(quotation_id);
