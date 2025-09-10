-- Roles
INSERT INTO role (authority) VALUES ('ROLE_ADMIN');
INSERT INTO role (authority) VALUES ('ROLE_OPERATOR');

-- Usuários
INSERT INTO users (name, cpf, email, password) VALUES ('Alberto Vilar', '05202259450', 'albertovilar1@gmail.com', '$2a$10$iUULLtjqP/VsKkgI492POuxOGJMit9T/OodlmVFIEtFjabK2ZAqsG');
INSERT INTO users (name, cpf, email, password) VALUES ('Carlos Medeiros', '12345678900', 'carlosmedeiros@email.com', '$2a$10$N.zmdr9k7uOCQb0VpVKS.OGCmGOm7pQWvRzAohqRdpp9g5rHQKw9O');

-- Associar roles aos usuários
INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 1); -- Alberto como ADMIN
INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 2); -- Alberto como OPERATOR
INSERT INTO tb_user_role (user_id, role_id) VALUES (2, 2); -- Carlos como OPERATOR

-- Endereços
INSERT INTO endereco (rua, bairro, cidade, estado, codigo_postal, pais) VALUES ('Sítio São Felix', 'Zona Rural', 'Santo Andre', 'Paraíba', '58670-000', 'Brasil');

INSERT INTO endereco (rua, bairro, cidade, estado, codigo_postal, pais) VALUES ('Sítio Jacaré', 'Zona Rural', 'Juazeirinho', 'Paraíba', '58660-000', 'Brasil');

-- Fazendas
INSERT INTO capril (nome, TOD, user_id, address_id, criado, atualizado) VALUES ('Capril Vilar', '16432', 1, 1, NOW(), NOW());

INSERT INTO capril (nome, TOD, user_id, address_id, criado, atualizado) VALUES ('Capril Medeiros', '99887', 2, 2, NOW(), NOW());

-- Telefones
INSERT INTO telefone (ddd, numero, goat_farm_id) VALUES ('21', '98988-2934', 1);
INSERT INTO telefone (ddd, numero, goat_farm_id) VALUES ('83', '99876-1234', 2);
INSERT INTO telefone (ddd, numero, goat_farm_id) VALUES ('11', '99876-2548', 2);

-- Inserir dados na tabela 'cabras' (BISAVÔS MATERNOS)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1403110395', 'NATAL DO JACOMÉ', 'MALE', 'SAANEN', 'Indefinida', '2010-01-01', 'ATIVO', 1, 1, NULL, NULL);
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1650112018', '12018 CAPRIMEL', 'FEMALE', 'SAANEN', 'Indefinida', '2012-01-01', 'ATIVO', 1, 1, NULL, NULL);
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('2104406006', 'HERE DO ANGICANO', 'MALE', 'SAANEN', 'Indefinida', '2006-01-01', 'ATIVO', 1, 1, NULL, NULL);
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('2114510040', 'TOPÁZIO DO CRS', 'FEMALE', 'SAANEN', 'Indefinida', '2010-01-01', 'ATIVO', 1, 1, NULL, NULL);

-- Inserir dados na tabela 'cabras' (BISAVÔS PATERNOS - LADO DO AVÔ)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1422911451', 'BALU DA CAPRIVAMA', 'MALE', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO', 1, 1, NULL, NULL);
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1422911408', 'COROA DA CAPRIVAMA', 'FEMALE', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO', 1, 1, NULL, NULL);

-- Inserir dados na tabela 'cabras' (BISAVÔS PATERNOS - LADO DA AVÓ)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1412811133', 'SHERIFF SAVANA', 'MALE', 'SAANEN', 'Indefinida', '2011-01-01', 'ATIVO', 1, 1, NULL, NULL);
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1418513119', 'JUCELISE DO JALILI', 'FEMALE', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO', 1, 1, NULL, NULL);

-- Inserir dados na tabela 'cabras' (AVÓS PATERNOS)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1422915618', 'PETRÓLEO CAPRIVAMAR', 'MALE', 'SAANEN', 'Indefinida', '2015-01-01', 'ATIVO', 1, 1, '1422911451', '1422911408');
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1422913470', 'BÉLGICA DA CAPRIVAMAR', 'FEMALE', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO', 1, 1, '1412811133', '1418513119');

-- Inserir dados na tabela 'cabras' (AVÓS MATERNOS)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1650113018', 'JOSA CAPRIMEL', 'MALE', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO', 1, 1, '1403110395', '1650112018');
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('2114513061', 'PANTALONA DO CRS', 'FEMALE', 'SAANEN', 'Indefinida', '2013-01-01', 'ATIVO', 1, 1, '2104406006', '2114510040');

-- Inserir dados na tabela 'cabras' (PAIS)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1635717065', 'C.V.C SIGNOS PETROLEO', 'MALE', 'SAANEN', 'Indefinida', '2017-01-01', 'ATIVO', 1, 1, '1422915618', '1422913470');
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('2114517012', 'NAIDE DO CRS', 'FEMALE', 'SAANEN', 'Indefinida', '2017-01-01', 'ATIVO', 1, 1, '1650113018', '2114513061');

-- Inserir dados na tabela 'cabras' (FILHOS)
INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id, tod, toe, categoria) VALUES ('1643218012', 'XEQUE V DO CAPRIL VILAR', 'MALE', 'SAANEN', 'CHAMOISÉE', '2018-06-27', 'ATIVO', 1, 1, '1635717065', '2114517012', '16432', '18012', 'PO');

INSERT INTO cabras (num_registro, nome, sexo, raca, cor, nascimento, status, capril_id, user_id, pai_id, mae_id) VALUES ('1643218013', 'IRMÃO DO XEQUE', 'MALE', 'SAANEN', 'CHAMOISÉE', '2020-01-01', 'ATIVO', 1, 1, '1635717065', '2114517012');

INSERT INTO eventos (goat_id, tipo_evento, data, descricao, local, veterinario, resultado) VALUES ('2114517012', 'PARTO', '2025-05-08', 'Nascimento de dois cabritos machos sem complicações.', 'Capril Vilar', 'Dr. João Silva', 'Ambos os cabritos nasceram saudáveis.');

INSERT INTO eventos (goat_id, tipo_evento, data, descricao, local, veterinario, resultado) VALUES ('2114517012', 'SAUDE', '2025-05-05', 'Apresentou sintomas de mastite clínica', 'Capril Vilar', 'Dr. Carlos Mendes', 'Iniciado tratamento com antibiótico específico. Isolamento recomendado por 5 dias.');
