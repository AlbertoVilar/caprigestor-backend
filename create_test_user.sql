-- Inserir usuário de teste
INSERT INTO users (name, email, cpf, password, created_at, updated_at) 
VALUES ('Leonardo Silva', 'leo@teste.com', '86228270036', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Inserir role ADMIN para o usuário
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id 
FROM users u, role r 
WHERE u.email = 'leo@teste.com' AND r.authority = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Verificar se foi criado
SELECT u.*, r.authority FROM users u 
LEFT JOIN tb_user_role ur ON u.id = ur.user_id 
LEFT JOIN role r ON ur.role_id = r.id 
WHERE u.email = 'leo@teste.com';