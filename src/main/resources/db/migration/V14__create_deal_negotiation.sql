-- ============================================================
-- V14: Deal Negotiation (SAL-004) + Deal Won/Lost (SAL-005)
-- FILLINUS ERP — Sales Module
--
-- Deal Negotiation is only created FROM a Quotation (BR-001, SAL-004) via
-- POST /quotations/{id}/deal-negotiation — no standalone create. Deal Result
-- (SAL-005) is only created FROM a Deal Negotiation via "Complete Negotiation"
-- and is a one-shot decision (BC-001/BC-006): once saved it cannot be edited,
-- and Delete is not supported in phase 1 (SAL-005 Flow 3.4).
--
-- Client auto-creation (SAL-005 BR-004) and Project creation (PAC-001) are
-- skipped — no Customer/Client master (SAL-007) or Project module exists yet,
-- same situation as Opportunity/Quotation's free-text "customer" field.
-- Communication Channel is free-text — no MST Communication Channel master yet.
-- ============================================================

CREATE TYPE deal_negotiation_status AS ENUM ('NEGOTIATING', 'WON', 'LOST');
CREATE TYPE deal_result_type AS ENUM ('WON', 'LOST');

CREATE TABLE IF NOT EXISTS deal_negotiations (
    id                      BIGSERIAL                  PRIMARY KEY,
    negotiation_no          VARCHAR(30)                 NOT NULL UNIQUE,
    quotation_id            BIGINT                       NOT NULL UNIQUE REFERENCES quotations(id),
    opportunity_id          BIGINT                       NOT NULL REFERENCES opportunities(id),
    customer                VARCHAR(255)                 NOT NULL,   -- inherited from Quotation at creation (BR-001)
    sales_rep_id            BIGINT                       NOT NULL REFERENCES users(id),
    meeting_date            DATE                         NOT NULL,
    communication_channel   VARCHAR(50)                  NOT NULL,
    contact_person          VARCHAR(200),
    internal_note           TEXT,
    status                  deal_negotiation_status      NOT NULL DEFAULT 'NEGOTIATING',
    is_deleted              BOOLEAN                      NOT NULL DEFAULT FALSE,
    created_by              BIGINT                       REFERENCES users(id),
    updated_by              BIGINT                       REFERENCES users(id),
    created_at              TIMESTAMP                    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_deal_negotiations_sales_rep_id ON deal_negotiations(sales_rep_id);
CREATE INDEX IF NOT EXISTS idx_deal_negotiations_is_deleted   ON deal_negotiations(is_deleted);

-- BR-003: each negotiation update is appended as a new, immutable history row
CREATE TABLE IF NOT EXISTS deal_negotiation_history (
    id                      BIGSERIAL       PRIMARY KEY,
    deal_negotiation_id     BIGINT          NOT NULL REFERENCES deal_negotiations(id) ON DELETE CASCADE,
    discussion_date         TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_by              BIGINT          REFERENCES users(id),
    discussion              TEXT            NOT NULL,
    customer_feedback       TEXT,
    next_action             TEXT,
    next_follow_up_date     DATE
);

CREATE INDEX IF NOT EXISTS idx_deal_negotiation_history_deal_id ON deal_negotiation_history(deal_negotiation_id);

-- BC-001: at most one final result per Deal Negotiation (enforced by UNIQUE)
CREATE TABLE IF NOT EXISTS deal_results (
    id                      BIGSERIAL           PRIMARY KEY,
    deal_negotiation_id     BIGINT              NOT NULL UNIQUE REFERENCES deal_negotiations(id),
    quotation_id            BIGINT              NOT NULL REFERENCES quotations(id),
    opportunity_id          BIGINT              NOT NULL REFERENCES opportunities(id),
    customer                VARCHAR(255)        NOT NULL,
    sales_rep_id            BIGINT              NOT NULL REFERENCES users(id),
    deal_amount             DECIMAL(18,2)       NOT NULL DEFAULT 0,
    result                  deal_result_type    NOT NULL,
    comment                 VARCHAR(1000),
    decision_by             BIGINT              REFERENCES users(id),
    decision_date           TIMESTAMP           NOT NULL DEFAULT NOW(),
    is_deleted              BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_deal_results_sales_rep_id ON deal_results(sales_rep_id);
