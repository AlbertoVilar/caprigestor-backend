CREATE TABLE operational_audit_entry (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL REFERENCES capril(id),
    goat_registration_number VARCHAR(20),
    action_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(80),
    actor_user_id BIGINT NOT NULL,
    actor_name VARCHAR(100) NOT NULL,
    actor_email VARCHAR(150) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_operational_audit_entry_farm_created
    ON operational_audit_entry (farm_id, created_at DESC, id DESC);

CREATE INDEX idx_operational_audit_entry_farm_goat_created
    ON operational_audit_entry (farm_id, goat_registration_number, created_at DESC, id DESC);
