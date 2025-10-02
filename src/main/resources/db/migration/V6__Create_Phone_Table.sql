-- Criação da tabela telefone (telefones das fazendas)
-- Esta tabela armazena os telefones de contato das fazendas

CREATE TABLE telefone (
    id BIGSERIAL PRIMARY KEY,
    ddd VARCHAR(3) NOT NULL,
    numero VARCHAR(15) NOT NULL,
    goat_farm_id BIGINT NOT NULL,
    
    -- Chave estrangeira
    FOREIGN KEY (goat_farm_id) REFERENCES capril(id) ON DELETE CASCADE,
    
    -- Constraint de unicidade para evitar telefones duplicados
    UNIQUE(ddd, numero)
);

-- Criação de índices para melhor performance
CREATE INDEX idx_telefone_goat_farm_id ON telefone(goat_farm_id);
CREATE INDEX idx_telefone_ddd_numero ON telefone(ddd, numero);