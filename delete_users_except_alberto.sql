-- Script para deletar todos os usuários exceto Alberto Vilar
-- Primeiro, remover as associações de roles dos usuários que serão deletados
DELETE FROM tb_user_role 
WHERE user_id IN (
    SELECT id FROM users 
    WHERE email != 'albertovilar1@gmail.com'
);

-- Depois, deletar os usuários (exceto Alberto Vilar)
DELETE FROM users 
WHERE email != 'albertovilar1@gmail.com';

-- Verificar quantos usuários restaram
SELECT COUNT(*) as total_usuarios FROM users;

-- Mostrar o usuário que restou
SELECT id, name, email FROM users;