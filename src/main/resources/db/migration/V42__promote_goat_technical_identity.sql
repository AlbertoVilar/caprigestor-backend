-- Promote the immutable technical identity to the database primary key.
--
-- V39 introduced cabras.id as an identity shadow, V40/V41 migrated the
-- dependent consumers to technical references.  This migration keeps
-- num_registro as a unique business/ABCC identifier and removes only the
-- legacy single-column foreign keys that depended on the former RG primary
-- key.  Farm-scoped technical FKs from V40/V41 already target cabras(id) and
-- are intentionally left intact.

ALTER TABLE cabras
    ADD CONSTRAINT uk_cabras_registration_number
        UNIQUE (num_registro);

-- PostgreSQL binds these legacy FKs to the old primary-key constraint.  They
-- are recreated after the technical PK is installed so that the business RG
-- remains referentially valid without being the structural identity.
ALTER TABLE cabras
    DROP CONSTRAINT IF EXISTS cabras_pai_num_registro_fkey,
    DROP CONSTRAINT IF EXISTS cabras_mae_num_registro_fkey;

ALTER TABLE eventos
    DROP CONSTRAINT IF EXISTS eventos_goat_registration_number_fkey;

ALTER TABLE health_events
    DROP CONSTRAINT IF EXISTS fk_health_events_goat;

ALTER TABLE animal_sale
    DROP CONSTRAINT IF EXISTS animal_sale_goat_registration_number_fkey;

ALTER TABLE cabras
    DROP CONSTRAINT cabras_pkey;

ALTER TABLE cabras
    ADD CONSTRAINT pk_cabras_technical_id PRIMARY KEY (id);

ALTER TABLE cabras
    ADD CONSTRAINT fk_cabras_father_registration
        FOREIGN KEY (pai_num_registro) REFERENCES cabras (num_registro)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_cabras_mother_registration
        FOREIGN KEY (mae_num_registro) REFERENCES cabras (num_registro)
        ON DELETE SET NULL;

ALTER TABLE eventos
    ADD CONSTRAINT fk_eventos_goat_registration
        FOREIGN KEY (goat_registration_number) REFERENCES cabras (num_registro)
        ON DELETE CASCADE;

ALTER TABLE health_events
    ADD CONSTRAINT fk_health_events_goat_registration
        FOREIGN KEY (goat_id) REFERENCES cabras (num_registro);

ALTER TABLE animal_sale
    ADD CONSTRAINT fk_animal_sale_goat_registration
        FOREIGN KEY (goat_registration_number) REFERENCES cabras (num_registro);

COMMENT ON COLUMN cabras.id IS 'Immutable technical GoatId and structural primary key';
COMMENT ON COLUMN cabras.num_registro IS 'Current registral RG, unique business identifier and ABCC lookup value';
