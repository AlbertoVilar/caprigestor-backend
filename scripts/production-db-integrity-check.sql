-- Checagem de integridade da base promovivel apos restore
-- Uso: psql -U <usuario> -d <banco> -f scripts/production-db-integrity-check.sql

\echo === Fazendas preservadas ===
SELECT id, nome
FROM capril
ORDER BY id;

\echo === Usuarios preservados ===
SELECT id, name, email
FROM users
ORDER BY id;

\echo === Total de animais por fazenda ===
SELECT farm_id, COUNT(*) AS goats
FROM cabras
GROUP BY farm_id
ORDER BY farm_id;

\echo === Dados transacionais que devem estar zerados ===
SELECT 'lactation' AS tabela, COUNT(*) AS total FROM lactation
UNION ALL
SELECT 'milk_production', COUNT(*) FROM milk_production
UNION ALL
SELECT 'farm_milk_production', COUNT(*) FROM farm_milk_production
UNION ALL
SELECT 'pregnancy', COUNT(*) FROM pregnancy
UNION ALL
SELECT 'reproductive_event', COUNT(*) FROM reproductive_event
UNION ALL
SELECT 'health_events', COUNT(*) FROM health_events
UNION ALL
SELECT 'inventory_item', COUNT(*) FROM inventory_item
UNION ALL
SELECT 'inventory_movement', COUNT(*) FROM inventory_movement
UNION ALL
SELECT 'inventory_balance', COUNT(*) FROM inventory_balance
UNION ALL
SELECT 'milk_sale', COUNT(*) FROM milk_sale
UNION ALL
SELECT 'animal_sale', COUNT(*) FROM animal_sale
UNION ALL
SELECT 'operational_expense', COUNT(*) FROM operational_expense
UNION ALL
SELECT 'operational_audit_entry', COUNT(*) FROM operational_audit_entry
ORDER BY tabela;

\echo === Ownership preservado ===
SELECT f.id AS farm_id,
       f.nome AS farm_name,
       u.id AS user_id,
       u.name AS user_name,
       u.email
FROM capril f
JOIN users u ON u.id = f.user_id
ORDER BY f.id;

\echo === Gate esperado ===
\echo Esperado: apenas Capril Vilar e Capril Alto Paraiso.
\echo Esperado: apenas usuarios reais necessarios.
\echo Esperado: tabelas transacionais fake zeradas.
