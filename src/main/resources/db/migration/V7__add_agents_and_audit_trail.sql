-- Account system: agents (staff/librarian) and audit trail for loans.

CREATE TABLE IF NOT EXISTS agents (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    telephone VARCHAR(32),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agents_email ON agents(email);
CREATE INDEX IF NOT EXISTS idx_agents_actif ON agents(actif);

CREATE TABLE IF NOT EXISTS audit_loans (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES emprunts(id) ON DELETE CASCADE,
    agent_id BIGINT NOT NULL REFERENCES agents(id) ON DELETE RESTRICT,
    action VARCHAR(32) NOT NULL,
    date_action TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_loans_loan_id ON audit_loans(loan_id);
CREATE INDEX IF NOT EXISTS idx_audit_loans_agent_id ON audit_loans(agent_id);
CREATE INDEX IF NOT EXISTS idx_audit_loans_date_action ON audit_loans(date_action);