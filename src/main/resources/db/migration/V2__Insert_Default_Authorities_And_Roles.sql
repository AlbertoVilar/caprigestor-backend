-- Inserir authorities padrão
INSERT INTO authority (name, description) VALUES 
('READ_GOATS', 'Permissão para visualizar caprinos'),
('READ_GENEALOGY', 'Permissão para visualizar genealogia'),
('MANAGE_OWN_FARM', 'Permissão para gerenciar própria fazenda'),
('CREATE_GOATS', 'Permissão para criar caprinos'),
('UPDATE_GOATS', 'Permissão para atualizar caprinos'),
('DELETE_GOATS', 'Permissão para deletar caprinos'),
('MANAGE_USERS', 'Permissão para gerenciar usuários'),
('MANAGE_ALL_FARMS', 'Permissão para gerenciar todas as fazendas'),
('SYSTEM_ADMIN', 'Permissão de administrador do sistema');

-- Inserir roles padrão
INSERT INTO role (authority, description) VALUES 
('ROLE_PUBLIC', 'Usuário público - pode visualizar caprinos e genealogia'),
('ROLE_FARM_OWNER', 'Dono de fazenda - pode gerenciar sua própria fazenda'),
('ROLE_ADMIN', 'Administrador do sistema - acesso total');

-- Associar authorities às roles
-- ROLE_PUBLIC: apenas leitura
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_PUBLIC' 
AND a.name IN ('READ_GOATS', 'READ_GENEALOGY');

-- ROLE_FARM_OWNER: leitura + gerenciamento da própria fazenda
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_FARM_OWNER' 
AND a.name IN ('READ_GOATS', 'READ_GENEALOGY', 'MANAGE_OWN_FARM', 'CREATE_GOATS', 'UPDATE_GOATS', 'DELETE_GOATS');

-- ROLE_ADMIN: todas as permissões
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_ADMIN';