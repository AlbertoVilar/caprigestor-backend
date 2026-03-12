ALTER TABLE inventory_lot
    ADD COLUMN IF NOT EXISTS farm_id BIGINT,
    ADD COLUMN IF NOT EXISTS item_id BIGINT,
    ADD COLUMN IF NOT EXISTS code VARCHAR(80),
    ADD COLUMN IF NOT EXISTS code_normalized VARCHAR(100),
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS expiration_date DATE,
    ADD COLUMN IF NOT EXISTS active BOOLEAN;

WITH lot_context AS (
    SELECT lot_id, farm_id, item_id
    FROM inventory_movement
    WHERE lot_id IS NOT NULL

    UNION ALL

    SELECT lot_id, farm_id, item_id
    FROM inventory_balance
    WHERE lot_id IS NOT NULL
),
resolved_context AS (
    SELECT
        lot_id,
        MIN(farm_id) AS farm_id,
        MIN(item_id) AS item_id,
        COUNT(DISTINCT farm_id) AS farm_count,
        COUNT(DISTINCT item_id) AS item_count
    FROM lot_context
    GROUP BY lot_id
)
UPDATE inventory_lot lot
SET
    farm_id = resolved.farm_id,
    item_id = resolved.item_id,
    code = COALESCE(lot.code, 'LEGACY-' || lot.id),
    code_normalized = COALESCE(lot.code_normalized, LOWER('legacy-' || lot.id)),
    active = COALESCE(lot.active, TRUE)
FROM resolved_context resolved
WHERE lot.id = resolved.lot_id
  AND resolved.farm_count = 1
  AND resolved.item_count = 1;

UPDATE inventory_lot
SET
    code = COALESCE(code, 'LEGACY-' || id),
    code_normalized = COALESCE(code_normalized, LOWER('legacy-' || id)),
    active = COALESCE(active, TRUE);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM inventory_lot
        WHERE farm_id IS NULL
           OR item_id IS NULL
    ) THEN
        RAISE EXCEPTION 'Não foi possível inferir farm_id e item_id para todos os lotes legados de inventory_lot.';
    END IF;
END $$;

ALTER TABLE inventory_lot
    ALTER COLUMN farm_id SET NOT NULL,
    ALTER COLUMN item_id SET NOT NULL,
    ALTER COLUMN code SET NOT NULL,
    ALTER COLUMN code_normalized SET NOT NULL,
    ALTER COLUMN active SET NOT NULL;

ALTER TABLE inventory_lot
    ADD CONSTRAINT fk_inventory_lot_farm
        FOREIGN KEY (farm_id) REFERENCES capril(id);

ALTER TABLE inventory_lot
    ADD CONSTRAINT fk_inventory_lot_item
        FOREIGN KEY (item_id) REFERENCES inventory_item(id);

ALTER TABLE inventory_lot
    ADD CONSTRAINT uk_inventory_lot_farm_item_code_normalized
        UNIQUE (farm_id, item_id, code_normalized);

CREATE INDEX IF NOT EXISTS idx_inventory_lot_farm
    ON inventory_lot(farm_id);

CREATE INDEX IF NOT EXISTS idx_inventory_lot_farm_item
    ON inventory_lot(farm_id, item_id);

CREATE INDEX IF NOT EXISTS idx_inventory_lot_farm_item_active
    ON inventory_lot(farm_id, item_id, active);
