CREATE TABLE farm_milk_production (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    production_date DATE NOT NULL,
    total_produced NUMERIC(12, 2) NOT NULL,
    withdrawal_produced NUMERIC(12, 2) NOT NULL,
    marketable_produced NUMERIC(12, 2) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_farm_milk_production_farm_date UNIQUE (farm_id, production_date),
    CONSTRAINT fk_farm_milk_production_farm
        FOREIGN KEY (farm_id)
        REFERENCES capril (id)
);

CREATE INDEX idx_farm_milk_production_farm_date
    ON farm_milk_production (farm_id, production_date);
