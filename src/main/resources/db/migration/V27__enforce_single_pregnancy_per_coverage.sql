-- Reproductive invariants hardening:
-- 1) Remove duplicated pregnancies bound to the same coverage_event_id.
-- 2) Enforce one pregnancy per (farm_id, coverage_event_id) when coverage_event_id is present.

WITH ranked AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY farm_id, coverage_event_id
            ORDER BY
                CASE
                    WHEN status = 'CLOSED' AND close_reason = 'BIRTH' THEN 0
                    WHEN status = 'ACTIVE' THEN 1
                    WHEN status = 'CLOSED' THEN 2
                    ELSE 3
                END,
                COALESCE(closed_at, confirm_date, breeding_date) DESC,
                id DESC
        ) AS rn
    FROM pregnancy
    WHERE coverage_event_id IS NOT NULL
)
DELETE FROM pregnancy
WHERE id IN (
    SELECT id
    FROM ranked
    WHERE rn > 1
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_pregnancy_single_coverage_per_goat
    ON pregnancy (farm_id, coverage_event_id)
    WHERE coverage_event_id IS NOT NULL;
