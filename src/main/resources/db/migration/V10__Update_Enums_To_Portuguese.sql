-- Atualiza constraints e dados para enums em português

-- 1) Remover constraints antigas de sexo e status (valores em inglês)
ALTER TABLE cabras DROP CONSTRAINT IF EXISTS cabras_sexo_check;
ALTER TABLE cabras DROP CONSTRAINT IF EXISTS cabras_status_check;

-- 2) Atualizar dados existentes para os novos valores em português
-- Sexo
UPDATE cabras SET sexo = 'MACHO' WHERE sexo = 'MALE';
UPDATE cabras SET sexo = 'FEMEA' WHERE sexo = 'FEMALE';

-- Status
UPDATE cabras SET status = 'ATIVO'    WHERE status = 'ACTIVE';
UPDATE cabras SET status = 'INATIVO'  WHERE status = 'INACTIVE';
UPDATE cabras SET status = 'FALECIDO' WHERE status = 'DECEASED';
UPDATE cabras SET status = 'VENDIDO'  WHERE status = 'SOLD';

-- Raça (enum GoatBreed): padronizar ALPINE -> ALPINA
UPDATE cabras SET raca = 'ALPINA' WHERE raca = 'ALPINE';

-- 3) Adicionar novas constraints com valores em português
ALTER TABLE cabras ADD CONSTRAINT cabras_sexo_check   CHECK (sexo   IN ('MACHO', 'FEMEA'));
ALTER TABLE cabras ADD CONSTRAINT cabras_status_check CHECK (status IN ('ATIVO', 'INATIVO', 'VENDIDO', 'FALECIDO'));