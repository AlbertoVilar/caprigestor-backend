-- Introduces the technical GoatId graph without changing the active RG graph.
--
-- The application remains unaware of these nullable shadow columns in this
-- wave. Existing RG columns and their constraints remain authoritative until
-- each consumer is migrated in a later module wave.

ALTER TABLE cabras
    ADD COLUMN pai_goat_id BIGINT,
    ADD COLUMN mae_goat_id BIGINT;

ALTER TABLE eventos
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE pregnancy
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE reproductive_event
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE health_events
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE lactation
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE milk_production
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE animal_sale
    ADD COLUMN goat_technical_id BIGINT;

ALTER TABLE operational_audit_entry
    ADD COLUMN goat_technical_id BIGINT;

-- PostgreSQL requires an exact candidate key for the farm-scoped target.
ALTER TABLE cabras
    ADD CONSTRAINT uk_cabras_farm_id
        UNIQUE (capril_id, id);

-- Milk production keeps lactation as its integrity boundary.
ALTER TABLE lactation
    ADD CONSTRAINT uk_lactation_farm_goat_technical_id
        UNIQUE (farm_id, goat_technical_id, id);

-- Backfill local genealogy by the globally unique persisted RG. A farm
-- predicate is deliberately not used: local parents may belong to another
-- farm under the current genealogy rules.
UPDATE cabras child
SET pai_goat_id = parent.id
FROM cabras parent
WHERE child.pai_num_registro IS NOT NULL
  AND child.pai_num_registro = parent.num_registro;

UPDATE cabras child
SET mae_goat_id = parent.id
FROM cabras parent
WHERE child.mae_num_registro IS NOT NULL
  AND child.mae_num_registro = parent.num_registro;

-- Events do not carry farm_id, so their technical reference remains global.
UPDATE eventos event_row
SET goat_technical_id = goat.id
FROM cabras goat
WHERE event_row.goat_registration_number = goat.num_registro;

-- Farm-scoped references must resolve through both farm_id and the persisted
-- RG before receiving a technical GoatId.
UPDATE pregnancy row_data
SET goat_technical_id = goat.id
FROM cabras goat
WHERE goat.capril_id = row_data.farm_id
  AND goat.num_registro = row_data.goat_id;

UPDATE reproductive_event row_data
SET goat_technical_id = goat.id
FROM cabras goat
WHERE goat.capril_id = row_data.farm_id
  AND goat.num_registro = row_data.goat_id;

UPDATE health_events row_data
SET goat_technical_id = goat.id
FROM cabras goat
WHERE goat.capril_id = row_data.farm_id
  AND goat.num_registro = row_data.goat_id;

UPDATE lactation row_data
SET goat_technical_id = goat.id
FROM cabras goat
WHERE goat.capril_id = row_data.farm_id
  AND goat.num_registro = row_data.goat_id;

UPDATE animal_sale row_data
SET goat_technical_id = goat.id
FROM cabras goat
WHERE goat.capril_id = row_data.farm_id
  AND goat.num_registro = row_data.goat_registration_number;

UPDATE operational_audit_entry row_data
SET goat_technical_id = goat.id
FROM cabras goat
WHERE row_data.goat_registration_number IS NOT NULL
  AND goat.capril_id = row_data.farm_id
  AND goat.num_registro = row_data.goat_registration_number;

-- Milk production derives its technical reference from the already resolved
-- lactation relation. This preserves the existing farm + RG + lactation
-- integrity boundary without inventing a direct cabras FK.
UPDATE milk_production production
SET goat_technical_id = lactation.goat_technical_id
FROM lactation
WHERE lactation.farm_id = production.farm_id
  AND lactation.id = production.lactation_id
  AND lactation.goat_id = production.goat_id;

