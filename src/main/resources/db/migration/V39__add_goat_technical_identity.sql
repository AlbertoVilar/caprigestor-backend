-- Adds a shadow technical identity without changing the structural RG.
--
-- The temporary sequence is used only for the backfill. The final sequence
-- is created when the column becomes GENERATED ALWAYS AS IDENTITY and is
-- explicitly aligned with the greatest assigned value.

ALTER TABLE cabras
    ADD COLUMN id BIGINT;

CREATE SEQUENCE cabras_id_backfill_seq
    AS BIGINT
    START WITH 1
    INCREMENT BY 1;

UPDATE cabras
SET id = nextval('cabras_id_backfill_seq'::regclass)
WHERE id IS NULL;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM cabras WHERE id IS NULL) THEN
        RAISE EXCEPTION 'V39: cabras.id contains NULL values after backfill';
    END IF;

    IF EXISTS (
        SELECT id
        FROM cabras
        GROUP BY id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'V39: cabras.id contains duplicate values after backfill';
    END IF;
END
$$;

ALTER TABLE cabras
    ADD CONSTRAINT uk_cabras_id UNIQUE (id);

ALTER TABLE cabras
    ALTER COLUMN id SET NOT NULL;

DROP SEQUENCE cabras_id_backfill_seq;

ALTER TABLE cabras
    ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY
        (SEQUENCE NAME cabras_id_seq START WITH 1 INCREMENT BY 1);

DO $$
DECLARE
    max_id BIGINT;
BEGIN
    SELECT MAX(id) INTO max_id FROM cabras;

    IF max_id IS NULL THEN
        PERFORM setval('cabras_id_seq'::regclass, 1, false);
    ELSE
        PERFORM setval('cabras_id_seq'::regclass, max_id, true);
    END IF;
END
$$;
