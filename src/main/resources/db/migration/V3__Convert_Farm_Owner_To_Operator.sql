-- Migração para converter ROLE_FARM_OWNER para ROLE_OPERATOR e remover ROLE_FARM_OWNER

-- 1. Atualizar associações de usuários de ROLE_FARM_OWNER para ROLE_OPERATOR
UPDATE tb_user_role 
SET role_id = (SELECT id FROM role WHERE authority = 'ROLE_OPERATOR')
WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_FARM_OWNER');

-- 2. Remover associações de authorities da ROLE_FARM_OWNER
DELETE FROM tb_role_authority 
WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_FARM_OWNER');

-- 3. Remover a ROLE_FARM_OWNER
DELETE FROM role WHERE authority = 'ROLE_FARM_OWNER';

-- 4. Garantir que ROLE_OPERATOR tenha as authorities necessárias para donos de fazenda
-- Associar authorities à ROLE_OPERATOR (caso não existam)
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_OPERATOR' 
AND a.name IN ('READ_GOATS', 'READ_GENEALOGY', 'MANAGE_OWN_FARM', 'CREATE_GOATS', 'UPDATE_GOATS', 'DELETE_GOATS')
AND NOT EXISTS (
    SELECT 1 FROM tb_role_authority tra 
    WHERE tra.role_id = r.id AND tra.authority_id = a.id
);