ALTER TABLE milk_production ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE milk_production ADD COLUMN canceled_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE milk_production ADD COLUMN canceled_reason VARCHAR(500);

ALTER TABLE milk_production DROP CONSTRAINT uk_milk_production_daily_shift;

CREATE UNIQUE INDEX ux_milk_production_active_daily_shift
    ON milk_production(farm_id, goat_id, date, shift)
    WHERE status = 'ACTIVE';
