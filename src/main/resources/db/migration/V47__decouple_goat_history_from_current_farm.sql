-- Decouple historical farm context from the current Goat farm.
--
-- GoatId is the stable biological identity.  farm_id remains the historical
-- or organizational context of each record and must not be required to match
-- cabras.capril_id after a future ownership change.

-- Fail closed if the pre-existing process graph already mixes biological
-- identities.  The migration must never silently repair such data.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM reproductive_event event
        JOIN pregnancy pregnancy ON pregnancy.id = event.pregnancy_id
        WHERE event.pregnancy_id IS NOT NULL
          AND event.goat_technical_id IS DISTINCT FROM pregnancy.goat_technical_id
    ) THEN
        RAISE EXCEPTION 'V47: reproductive event and pregnancy GoatIds diverge';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM reproductive_event event
        JOIN reproductive_event related ON related.id = event.related_event_id
        WHERE event.related_event_id IS NOT NULL
          AND event.goat_technical_id IS DISTINCT FROM related.goat_technical_id
    ) THEN
        RAISE EXCEPTION 'V47: reproductive event and related event GoatIds diverge';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pregnancy pregnancy
        JOIN reproductive_event coverage ON coverage.id = pregnancy.coverage_event_id
        WHERE pregnancy.coverage_event_id IS NOT NULL
          AND pregnancy.goat_technical_id IS DISTINCT FROM coverage.goat_technical_id
    ) THEN
        RAISE EXCEPTION 'V47: pregnancy and coverage event GoatIds diverge';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM milk_production production
        JOIN lactation lactation ON lactation.id = production.lactation_id
        WHERE production.lactation_id IS NOT NULL
          AND production.goat_technical_id IS DISTINCT FROM lactation.goat_technical_id
    ) THEN
        RAISE EXCEPTION 'V47: milk production and lactation GoatIds diverge';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM milk_production production
        JOIN health_events withdrawal ON withdrawal.id = production.milk_withdrawal_event_id
        WHERE production.milk_withdrawal_event_id IS NOT NULL
          AND production.goat_technical_id IS DISTINCT FROM withdrawal.goat_technical_id
    ) THEN
        RAISE EXCEPTION 'V47: milk production and withdrawal event GoatIds diverge';
    END IF;
END
$$;

ALTER TABLE pregnancy
    DROP CONSTRAINT IF EXISTS fk_pregnancy_farm_goat_technical,
    DROP CONSTRAINT IF EXISTS fk_pregnancy_farm_coverage_event;

ALTER TABLE reproductive_event
    DROP CONSTRAINT IF EXISTS fk_reproductive_event_farm_goat_technical,
    DROP CONSTRAINT IF EXISTS fk_reproductive_event_farm_pregnancy,
    DROP CONSTRAINT IF EXISTS fk_reproductive_event_farm_related_event;

ALTER TABLE health_events
    DROP CONSTRAINT IF EXISTS fk_health_events_farm_goat_technical;

ALTER TABLE lactation
    DROP CONSTRAINT IF EXISTS fk_lactation_farm_goat_technical;

ALTER TABLE milk_production
    DROP CONSTRAINT IF EXISTS fk_milk_production_farm_goat_lactation,
    DROP CONSTRAINT IF EXISTS fk_milk_production_farm_goat_technical_lactation,
    DROP CONSTRAINT IF EXISTS fk_milk_production_farm_withdrawal_event;

ALTER TABLE animal_sale
    DROP CONSTRAINT IF EXISTS fk_animal_sale_farm_goat_technical;

ALTER TABLE operational_audit_entry
    DROP CONSTRAINT IF EXISTS fk_operational_audit_entry_farm_goat_technical;

-- Replace the old simple process FKs with GoatId+process candidate keys.  The
-- process id alone is not sufficient: it could belong to another Goat.
ALTER TABLE reproductive_event
    DROP CONSTRAINT IF EXISTS fk_reproductive_event_pregnancy;

ALTER TABLE milk_production
    DROP CONSTRAINT IF EXISTS fk_milk_production_lactation;

