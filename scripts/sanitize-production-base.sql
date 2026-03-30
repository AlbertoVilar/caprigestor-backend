\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE _preserved_farms (
    id BIGINT PRIMARY KEY
) ON COMMIT DROP;

INSERT INTO _preserved_farms (id)
VALUES (1), (14);

CREATE TEMP TABLE _preserved_users (
    id BIGINT PRIMARY KEY
) ON COMMIT DROP;

INSERT INTO _preserved_users (id)
VALUES (1), (14);

CREATE TEMP TABLE _deleted_counts (
    table_name TEXT PRIMARY KEY,
    deleted_count BIGINT NOT NULL
) ON COMMIT DROP;

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM capril WHERE id IN (SELECT id FROM _preserved_farms)) <> 2 THEN
        RAISE EXCEPTION 'Saneamento abortado: nem todas as fazendas preservadas foram encontradas.';
    END IF;

    IF (SELECT COUNT(*) FROM users WHERE id IN (SELECT id FROM _preserved_users)) <> 2 THEN
        RAISE EXCEPTION 'Saneamento abortado: nem todos os usuários preservados foram encontrados.';
    END IF;
END $$;

WITH deleted AS (
    DELETE FROM animal_sale
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'animal_sale', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM milk_sale
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'milk_sale', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM commercial_customer
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'commercial_customer', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM operational_expense
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'operational_expense', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM operational_audit_entry
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'operational_audit_entry', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM inventory_movement
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'inventory_movement', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM inventory_balance
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'inventory_balance', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM inventory_lot
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'inventory_lot', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM inventory_idempotency
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'inventory_idempotency', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM inventory_item
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'inventory_item', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM farm_milk_production
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'farm_milk_production', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM milk_production
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'milk_production', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM lactation
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'lactation', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM health_events
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'health_events', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM reproductive_event
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'reproductive_event', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM pregnancy
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'pregnancy', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM eventos
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'eventos', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM password_reset_token
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'password_reset_token', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM tb_farm_operator
    WHERE farm_id NOT IN (SELECT id FROM _preserved_farms)
       OR user_id NOT IN (SELECT id FROM _preserved_users)
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'tb_farm_operator', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM telefone
    WHERE goat_farm_id NOT IN (SELECT id FROM _preserved_farms)
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'telefone', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM cabras
    WHERE capril_id NOT IN (SELECT id FROM _preserved_farms)
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'cabras', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM capril
    WHERE id NOT IN (SELECT id FROM _preserved_farms)
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'capril', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM users
    WHERE id NOT IN (SELECT id FROM _preserved_users)
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'users', COUNT(*) FROM deleted;

WITH deleted AS (
    DELETE FROM endereco e
    WHERE NOT EXISTS (
        SELECT 1
        FROM capril c
        WHERE c.address_id = e.id
    )
    RETURNING 1
)
INSERT INTO _deleted_counts (table_name, deleted_count)
SELECT 'endereco', COUNT(*) FROM deleted;

SELECT table_name, deleted_count
FROM _deleted_counts
ORDER BY table_name;

COMMIT;
