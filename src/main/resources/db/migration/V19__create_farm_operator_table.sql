CREATE TABLE tb_farm_operator (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_farm_operator UNIQUE (farm_id, user_id),
    CONSTRAINT fk_farm_operator_farm FOREIGN KEY (farm_id) REFERENCES capril(id) ON DELETE CASCADE,
    CONSTRAINT fk_farm_operator_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_farm_operator_farm_id ON tb_farm_operator(farm_id);
CREATE INDEX idx_farm_operator_user_id ON tb_farm_operator(user_id);
