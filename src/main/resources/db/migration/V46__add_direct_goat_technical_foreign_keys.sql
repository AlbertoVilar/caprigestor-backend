-- W3: prepare the direct GoatId integrity boundary without changing behavior.
-- V40 composite farm-scoped constraints remain in place until a later wave.

ALTER TABLE pregnancy
    ADD CONSTRAINT fk_pregnancy_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE pregnancy
    VALIDATE CONSTRAINT fk_pregnancy_goat_technical_direct;

ALTER TABLE reproductive_event
    ADD CONSTRAINT fk_reproductive_event_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE reproductive_event
    VALIDATE CONSTRAINT fk_reproductive_event_goat_technical_direct;

ALTER TABLE health_events
    ADD CONSTRAINT fk_health_events_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE health_events
    VALIDATE CONSTRAINT fk_health_events_goat_technical_direct;

ALTER TABLE lactation
    ADD CONSTRAINT fk_lactation_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE lactation
    VALIDATE CONSTRAINT fk_lactation_goat_technical_direct;

ALTER TABLE milk_production
    ADD CONSTRAINT fk_milk_production_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE milk_production
    VALIDATE CONSTRAINT fk_milk_production_goat_technical_direct;

ALTER TABLE animal_sale
    ADD CONSTRAINT fk_animal_sale_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE animal_sale
    VALIDATE CONSTRAINT fk_animal_sale_goat_technical_direct;

ALTER TABLE operational_audit_entry
    ADD CONSTRAINT fk_operational_audit_entry_goat_technical_direct
        FOREIGN KEY (goat_technical_id) REFERENCES cabras (id)
        ON DELETE NO ACTION NOT VALID;
ALTER TABLE operational_audit_entry
    VALIDATE CONSTRAINT fk_operational_audit_entry_goat_technical_direct;
