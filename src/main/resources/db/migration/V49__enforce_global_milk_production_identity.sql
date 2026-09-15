-- Enforce biological identity for active milk-production uniqueness.
-- Existing rows are never repaired or discarded: any conflict aborts the
-- migration before the legacy index is replaced.
DO $$
BEGIN
    IF EXISTS (
        SELECT goat_technical_id, date, shift
        FROM milk_production
        WHERE status = 'ACTIVE'
        GROUP BY goat_technical_id, date, shift
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'V49: duplicate ACTIVE milk production exists for GoatId/date/shift';
    END IF;
END
$$;

DROP INDEX IF EXISTS ux_milk_production_active_daily_shift;

CREATE UNIQUE INDEX ux_milk_production_active_goat_technical_daily_shift
    ON milk_production (goat_technical_id, date, shift)
    WHERE status = 'ACTIVE';