ALTER TABLE pregnancy
    ADD CONSTRAINT uk_pregnancy_goat_technical_id UNIQUE (goat_technical_id, id);

ALTER TABLE reproductive_event
    ADD CONSTRAINT uk_reproductive_event_goat_technical_id UNIQUE (goat_technical_id, id);

ALTER TABLE health_events
    ADD CONSTRAINT uk_health_events_goat_technical_id UNIQUE (goat_technical_id, id);

ALTER TABLE lactation
    ADD CONSTRAINT uk_lactation_goat_technical_id UNIQUE (goat_technical_id, id);

-- Preserve process identity directly when the former farm-scoped FK is removed.
ALTER TABLE reproductive_event
    ADD CONSTRAINT fk_reproductive_event_pregnancy
        FOREIGN KEY (goat_technical_id, pregnancy_id)
        REFERENCES pregnancy (goat_technical_id, id)
        ON DELETE SET NULL (pregnancy_id)
        NOT VALID,
    ADD CONSTRAINT fk_reproductive_event_related_event_direct
        FOREIGN KEY (goat_technical_id, related_event_id)
        REFERENCES reproductive_event (goat_technical_id, id)
        ON DELETE NO ACTION
        NOT VALID;

ALTER TABLE pregnancy
    ADD CONSTRAINT fk_pregnancy_coverage_event_direct
        FOREIGN KEY (goat_technical_id, coverage_event_id)
        REFERENCES reproductive_event (goat_technical_id, id)
        ON DELETE NO ACTION
        NOT VALID;

ALTER TABLE milk_production
    ADD CONSTRAINT fk_milk_production_lactation
        FOREIGN KEY (goat_technical_id, lactation_id)
        REFERENCES lactation (goat_technical_id, id)
        ON DELETE NO ACTION
        NOT VALID,
    ADD CONSTRAINT fk_milk_production_withdrawal_event_direct
        FOREIGN KEY (goat_technical_id, milk_withdrawal_event_id)
        REFERENCES health_events (goat_technical_id, id)
        ON DELETE NO ACTION
        NOT VALID;

ALTER TABLE reproductive_event
    VALIDATE CONSTRAINT fk_reproductive_event_pregnancy;

ALTER TABLE reproductive_event
    VALIDATE CONSTRAINT fk_reproductive_event_related_event_direct;

ALTER TABLE pregnancy
    VALIDATE CONSTRAINT fk_pregnancy_coverage_event_direct;

ALTER TABLE milk_production
    VALIDATE CONSTRAINT fk_milk_production_lactation;

ALTER TABLE milk_production
    VALIDATE CONSTRAINT fk_milk_production_withdrawal_event_direct;

-- A biological Goat can have at most one active pregnancy globally, regardless
-- of the farm that owns the current record.
DO $$
BEGIN
    IF EXISTS (
        SELECT goat_technical_id
        FROM pregnancy
        WHERE status = 'ACTIVE'
        GROUP BY goat_technical_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'V47: multiple active pregnancies exist for the same GoatId';
    END IF;
END
$$;

DROP INDEX IF EXISTS ux_pregnancy_single_active_per_goat_technical;
DROP INDEX IF EXISTS ux_pregnancy_single_active_per_goat;
CREATE UNIQUE INDEX ux_pregnancy_single_active_per_goat_technical
    ON pregnancy (goat_technical_id)
    WHERE status = 'ACTIVE';

-- A biological Goat can have at most one active lactation globally, regardless
-- of the farm that owns the current record.
DO $$
BEGIN
    IF EXISTS (
        SELECT goat_technical_id
        FROM lactation
        WHERE status = 'ACTIVE'
        GROUP BY goat_technical_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'V47: multiple active lactations exist for the same GoatId';
    END IF;
END
$$;

DROP INDEX IF EXISTS ux_lactation_single_active_per_goat_technical;
CREATE UNIQUE INDEX ux_lactation_single_active_per_goat_technical
    ON lactation (goat_technical_id)
    WHERE status = 'ACTIVE';
