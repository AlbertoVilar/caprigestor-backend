-- Seed de dados para Capril Vilar compatível com schema atual
-- Este script faz UPSERTs para evitar conflitos em ambientes já parcialmente populados

-- Roles básicas (evita conflito por UNIQUE em authority)
INSERT INTO role (authority) VALUES ('ROLE_ADMIN') ON CONFLICT (authority) DO NOTHING;
INSERT INTO role (authority) VALUES ('ROLE_OPERATOR') ON CONFLICT (authority) DO NOTHING;

-- Usuário proprietário (evita conflito por UNIQUE em email)
INSERT INTO users (name, cpf, email, password)
VALUES ('Alberto Vilar', '05202259450', 'albertovilar1@gmail.com', '$2a$10$iUULLtjqP/VsKkgI492POuxOGJMit9T/OodlmVFIEtFjabK2ZAqsG')
ON CONFLICT (email) DO NOTHING;

-- Associações de roles ao usuário (evita duplicidades pela PK composta)
INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id FROM users u, role r
WHERE u.email = 'albertovilar1@gmail.com' AND r.authority = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO tb_user_role (user_id, role_id)
SELECT u.id, r.id FROM users u, role r
WHERE u.email = 'albertovilar1@gmail.com' AND r.authority = 'ROLE_OPERATOR'
ON CONFLICT DO NOTHING;

-- Endereço principal do Capril Vilar
INSERT INTO endereco (rua, bairro, cidade, estado, cep, pais)
VALUES ('Sítio São Felix', 'Zona Rural', 'Santo Andre', 'Paraíba', '58670-000', 'Brasil');

-- Fazenda Capril Vilar (usa colunas name/tod/user_id/address_id do schema atual)
INSERT INTO capril (name, tod, user_id, address_id, created_at, updated_at)
VALUES (
  'Capril Vilar',
  '16432',
  (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'),
  (SELECT id FROM endereco WHERE rua = 'Sítio São Felix' AND cep = '58670-000' ORDER BY id DESC LIMIT 1),
  NOW(), NOW()
)
ON CONFLICT (name) DO UPDATE SET
  tod = EXCLUDED.tod,
  user_id = EXCLUDED.user_id,
  address_id = EXCLUDED.address_id,
  updated_at = NOW();

-- Telefones do Capril Vilar
INSERT INTO telefone (ddd, numero, goat_farm_id)
VALUES ('21', '98988-2934', (SELECT id FROM capril WHERE name = 'Capril Vilar'))
ON CONFLICT (ddd, numero) DO NOTHING;

-- Inserções de cabras (ordem respeita relacionamentos de pais/maes)
-- Todas vinculadas ao usuário Alberto e ao Capril Vilar

-- BISAVÔS MATERNOS
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1403110395', 'NATAL DO JACOMÉ', 'MACHO', 'SAANEN', 'Indefinida', '2010-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1650112018', '12018 CAPRIMEL', 'FEMEA', 'SAANEN', 'Indefinida', '2012-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('2104406006', 'HERE DO ANGICANO', 'MACHO', 'SAANEN', 'Indefinida', '2006-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('2114510040', 'TOPÁZIO DO CRS', 'FEMEA', 'SAANEN', 'Indefinida', '2010-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

-- BISAVÔS PATERNOS - LADO DO AVÔ
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1422911451', 'BALU DA CAPRIVAMA', 'MACHO', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1422911408', 'COROA DA CAPRIVAMA', 'FEMEA', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

-- BISAVÔS PATERNOS - LADO DA AVÓ
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1412811133', 'SHERIFF SAVANA', 'MACHO', 'SAANEN', 'Indefinida', '2011-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1418513119', 'JUCELISE DO JALILI', 'FEMEA', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), NULL, NULL)
ON CONFLICT (num_registro) DO NOTHING;

-- AVÓS PATERNOS
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1422915618', 'PETRÓLEO CAPRIVAMAR', 'MACHO', 'SAANEN', 'Indefinida', '2015-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1422911451', '1422911408')
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1422913470', 'BÉLGICA DA CAPRIVAMAR', 'FEMEA', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1412811133', '1418513119')
ON CONFLICT (num_registro) DO NOTHING;

-- AVÓS MATERNOS
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1650113018', 'JOSA CAPRIMEL', 'MACHO', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1403110395', '1650112018')
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('2114513061', 'PANTALONA DO CRS', 'FEMEA', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '2104406006', '2114510040')
ON CONFLICT (num_registro) DO NOTHING;

-- PAIS
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1635717065', 'C.V.C SIGNOS PETROLEO', 'MACHO', 'SAANEN', 'Indefinida', '2017-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1422915618', '1422913470')
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('2114517012', 'NAIDE DO CRS', 'FEMEA', 'SAANEN', 'Indefinida', '2017-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1650113018', '2114513061')
ON CONFLICT (num_registro) DO NOTHING;

-- FILHOS
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro, tod, toe, categoria)
VALUES ('1643218012', 'XEQUE V DO CAPRIL VILAR', 'MACHO', 'SAANEN', 'CHAMOISÉE', '2018-06-27', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1635717065', '2114517012', '16432', '18012', 'PO')
ON CONFLICT (num_registro) DO NOTHING;

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, data_nascimento, status, capril_id, usuario_id, pai_num_registro, mae_num_registro)
VALUES ('1643218013', 'IRMÃO DO XEQUE', 'MACHO', 'SAANEN', 'CHAMOISÉE', '2020-01-01', 'ATIVO',
        (SELECT id FROM capril WHERE name = 'Capril Vilar'), (SELECT id FROM users WHERE email = 'albertovilar1@gmail.com'), '1635717065', '2114517012')
ON CONFLICT (num_registro) DO NOTHING;

-- Eventos
INSERT INTO eventos (goat_registration_number, tipo_evento, data, descricao, local, veterinario, resultado)
VALUES ('2114517012', 'PARTO', '2025-05-08', 'Nascimento de dois cabritos machos sem complicações.', 'Capril Vilar', 'Dr. João Silva', 'Ambos os cabritos nasceram saudáveis.');

INSERT INTO eventos (goat_registration_number, tipo_evento, data, descricao, local, veterinario, resultado)
VALUES ('2114517012', 'SAUDE', '2025-05-05', 'Apresentou sintomas de mastite clínica', 'Capril Vilar', 'Dr. Carlos Mendes', 'Iniciado tratamento com antibiótico específico. Isolamento recomendado por 5 dias.');
-- Active lactation seed for goatId 2114513061
INSERT INTO lactation (farm_id, goat_id, status, start_date, created_at, updated_at)
SELECT
  (SELECT id FROM capril WHERE name = 'Capril Vilar'),
  '2114513061',
  'ACTIVE',
  '2026-01-01',
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM lactation
  WHERE farm_id = (SELECT id FROM capril WHERE name = 'Capril Vilar')
    AND goat_id = '2114513061'
    AND status = 'ACTIVE'
);

