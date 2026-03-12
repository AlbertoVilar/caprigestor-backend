-- Normaliza as cores legadas das cabras do seed do Capril Vilar.
-- Esta migration é idempotente e atua apenas sobre a fazenda seed.

UPDATE cabras
SET cor = U&'CHAMOIS\00C9E'
WHERE capril_id = (
    SELECT id
    FROM capril
    WHERE name = 'Capril Vilar'
)
  AND cor IS DISTINCT FROM U&'CHAMOIS\00C9E';