ALTER TABLE milk_production
    ADD COLUMN recorded_during_milk_withdrawal BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE milk_production
    ADD COLUMN milk_withdrawal_event_id BIGINT;

ALTER TABLE milk_production
    ADD COLUMN milk_withdrawal_end_date DATE;

ALTER TABLE milk_production
    ADD COLUMN milk_withdrawal_source VARCHAR(120);
