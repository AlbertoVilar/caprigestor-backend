-- Registra rastreabilidade minima da saida controlada de animais do rebanho.
-- Escopo: somente evolucao incremental da entidade cabras.

ALTER TABLE cabras
    ADD COLUMN IF NOT EXISTS exit_type VARCHAR(30);

ALTER TABLE cabras
    ADD COLUMN IF NOT EXISTS exit_date DATE;

ALTER TABLE cabras
    ADD COLUMN IF NOT EXISTS exit_notes VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_cabras_exit_type ON cabras(exit_type);
CREATE INDEX IF NOT EXISTS idx_cabras_exit_date ON cabras(exit_date);
