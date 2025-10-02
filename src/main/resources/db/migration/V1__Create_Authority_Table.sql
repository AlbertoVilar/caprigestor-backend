-- Criação da tabela authority (permissões)
-- Esta tabela armazena as permissões disponíveis no sistema

CREATE TABLE authority (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT
);

-- Inserindo as permissões básicas do sistema
INSERT INTO authority (name, description) VALUES 
('READ_GOAT', 'Permissão para visualizar caprinos'),
('WRITE_GOAT', 'Permissão para criar e editar caprinos'),
('DELETE_GOAT', 'Permissão para excluir caprinos'),
('READ_FARM', 'Permissão para visualizar fazendas'),
('WRITE_FARM', 'Permissão para criar e editar fazendas'),
('DELETE_FARM', 'Permissão para excluir fazendas'),
('READ_USER', 'Permissão para visualizar usuários'),
('WRITE_USER', 'Permissão para criar e editar usuários'),
('DELETE_USER', 'Permissão para excluir usuários'),
('MANAGE_SYSTEM', 'Permissão para gerenciar o sistema');