-- Fail closed before creating technical constraints. Existing V38 FKs make
-- these cases unexpected, but explicit checks keep the migration safe against
-- drift and prevent silently choosing an ambiguous relationship.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM cabras child
        LEFT JOIN cabras parent
            ON parent.num_registro = child.pai_num_registro
        WHERE child.pai_num_registro IS NOT NULL
        GROUP BY child.num_registro, child.pai_num_registro
        HAVING COUNT(parent.num_registro) <> 1
    ) THEN
        RAISE EXCEPTION 'V40: pai_num_registro nao resolve exatamente um Goat';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM cabras child
        LEFT JOIN cabras parent
            ON parent.num_registro = child.mae_num_registro
        WHERE child.mae_num_registro IS NOT NULL
        GROUP BY child.num_registro, child.mae_num_registro
        HAVING COUNT(parent.num_registro) <> 1
    ) THEN
        RAISE EXCEPTION 'V40: mae_num_registro nao resolve exatamente um Goat';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE pai_num_registro IS NOT NULL AND pai_goat_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: pai_goat_id ficou NULL para pai local';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE mae_num_registro IS NOT NULL AND mae_goat_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: mae_goat_id ficou NULL para mae local';
    END IF;

    IF EXISTS (
        SELECT 1 FROM eventos
        WHERE goat_registration_number IS NOT NULL
          AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: evento nao resolve goat_registration_number';
    END IF;

    IF EXISTS (
        SELECT 1 FROM pregnancy
        WHERE goat_id IS NOT NULL AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: pregnancy nao resolve farm_id + goat_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM reproductive_event
        WHERE goat_id IS NOT NULL AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: reproductive_event nao resolve farm_id + goat_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM health_events
        WHERE goat_id IS NOT NULL AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: health_events nao resolve farm_id + goat_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM lactation
        WHERE goat_id IS NOT NULL AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: lactation nao resolve farm_id + goat_id';
    END IF;

    IF EXISTS (
        SELECT 1 FROM animal_sale
        WHERE goat_registration_number IS NOT NULL
          AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: animal_sale nao resolve farm_id + RG';
    END IF;

    IF EXISTS (
        SELECT 1 FROM operational_audit_entry
        WHERE goat_registration_number IS NOT NULL
          AND goat_technical_id IS NULL
    ) THEN
        RAISE EXCEPTION 'V40: operational_audit_entry nao resolve farm_id + RG';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM milk_production production
        LEFT JOIN lactation
            ON lactation.farm_id = production.farm_id
           AND lactation.id = production.lactation_id
        WHERE production.goat_id IS NOT NULL
          AND (
              production.goat_technical_id IS NULL
              OR lactation.goat_technical_id IS DISTINCT FROM production.goat_technical_id
          )
    ) THEN
        RAISE EXCEPTION 'V40: milk_production nao coincide com sua lactation';
    END IF;
END
$$;

ALTER TABLE cabras
    ADD CONSTRAINT fk_cabras_pai_goat_id
        FOREIGN KEY (pai_goat_id)
        REFERENCES cabras (id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_cabras_mae_goat_id
        FOREIGN KEY (mae_goat_id)
        REFERENCES cabras (id)
        ON DELETE SET NULL;

ALTER TABLE eventos
    ADD CONSTRAINT fk_eventos_goat_technical_id
        FOREIGN KEY (goat_technical_id)
        REFERENCES cabras (id)
        ON DELETE CASCADE;

ALTER TABLE pregnancy
    ADD CONSTRAINT fk_pregnancy_farm_goat_technical
        FOREIGN KEY (farm_id, goat_technical_id)
        REFERENCES cabras (capril_id, id)
        ON DELETE NO ACTION;

ALTER TABLE reproductive_event
    ADD CONSTRAINT fk_reproductive_event_farm_goat_technical
        FOREIGN KEY (farm_id, goat_technical_id)
        REFERENCES cabras (capril_id, id)
        ON DELETE NO ACTION;

ALTER TABLE health_events
    ADD CONSTRAINT fk_health_events_farm_goat_technical
        FOREIGN KEY (farm_id, goat_technical_id)
        REFERENCES cabras (capril_id, id)
        ON DELETE NO ACTION;

ALTER TABLE lactation
    ADD CONSTRAINT fk_lactation_farm_goat_technical
        FOREIGN KEY (farm_id, goat_technical_id)
        REFERENCES cabras (capril_id, id)
        ON DELETE NO ACTION;

ALTER TABLE milk_production
    ADD CONSTRAINT fk_milk_production_farm_goat_technical_lactation
        FOREIGN KEY (farm_id, goat_technical_id, lactation_id)
        REFERENCES lactation (farm_id, goat_technical_id, id)
        ON DELETE NO ACTION;

ALTER TABLE animal_sale
    ADD CONSTRAINT fk_animal_sale_farm_goat_technical
        FOREIGN KEY (farm_id, goat_technical_id)
        REFERENCES cabras (capril_id, id)
        ON DELETE NO ACTION;

ALTER TABLE operational_audit_entry
    ADD CONSTRAINT fk_operational_audit_entry_farm_goat_technical
        FOREIGN KEY (farm_id, goat_technical_id)
        REFERENCES cabras (capril_id, id)
        ON DELETE NO ACTION;
