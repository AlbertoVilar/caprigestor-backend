-- Legacy external sales remain valid with a NULL target. W13 ownership sales
-- explicitly identify the receiving farm; the ownership ledger stays canonical.
ALTER TABLE animal_sale
    ADD COLUMN target_farm_id BIGINT;

ALTER TABLE animal_sale
    ADD CONSTRAINT fk_animal_sale_target_farm
        FOREIGN KEY (target_farm_id) REFERENCES capril (id) ON DELETE RESTRICT;

CREATE INDEX idx_animal_sale_target_farm_date
    ON animal_sale (target_farm_id, sale_date DESC, id DESC)
    WHERE target_farm_id IS NOT NULL;

CREATE UNIQUE INDEX ux_ownership_transfer_sale
    ON ownership_transfer (sale_id)
    WHERE sale_id IS NOT NULL;
