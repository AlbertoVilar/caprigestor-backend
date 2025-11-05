-- Criação da tabela capril (fazendas de caprinos)
-- Alinha os nomes das colunas com a entidade GoatFarm

-- Em ambiente de desenvolvimento, pode existir uma tabela antiga com nomes em PT-BR.
-- Para garantir consistência, recriamos a tabela com os nomes esperados pelo JPA.
-- ATENÇÃO: Em produção, prefira scripts de ALTER TABLE em vez de DROP.

DROP TABLE IF EXISTS capril CASCADE;

CREATE TABLE capril (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    tod VARCHAR(5) UNIQUE,
    user_id BIGINT NOT NULL,
    address_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 0,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES endereco(id) ON DELETE SET NULL
);

-- Índices auxiliares
CREATE INDEX idx_capril_user_id ON capril(user_id);
CREATE INDEX idx_capril_address_id ON capril(address_id);