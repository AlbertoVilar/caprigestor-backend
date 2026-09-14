-- W5: establish the canonical ownership baseline for legacy goats.
--
-- The legacy development database may be discarded before HML.  This
-- migration nevertheless remains a deterministic, fail-closed upgrade for
-- databases that already contain goats.  It does not infer creator data,
-- rewrite historical farm snapshots, mutate cabras.capril_id, or create
-- ownership transfers.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM goat_ownership_period) THEN
        RAISE EXCEPTION 'V48: goat_ownership_period must be empty before legacy backfill';
    END IF;

    IF EXISTS (SELECT 1 FROM ownership_transfer) THEN
        RAISE EXCEPTION 'V48: ownership_transfer must be empty before legacy backfill';
    END IF;

    IF EXISTS (SELECT 1 FROM cabras WHERE capril_id IS NULL) THEN
        RAISE EXCEPTION 'V48: legacy goat has no farm and cannot receive an ownership baseline';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE (exit_type IS NULL) <> (exit_date IS NULL)
    ) THEN
        RAISE EXCEPTION 'V48: legacy exit_type and exit_date must be supplied together';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE exit_type IS NOT NULL
          AND upper(btrim(exit_type)) NOT IN
              ('VENDA', 'MORTE', 'DESCARTE', 'DOACAO', 'TRANSFERENCIA')
    ) THEN
        RAISE EXCEPTION 'V48: legacy exit_type has no safe ownership mapping';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE exit_date IS NOT NULL
          AND exit_date < data_nascimento
    ) THEN
        RAISE EXCEPTION 'V48: legacy exit_date precedes data_nascimento';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE upper(btrim(status)) IN ('VENDIDO', 'FALECIDO')
          AND (exit_type IS NULL OR exit_date IS NULL)
    ) THEN
        RAISE EXCEPTION 'V48: terminal legacy status requires exit_type and exit_date';
    END IF;

    IF EXISTS (
        SELECT 1 FROM cabras
        WHERE (upper(btrim(status)) = 'VENDIDO' AND upper(btrim(exit_type)) <> 'VENDA')
           OR (upper(btrim(status)) = 'FALECIDO' AND upper(btrim(exit_type)) <> 'MORTE')
    ) THEN
        RAISE EXCEPTION 'V48: legacy status and exit_type contradict each other';
    END IF;
END
$$;

INSERT INTO goat_ownership_period
    (goat_id, farm_id, started_at, ended_at, entry_type, exit_type, source)
SELECT
    goat.id,
    goat.capril_id,
    goat.data_nascimento::timestamp AT TIME ZONE 'America/Sao_Paulo',
    CASE
        WHEN goat.exit_date IS NULL THEN NULL
        ELSE (goat.exit_date + 1)::timestamp AT TIME ZONE 'America/Sao_Paulo'
    END,
    'MANUAL_IMPORT',
    CASE upper(btrim(goat.exit_type))
        WHEN 'VENDA' THEN 'EXTERNAL_SALE'
        WHEN 'MORTE' THEN 'DEATH'
        WHEN 'DESCARTE' THEN 'RETIREMENT'
        WHEN 'DOACAO' THEN 'DONATION'
        WHEN 'TRANSFERENCIA' THEN 'TRANSFER_OUT'
        ELSE NULL
    END,
    'LEGACY_BACKFILL'
FROM cabras goat;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM cabras goat
        LEFT JOIN goat_ownership_period period ON period.goat_id = goat.id
        GROUP BY goat.id
        HAVING COUNT(period.id) <> 1
    ) THEN
        RAISE EXCEPTION 'V48: every legacy goat must have exactly one baseline ownership period';
    END IF;

    IF EXISTS (
        SELECT goat_id FROM goat_ownership_period
        WHERE ended_at IS NULL
        GROUP BY goat_id HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'V48: more than one open ownership period exists for a GoatId';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM cabras goat
        JOIN goat_ownership_period period ON period.goat_id = goat.id
        WHERE (goat.exit_date IS NULL AND period.ended_at IS NOT NULL)
           OR (goat.exit_date IS NOT NULL AND period.ended_at IS NULL)
           OR period.farm_id <> goat.capril_id
           OR period.entry_type <> 'MANUAL_IMPORT'
           OR period.source <> 'LEGACY_BACKFILL'
    ) THEN
        RAISE EXCEPTION 'V48: legacy ownership baseline invariants failed';
    END IF;

    IF EXISTS (SELECT 1 FROM ownership_transfer) THEN
        RAISE EXCEPTION 'V48: legacy backfill must not create ownership transfers';
    END IF;

END
$$;
