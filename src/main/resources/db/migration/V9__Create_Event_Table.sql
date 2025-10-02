-- Criação da tabela eventos (eventos dos caprinos)
-- Esta tabela armazena eventos relacionados aos animais (vacinação, nascimento, etc.)

CREATE TABLE eventos (
    id BIGSERIAL PRIMARY KEY,
    goat_registration_number VARCHAR(20) NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    data DATE NOT NULL,
    descricao TEXT,
    local VARCHAR(255),
    veterinario VARCHAR(255),
    resultado VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chave estrangeira
    FOREIGN KEY (goat_registration_number) REFERENCES cabras(num_registro) ON DELETE CASCADE
);

-- Criação de índices para melhor performance
CREATE INDEX idx_eventos_goat_registration_number ON eventos(goat_registration_number);
CREATE INDEX idx_eventos_tipo_evento ON eventos(tipo_evento);
CREATE INDEX idx_eventos_data ON eventos(data);