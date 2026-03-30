ALTER TABLE farm_milk_production
    ADD CONSTRAINT ck_farm_milk_production_total_non_negative
        CHECK (total_produced >= 0),
    ADD CONSTRAINT ck_farm_milk_production_withdrawal_non_negative
        CHECK (withdrawal_produced >= 0),
    ADD CONSTRAINT ck_farm_milk_production_marketable_non_negative
        CHECK (marketable_produced >= 0),
    ADD CONSTRAINT ck_farm_milk_production_volume_balance
        CHECK (total_produced = withdrawal_produced + marketable_produced);
