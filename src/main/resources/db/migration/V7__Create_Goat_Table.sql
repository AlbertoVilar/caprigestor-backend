-- Criação da tabela cabras (caprinos)
-- Esta tabela armazena os animais (cabras/bodes) do sistema

CREATE TABLE cabras (
    num_registro VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sexo VARCHAR(10) NOT NULL CHECK (sexo IN ('MALE', 'FEMALE')),
    raca VARCHAR(50),
    cor VARCHAR(30),
    data_nascimento DATE NOT NULL,
    status VARCHAR(15) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'SOLD', 'DECEASED')),
    tod VARCHAR(15),
    toe VARCHAR(15),
    categoria VARCHAR(10) CHECK (categoria IN ('PO', 'PA', 'PC')),
    pai_num_registro VARCHAR(20),
    mae_num_registro VARCHAR(20),
    usuario_id BIGINT NOT NULL,
    capril_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    FOREIGN KEY (pai_num_registro) REFERENCES cabras(num_registro) ON DELETE SET NULL,
    FOREIGN KEY (mae_num_registro) REFERENCES cabras(num_registro) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (capril_id) REFERENCES capril(id) ON DELETE CASCADE
);

-- Criação de índices para melhor performance
CREATE INDEX idx_cabras_usuario_id ON cabras(usuario_id);
CREATE INDEX idx_cabras_capril_id ON cabras(capril_id);
CREATE INDEX idx_cabras_pai_num_registro ON cabras(pai_num_registro);
CREATE INDEX idx_cabras_mae_num_registro ON cabras(mae_num_registro);
CREATE INDEX idx_cabras_nome ON cabras(nome);
CREATE INDEX idx_cabras_sexo ON cabras(sexo);
CREATE INDEX idx_cabras_status ON cabras(status);
CREATE INDEX idx_cabras_data_nascimento ON cabras(data_nascimento);