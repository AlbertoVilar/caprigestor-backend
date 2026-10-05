-- Enforce the business invariant that a Goat may have only one ACTIVE
-- lactation at a time. Historical DRY lactations remain unrestricted.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM lactation
        WHERE status = 'ACTIVE'
        GROUP BY farm_id, goat_technical_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'V44: existem multiplas lactacoes ativas para a mesma cabra/fazenda';
    END IF;
END
$$;

CREATE UNIQUE INDEX ux_lactation_single_active_per_goat_technical
    ON lactation (farm_id, goat_technical_id)
    WHERE status = 'ACTIVE';
