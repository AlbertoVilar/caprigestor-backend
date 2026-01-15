-- Diagnóstico: localizar pregnancies duplicadas com status ACTIVE por (farm_id, goat_id)
SELECT farm_id, goat_id, COUNT(*) AS active_count
FROM pregnancy
WHERE status = 'ACTIVE'
GROUP BY farm_id, goat_id
HAVING COUNT(*) > 1;

-- Fix seguro: mantém a ACTIVE mais recente e fecha as demais
UPDATE pregnancy p 
SET status = 'CLOSED',
    closed_at = CURRENT_DATE,
    close_reason = 'DATA_FIX_DUPLICATED_ACTIVE'
WHERE p.status = 'ACTIVE'
  AND EXISTS (
      SELECT 1 
      FROM pregnancy p2 
      WHERE p2.farm_id = p.farm_id 
        AND p2.goat_id = p.goat_id 
        AND p2.status = 'ACTIVE' 
        AND ( 
          p2.breeding_date > p.breeding_date 
          OR (p2.breeding_date = p.breeding_date AND p2.id > p.id) 
        ) 
  );

