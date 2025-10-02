-- Criação da tabela capril (fazendas de caprinos)
-- Esta tabela armazena as fazendas/caprils do sistema

CREATE TABLE capril (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    tod VARCHAR(15) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    address_id BIGINT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES endereco(id) ON DELETE SET NULL
);

-- Criação de índices para melhor performance
CREATE INDEX idx_capril_user_id ON capril(user_id);
CREATE INDEX idx_capril_address_id ON capril(address_id);
CREATE INDEX idx_capril_tod ON capril(tod);
CREATE INDEX idx_capril_nome ON capril(nome);