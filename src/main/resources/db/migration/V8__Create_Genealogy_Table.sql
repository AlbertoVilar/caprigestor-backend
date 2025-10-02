-- Criação da tabela genealogia (informações genealógicas)
-- Esta tabela armazena informações detalhadas de genealogia dos caprinos

CREATE TABLE genealogia (
    id BIGSERIAL PRIMARY KEY,
    
    -- Dados do animal
    nome_animal VARCHAR(100),
    registro_animal VARCHAR(20),
    criador VARCHAR(255),
    proprietario VARCHAR(255),
    raca VARCHAR(50),
    pelagem VARCHAR(30),
    situacao VARCHAR(15),
    sexo VARCHAR(10),
    categoria VARCHAR(10),
    tod VARCHAR(15),
    toe VARCHAR(15),
    data_nascimento VARCHAR(20),
    
    -- Pais
    pai_nome VARCHAR(100),
    pai_registro VARCHAR(20),
    mae_nome VARCHAR(100),
    mae_registro VARCHAR(20),
    
    -- Avós paternos
    avo_paterno_nome VARCHAR(100),
    avo_paterno_registro VARCHAR(20),
    avo_paterna_nome VARCHAR(100),
    avo_paterna_registro VARCHAR(20),
    
    -- Avós maternos
    avo_materno_nome VARCHAR(100),
    avo_materno_registro VARCHAR(20),
    avo_materna_nome VARCHAR(100),
    avo_materna_registro VARCHAR(20),
    
    -- Bisavós paternos (lado paterno)
    bisavo_paterno_1_nome VARCHAR(100),
    bisavo_paterno_1_registro VARCHAR(20),
    bisavo_paterna_1_nome VARCHAR(100),
    bisavo_paterna_1_registro VARCHAR(20),
    bisavo_paterno_2_nome VARCHAR(100),
    bisavo_paterno_2_registro VARCHAR(20),
    bisavo_paterna_2_nome VARCHAR(100),
    bisavo_paterna_2_registro VARCHAR(20),
    
    -- Bisavós maternos (lado materno)
    bisavo_materno_1_nome VARCHAR(100),
    bisavo_materno_1_registro VARCHAR(20),
    bisavo_materna_1_nome VARCHAR(100),
    bisavo_materna_1_registro VARCHAR(20),
    bisavo_materno_2_nome VARCHAR(100),
    bisavo_materno_2_registro VARCHAR(20),
    bisavo_materna_2_nome VARCHAR(100),
    bisavo_materna_2_registro VARCHAR(20),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criação de índices para melhor performance
CREATE INDEX idx_genealogia_registro_animal ON genealogia(registro_animal);
CREATE INDEX idx_genealogia_nome_animal ON genealogia(nome_animal);
CREATE INDEX idx_genealogia_pai_registro ON genealogia(pai_registro);
CREATE INDEX idx_genealogia_mae_registro ON genealogia(mae_registro);