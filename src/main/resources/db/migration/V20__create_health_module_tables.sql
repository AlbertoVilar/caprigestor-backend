CREATE TABLE health_events (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    goat_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    scheduled_date DATE NOT NULL,
    performed_at TIMESTAMP,
    responsible VARCHAR(255),
    notes TEXT,
    product_name VARCHAR(100),
    active_ingredient VARCHAR(100),
    dose DECIMAL(10,3),
    dose_unit VARCHAR(20),
    route VARCHAR(50),
    batch_number VARCHAR(50),
    withdrawal_milk_days INTEGER,
    withdrawal_meat_days INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_health_events_farm FOREIGN KEY (farm_id) REFERENCES capril(id),
    CONSTRAINT fk_health_events_goat FOREIGN KEY (goat_id) REFERENCES cabras(num_registro)
);

CREATE INDEX idx_health_farm_goat ON health_events(farm_id, goat_id);
CREATE INDEX idx_health_farm_status ON health_events(farm_id, status);
CREATE INDEX idx_health_farm_date ON health_events(farm_id, scheduled_date);
