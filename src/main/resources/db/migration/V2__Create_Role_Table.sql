-- Criação da tabela role (papéis/funções)
-- Esta tabela armazena os papéis disponíveis no sistema

CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    authority VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
);

-- Criação da tabela de relacionamento entre roles e authorities
CREATE TABLE tb_role_authority (
    role_id BIGINT NOT NULL,
    authority_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, authority_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (authority_id) REFERENCES authority(id) ON DELETE CASCADE
);

-- Inserindo os papéis básicos do sistema
INSERT INTO role (authority, description) VALUES 
('ROLE_ADMIN', 'Administrador do sistema com acesso total'),
('ROLE_FARM_OWNER', 'Proprietário de fazenda com acesso aos seus dados'),
('ROLE_OPERATOR', 'Operador com acesso limitado para operações básicas'),
('ROLE_VIEWER', 'Visualizador com acesso apenas de leitura');

-- Associando permissões aos papéis
-- ADMIN tem todas as permissões
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_ADMIN';

-- FARM_OWNER tem permissões de leitura e escrita
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_FARM_OWNER' 
AND a.name IN ('READ_GOAT', 'WRITE_GOAT', 'READ_FARM', 'WRITE_FARM', 'READ_USER');

-- OPERATOR tem permissões básicas de operação
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_OPERATOR' 
AND a.name IN ('READ_GOAT', 'WRITE_GOAT', 'READ_FARM');

-- VIEWER tem apenas permissões de leitura
INSERT INTO tb_role_authority (role_id, authority_id) 
SELECT r.id, a.id 
FROM role r, authority a 
WHERE r.authority = 'ROLE_VIEWER' 
AND a.name IN ('READ_GOAT', 'READ_FARM', 'READ_USER');