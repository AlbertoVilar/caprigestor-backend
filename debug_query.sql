-- Verificar associação entre user_id=23 e owner_id=21
SELECT 
    u.id as user_id, 
    u.name as user_name,
    u.email as user_email, 
    o.id as owner_id, 
    o.nome as owner_name, 
    o.email as owner_email
FROM users u 
LEFT JOIN owners o ON u.email = o.email 
WHERE u.id = 23 OR o.id = 21;

-- Verificar se existe owner com id=21
SELECT * FROM owners WHERE id = 21;

-- Verificar se existe user com id=23
SELECT * FROM users WHERE id = 23;

-- Verificar todos os owners e users para entender a estrutura
SELECT u.id as user_id, u.email as user_email, o.id as owner_id, o.email as owner_email 
FROM users u 
FULL OUTER JOIN owners o ON u.email = o.email 
ORDER BY u.id, o.id;