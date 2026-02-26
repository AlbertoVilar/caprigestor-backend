CREATE TABLE IF NOT EXISTS inventory_item (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    name_normalized VARCHAR(140) NOT NULL,
    track_lot BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_inventory_item_farm_name_normalized UNIQUE (farm_id, name_normalized),
    CONSTRAINT fk_inventory_item_farm FOREIGN KEY (farm_id) REFERENCES capril(id)
);

CREATE TABLE IF NOT EXISTS inventory_lot (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS inventory_balance (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    lot_id BIGINT NULL,
    quantity NUMERIC(19, 3) NOT NULL,
    CONSTRAINT uk_inventory_balance_farm_item_lot UNIQUE (farm_id, item_id, lot_id),
    CONSTRAINT fk_inventory_balance_farm FOREIGN KEY (farm_id) REFERENCES capril(id),
    CONSTRAINT fk_inventory_balance_item FOREIGN KEY (item_id) REFERENCES inventory_item(id),
    CONSTRAINT fk_inventory_balance_lot FOREIGN KEY (lot_id) REFERENCES inventory_lot(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_inventory_balance_farm_item_null_lot
    ON inventory_balance(farm_id, item_id)
    WHERE lot_id IS NULL;

CREATE TABLE IF NOT EXISTS inventory_movement (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    adjust_direction VARCHAR(20),
    quantity NUMERIC(19, 3) NOT NULL,
    item_id BIGINT NOT NULL,
    lot_id BIGINT NULL,
    movement_date DATE NOT NULL,
    reason VARCHAR(500),
    resulting_balance NUMERIC(19, 3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_inventory_movement_quantity_positive CHECK (quantity > 0),
    CONSTRAINT fk_inventory_movement_farm FOREIGN KEY (farm_id) REFERENCES capril(id),
    CONSTRAINT fk_inventory_movement_item FOREIGN KEY (item_id) REFERENCES inventory_item(id),
    CONSTRAINT fk_inventory_movement_lot FOREIGN KEY (lot_id) REFERENCES inventory_lot(id)
);

CREATE TABLE IF NOT EXISTS inventory_idempotency (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    response_payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_inventory_idempotency_farm_key UNIQUE (farm_id, idempotency_key),
    CONSTRAINT fk_inventory_idempotency_farm FOREIGN KEY (farm_id) REFERENCES capril(id)
);

CREATE INDEX IF NOT EXISTS idx_inventory_item_farm
    ON inventory_item(farm_id);

CREATE INDEX IF NOT EXISTS idx_inventory_balance_farm_item
    ON inventory_balance(farm_id, item_id);

CREATE INDEX IF NOT EXISTS idx_inventory_movement_farm_item_date
    ON inventory_movement(farm_id, item_id, movement_date DESC);

CREATE INDEX IF NOT EXISTS idx_inventory_movement_farm_date
    ON inventory_movement(farm_id, movement_date DESC);
