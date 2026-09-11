-- The current registral number remains a unique business/ABCC identifier,
-- but it is mutable through the explicit rectification use case. Structural
-- relations therefore use cabras.id exclusively from this version onward.

ALTER TABLE cabras
    DROP CONSTRAINT IF EXISTS fk_cabras_father_registration,
    DROP CONSTRAINT IF EXISTS fk_cabras_mother_registration;

ALTER TABLE eventos
    DROP CONSTRAINT IF EXISTS fk_eventos_goat_registration;

ALTER TABLE pregnancy
    DROP CONSTRAINT IF EXISTS fk_pregnancy_farm_goat;

ALTER TABLE reproductive_event
    DROP CONSTRAINT IF EXISTS fk_reproductive_event_farm_goat;

ALTER TABLE health_events
    DROP CONSTRAINT IF EXISTS fk_health_events_farm_goat,
    DROP CONSTRAINT IF EXISTS fk_health_events_goat_registration;

ALTER TABLE lactation
    DROP CONSTRAINT IF EXISTS fk_lactation_farm_goat;

ALTER TABLE animal_sale
    DROP CONSTRAINT IF EXISTS fk_animal_sale_farm_goat,
    DROP CONSTRAINT IF EXISTS fk_animal_sale_goat_registration;

ALTER TABLE operational_audit_entry
    DROP CONSTRAINT IF EXISTS fk_operational_audit_entry_farm_goat;

CREATE TABLE goat_registration_history (
    id BIGSERIAL PRIMARY KEY,
    goat_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    old_registration_number VARCHAR(20) NOT NULL,
    old_tod VARCHAR(15),
    old_toe VARCHAR(15),
    new_registration_number VARCHAR(20) NOT NULL,
    new_tod VARCHAR(15),
    new_toe VARCHAR(15),
    source VARCHAR(30) NOT NULL,
    evidence_reference VARCHAR(255) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goat_registration_history_goat
        FOREIGN KEY (goat_id) REFERENCES cabras(id) ON DELETE RESTRICT,
    CONSTRAINT fk_goat_registration_history_farm
        FOREIGN KEY (farm_id) REFERENCES capril(id) ON DELETE RESTRICT,
    CONSTRAINT fk_goat_registration_history_farm_goat
        FOREIGN KEY (farm_id, goat_id) REFERENCES cabras (capril_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_goat_registration_history_actor
        FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT ck_goat_registration_history_source
        CHECK (source IN ('ABCC', 'OFFICIAL_DOCUMENT', 'OTHER')),
    CONSTRAINT ck_goat_registration_history_evidence_not_blank
        CHECK (length(btrim(evidence_reference)) > 0),
    CONSTRAINT ck_goat_registration_history_reason_not_blank
        CHECK (length(btrim(reason)) > 0)
);

CREATE INDEX idx_goat_registration_history_goat_created
    ON goat_registration_history (goat_id, created_at DESC, id DESC);

CREATE INDEX idx_goat_registration_history_farm_goat
    ON goat_registration_history (farm_id, goat_id, created_at DESC, id DESC);

COMMENT ON TABLE goat_registration_history IS
    'Auditable registral identity corrections; old/new RG values are historical snapshots.';
COMMENT ON COLUMN goat_registration_history.goat_id IS
    'Stable technical GoatId; the immutable structural identity.';
COMMENT ON COLUMN goat_registration_history.old_registration_number IS
    'RG recorded immediately before the correction.';
COMMENT ON COLUMN goat_registration_history.new_registration_number IS
    'RG derived from the corrected TOD + TOE.';
