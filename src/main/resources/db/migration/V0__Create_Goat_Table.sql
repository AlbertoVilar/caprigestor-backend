-- Criação da tabela goat compatível com H2 e PostgreSQL
CREATE TABLE goat (
    registration_number VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(16),
    breed VARCHAR(32),
    color VARCHAR(32),
    birth_date DATE,
    status VARCHAR(32),
    tod VARCHAR(32),
    toe VARCHAR(32),
    category VARCHAR(32),
    father_registration_number VARCHAR(32),
    mother_registration_number VARCHAR(32),
    farm_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- Índices para performance e integridade
CREATE INDEX idx_goat_farm_id ON goat(farm_id);
CREATE INDEX idx_goat_user_id ON goat(user_id);

