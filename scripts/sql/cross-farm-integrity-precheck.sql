-- CAPRIGESTOR — precheck somente leitura para invariantes entre fazendas.
--
-- Execute com uma conta de leitura no banco-alvo antes de adicionar ou validar
-- constraints compostas. O resultado contém somente contagens; nunca inclua
-- identificadores de animais, clientes ou fazendas em evidências operacionais.
--
-- Resultado esperado para iniciar uma migration: todas as contagens em zero.

SELECT 'pregnancy_missing_goat' AS check_name, count(*) AS violation_count
FROM pregnancy p
LEFT JOIN cabras g ON g.num_registro = p.goat_id
WHERE g.num_registro IS NULL

UNION ALL

SELECT 'pregnancy_goat_farm_mismatch', count(*)
FROM pregnancy p
JOIN cabras g ON g.num_registro = p.goat_id
WHERE p.farm_id <> g.capril_id

UNION ALL

SELECT 'reproductive_event_missing_goat', count(*)
FROM reproductive_event e
LEFT JOIN cabras g ON g.num_registro = e.goat_id
WHERE g.num_registro IS NULL

UNION ALL

SELECT 'reproductive_event_goat_farm_mismatch', count(*)
FROM reproductive_event e
JOIN cabras g ON g.num_registro = e.goat_id
WHERE e.farm_id <> g.capril_id

UNION ALL

SELECT 'reproductive_event_missing_pregnancy', count(*)
FROM reproductive_event e
LEFT JOIN pregnancy p ON p.id = e.pregnancy_id
WHERE e.pregnancy_id IS NOT NULL
  AND p.id IS NULL

UNION ALL

SELECT 'reproductive_event_pregnancy_farm_mismatch', count(*)
FROM reproductive_event e
JOIN pregnancy p ON p.id = e.pregnancy_id
WHERE e.farm_id <> p.farm_id

UNION ALL

SELECT 'health_event_goat_farm_mismatch', count(*)
FROM health_events h
JOIN cabras g ON g.num_registro = h.goat_id
WHERE h.farm_id <> g.capril_id

UNION ALL

SELECT 'animal_sale_customer_farm_mismatch', count(*)
FROM animal_sale s
JOIN commercial_customer c ON c.id = s.customer_id
WHERE s.farm_id <> c.farm_id

UNION ALL

SELECT 'animal_sale_goat_farm_mismatch', count(*)
FROM animal_sale s
JOIN cabras g ON g.num_registro = s.goat_registration_number
WHERE s.farm_id <> g.capril_id

UNION ALL

SELECT 'milk_sale_customer_farm_mismatch', count(*)
FROM milk_sale s
JOIN commercial_customer c ON c.id = s.customer_id
WHERE s.farm_id <> c.farm_id

UNION ALL

SELECT 'inventory_balance_item_farm_mismatch', count(*)
FROM inventory_balance b
JOIN inventory_item i ON i.id = b.item_id
WHERE b.farm_id <> i.farm_id

UNION ALL

SELECT 'inventory_movement_item_farm_mismatch', count(*)
FROM inventory_movement m
JOIN inventory_item i ON i.id = m.item_id
WHERE m.farm_id <> i.farm_id

UNION ALL

SELECT 'inventory_balance_lot_context_mismatch', count(*)
FROM inventory_balance b
JOIN inventory_lot l ON l.id = b.lot_id
WHERE b.lot_id IS NOT NULL
  AND (b.farm_id <> l.farm_id OR b.item_id <> l.item_id)

UNION ALL

SELECT 'inventory_movement_lot_context_mismatch', count(*)
FROM inventory_movement m
JOIN inventory_lot l ON l.id = m.lot_id
WHERE m.lot_id IS NOT NULL
  AND (m.farm_id <> l.farm_id OR m.item_id <> l.item_id)

ORDER BY check_name;
