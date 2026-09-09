-- Impede que registros pertencentes a uma fazenda apontem para dados de outra.
-- Os FKs simples anteriores permanecem: estes FKs compostos acrescentam o
-- contexto de farm_id sem alterar as regras de negócio ou de autorização.

ALTER TABLE cabras
    ADD CONSTRAINT uk_cabras_farm_registration
        UNIQUE (capril_id, num_registro);

ALTER TABLE commercial_customer
    ADD CONSTRAINT uk_commercial_customer_farm_id
        UNIQUE (farm_id, id);

ALTER TABLE pregnancy
    ADD CONSTRAINT uk_pregnancy_farm_id
        UNIQUE (farm_id, id);

ALTER TABLE reproductive_event
    ADD CONSTRAINT uk_reproductive_event_farm_id
        UNIQUE (farm_id, id);

ALTER TABLE health_events
    ADD CONSTRAINT uk_health_events_farm_id
        UNIQUE (farm_id, id);

ALTER TABLE inventory_item
    ADD CONSTRAINT uk_inventory_item_farm_id
        UNIQUE (farm_id, id);

ALTER TABLE inventory_lot
    ADD CONSTRAINT uk_inventory_lot_farm_item_id
        UNIQUE (farm_id, item_id, id);

ALTER TABLE lactation
    ADD CONSTRAINT uk_lactation_farm_goat_id
        UNIQUE (farm_id, goat_id, id);

ALTER TABLE pregnancy
    ADD CONSTRAINT fk_pregnancy_farm_goat
        FOREIGN KEY (farm_id, goat_id)
        REFERENCES cabras (capril_id, num_registro);

ALTER TABLE reproductive_event
    ADD CONSTRAINT fk_reproductive_event_farm_goat
        FOREIGN KEY (farm_id, goat_id)
        REFERENCES cabras (capril_id, num_registro),
    ADD CONSTRAINT fk_reproductive_event_farm_pregnancy
        FOREIGN KEY (farm_id, pregnancy_id)
        REFERENCES pregnancy (farm_id, id)
        ON DELETE SET NULL (pregnancy_id),
    ADD CONSTRAINT fk_reproductive_event_farm_related_event
        FOREIGN KEY (farm_id, related_event_id)
        REFERENCES reproductive_event (farm_id, id);

ALTER TABLE pregnancy
    ADD CONSTRAINT fk_pregnancy_farm_coverage_event
        FOREIGN KEY (farm_id, coverage_event_id)
        REFERENCES reproductive_event (farm_id, id);

ALTER TABLE health_events
    ADD CONSTRAINT fk_health_events_farm_goat
        FOREIGN KEY (farm_id, goat_id)
        REFERENCES cabras (capril_id, num_registro);

ALTER TABLE animal_sale
    ADD CONSTRAINT fk_animal_sale_farm_customer
        FOREIGN KEY (farm_id, customer_id)
        REFERENCES commercial_customer (farm_id, id),
    ADD CONSTRAINT fk_animal_sale_farm_goat
        FOREIGN KEY (farm_id, goat_registration_number)
        REFERENCES cabras (capril_id, num_registro);

ALTER TABLE milk_sale
    ADD CONSTRAINT fk_milk_sale_farm_customer
        FOREIGN KEY (farm_id, customer_id)
        REFERENCES commercial_customer (farm_id, id);

ALTER TABLE inventory_lot
    ADD CONSTRAINT fk_inventory_lot_farm_item
        FOREIGN KEY (farm_id, item_id)
        REFERENCES inventory_item (farm_id, id);

ALTER TABLE inventory_balance
    ADD CONSTRAINT fk_inventory_balance_farm_item
        FOREIGN KEY (farm_id, item_id)
        REFERENCES inventory_item (farm_id, id),
    ADD CONSTRAINT fk_inventory_balance_farm_item_lot
        FOREIGN KEY (farm_id, item_id, lot_id)
        REFERENCES inventory_lot (farm_id, item_id, id);

ALTER TABLE inventory_movement
    ADD CONSTRAINT fk_inventory_movement_farm_item
        FOREIGN KEY (farm_id, item_id)
        REFERENCES inventory_item (farm_id, id),
    ADD CONSTRAINT fk_inventory_movement_farm_item_lot
        FOREIGN KEY (farm_id, item_id, lot_id)
        REFERENCES inventory_lot (farm_id, item_id, id);

ALTER TABLE lactation
    ADD CONSTRAINT fk_lactation_farm_goat
        FOREIGN KEY (farm_id, goat_id)
        REFERENCES cabras (capril_id, num_registro);

ALTER TABLE milk_production
    ADD CONSTRAINT fk_milk_production_farm_goat_lactation
        FOREIGN KEY (farm_id, goat_id, lactation_id)
        REFERENCES lactation (farm_id, goat_id, id),
    ADD CONSTRAINT fk_milk_production_farm_withdrawal_event
        FOREIGN KEY (farm_id, milk_withdrawal_event_id)
        REFERENCES health_events (farm_id, id);

ALTER TABLE operational_audit_entry
    ADD CONSTRAINT fk_operational_audit_entry_farm_goat
        FOREIGN KEY (farm_id, goat_registration_number)
        REFERENCES cabras (capril_id, num_registro);
