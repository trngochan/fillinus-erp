-- ============================================================
-- V6: Add SALE role + seed test accounts
-- FILLINUS ERP — Sales Module
-- ============================================================

-- Add SALE role
INSERT INTO roles (name, description)
VALUES ('SALE', 'Sales Representative — lead and opportunity management')
ON CONFLICT (name) DO NOTHING;
