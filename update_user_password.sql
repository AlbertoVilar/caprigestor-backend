-- Atualizar senha do usuário albertovilar1@gmail.com para 'password'
-- Hash BCrypt para 'password': $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
WHERE email = 'albertovilar1@gmail.com';

-- Verificar se a atualização foi bem-sucedida
SELECT email, password FROM users WHERE email = 'albertovilar1@gmail.com';