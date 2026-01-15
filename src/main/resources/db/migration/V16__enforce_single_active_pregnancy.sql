-- Enforce single ACTIVE pregnancy per (farm_id, goat_id)
-- This migration is designed to be safe for PostgreSQL and H2 in PostgreSQL mode.

-- PostgreSQL and H2 (MODE=PostgreSQL) support partial indexes with WHERE clause.
CREATE UNIQUE INDEX ux_pregnancy_single_active_per_goat
    ON pregnancy(farm_id, goat_id)
    WHERE status = 'ACTIVE';

-- Manual data-fix helper (DO NOT EXECUTE AUTOMATICALLY)
-- Use this query to list goats with duplicated ACTIVE pregnancies:
-- SELECT farm_id, goat_id, COUNT(*) AS active_count
-- FROM pregnancy
-- WHERE status = 'ACTIVE'
-- GROUP BY farm_id, goat_id
-- HAVING COUNT(*) > 1;
--
-- Suggested manual fix (keeps the most recent ACTIVE, closes others):
-- UPDATE pregnancy p
-- SET status = 'CLOSED',
--     closed_at = CURRENT_DATE,
--     close_reason = 'DATA_FIX_DUPLICATED_ACTIVE'
-- WHERE p.status = 'ACTIVE'
--   AND EXISTS (
--       SELECT 1
--       FROM pregnancy p2
--       WHERE p2.farm_id = p.farm_id
--         AND p2.goat_id = p.goat_id
--         AND p2.status = 'ACTIVE'
--         AND (
--           p2.breeding_date > p.breeding_date
--           OR (p2.breeding_date = p.breeding_date AND p2.id > p.id)
--         )
--   );
