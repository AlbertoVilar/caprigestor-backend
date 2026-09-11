-- Completes the first structural GoatId consumer wave.
--
-- V39 introduced cabras.id and V40 backfilled nullable technical shadows while
-- preserving the RG columns as business/historical snapshots.  From this
-- migration onward every persisted goat-dependent record must carry the
-- technical identity; the old RG columns remain intentionally available for
-- display, external ABCC references and historical audit snapshots.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM eventos WHERE goat_technical_id IS NULL) THEN
        RAISE EXCEPTION 'V41: eventos possui referencia tecnica nula';
    END IF;

    IF EXISTS (SELECT 1 FROM pregnancy WHERE goat_technical_id IS NULL) THEN
        RAISE EXCEPTION 'V41: pregnancy possui referencia tecnica nula';
    END IF;

    IF EXISTS (SELECT 1 FROM reproductive_event WHERE goat_technical_id IS NULL) THEN
        RAISE EXCEPTION 'V41: reproductive_event possui referencia tecnica nula';
    END IF;

    IF EXISTS (SELECT 1 FROM health_events WHERE goat_technical_id IS NULL) THEN
        RAISE EXCEPTION 'V41: health_events possui referencia tecnica nula';
    END IF;

    IF EXISTS (SELECT 1 FROM lactation WHERE goat_technical_id IS NULL) THEN
        RAISE EXCEPTION 'V41: lactation possui referencia tecnica nula';
    END IF;

    IF EXISTS (SELECT 1 FROM milk_production WHERE goat_technical_id IS NULL) THEN
        RAISE EXCEPTION 'V41: milk_production possui referencia tecnica nula';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM animal_sale
        WHERE goat_registration_number IS NOT NULL
          AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V41: animal_sale possui referencia tecnica nula';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM operational_audit_entry
        WHERE goat_registration_number IS NOT NULL
          AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V41: auditoria com RG nao possui referencia tecnica';
    END IF;
END
$$;

ALTER TABLE eventos
    ALTER COLUMN goat_technical_id SET NOT NULL;

ALTER TABLE pregnancy
    ALTER COLUMN goat_technical_id SET NOT NULL;

ALTER TABLE reproductive_event
    ALTER COLUMN goat_technical_id SET NOT NULL;

ALTER TABLE health_events
    ALTER COLUMN goat_technical_id SET NOT NULL;

ALTER TABLE lactation
    ALTER COLUMN goat_technical_id SET NOT NULL;

ALTER TABLE milk_production
    ALTER COLUMN goat_technical_id SET NOT NULL;

ALTER TABLE animal_sale
    ALTER COLUMN goat_technical_id SET NOT NULL;

-- Technical identity is now the access path for all goat-dependent queries.
-- RG indexes remain in place because RG is still a public/business snapshot.
CREATE INDEX idx_cabras_pai_goat_id ON cabras (pai_goat_id);
CREATE INDEX idx_cabras_mae_goat_id ON cabras (mae_goat_id);
CREATE INDEX idx_eventos_goat_technical_id ON eventos (goat_technical_id);
CREATE INDEX idx_pregnancy_farm_goat_technical ON pregnancy (farm_id, goat_technical_id);
CREATE INDEX idx_reproductive_event_farm_goat_technical
    ON reproductive_event (farm_id, goat_technical_id, event_date);
CREATE INDEX idx_health_events_farm_goat_technical
    ON health_events (farm_id, goat_technical_id, scheduled_date);
CREATE INDEX idx_lactation_farm_goat_technical
    ON lactation (farm_id, goat_technical_id, start_date);
CREATE INDEX idx_milk_production_farm_goat_technical_date
    ON milk_production (farm_id, goat_technical_id, date);
CREATE INDEX idx_animal_sale_farm_goat_technical
    ON animal_sale (farm_id, goat_technical_id);
CREATE INDEX idx_operational_audit_entry_farm_goat_technical_created
    ON operational_audit_entry (farm_id, goat_technical_id, created_at DESC, id DESC);

-- Preserve the existing RG invariant during the transition and enforce the
-- same business rule on the stable technical identity.
CREATE UNIQUE INDEX ux_pregnancy_single_active_per_goat_technical
    ON pregnancy (farm_id, goat_technical_id)
    WHERE status = 'ACTIVE';

-- An audit row may be unrelated to a goat.  If it records an RG snapshot,
-- however, it must also identify the stable GoatId.
ALTER TABLE operational_audit_entry
    ADD CONSTRAINT ck_operational_audit_goat_snapshot_consistent
    CHECK (
        goat_registration_number IS NULL
        OR goat_technical_id IS NOT NULL
    );

COMMENT ON COLUMN cabras.id IS
    'Stable technical GoatId. Never expose as a mutable business RG.';

COMMENT ON COLUMN eventos.goat_registration_number IS
    'Historical/public RG snapshot; structural joins use goat_technical_id.';

COMMENT ON COLUMN animal_sale.goat_registration_number IS
    'Sale-time RG snapshot; structural joins use goat_technical_id.';

COMMENT ON COLUMN operational_audit_entry.goat_registration_number IS
    'Audit-time RG snapshot; structural joins use goat_technical_id.';
