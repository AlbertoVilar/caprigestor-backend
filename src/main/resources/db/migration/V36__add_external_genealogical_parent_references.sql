ALTER TABLE cabras
    ADD COLUMN pai_rg_externo VARCHAR(20),
    ADD COLUMN mae_rg_externo VARCHAR(20);

ALTER TABLE cabras
    ADD CONSTRAINT cabras_pai_referencia_genealogica_check
        CHECK (pai_num_registro IS NULL OR pai_rg_externo IS NULL),
    ADD CONSTRAINT cabras_mae_referencia_genealogica_check
        CHECK (mae_num_registro IS NULL OR mae_rg_externo IS NULL);
