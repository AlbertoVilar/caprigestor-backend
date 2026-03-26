CREATE TABLE commercial_customer (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL REFERENCES capril(id),
    name VARCHAR(120) NOT NULL,
    document VARCHAR(40),
    phone VARCHAR(40),
    email VARCHAR(120),
    notes VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_commercial_customer_farm_name
    ON commercial_customer (farm_id, name);

CREATE TABLE animal_sale (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL REFERENCES capril(id),
    customer_id BIGINT NOT NULL REFERENCES commercial_customer(id),
    goat_registration_number VARCHAR(20) NOT NULL REFERENCES cabras(num_registro),
    goat_name VARCHAR(100) NOT NULL,
    sale_date DATE NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    payment_status VARCHAR(10) NOT NULL,
    payment_date DATE,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_animal_sale_goat_registration UNIQUE (goat_registration_number)
);

CREATE INDEX idx_animal_sale_farm_date
    ON animal_sale (farm_id, sale_date DESC, id DESC);

CREATE INDEX idx_animal_sale_farm_payment
    ON animal_sale (farm_id, payment_status, due_date);

CREATE TABLE milk_sale (
    id BIGSERIAL PRIMARY KEY,
    farm_id BIGINT NOT NULL REFERENCES capril(id),
    customer_id BIGINT NOT NULL REFERENCES commercial_customer(id),
    sale_date DATE NOT NULL,
    quantity_liters NUMERIC(12, 2) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    due_date DATE NOT NULL,
    payment_status VARCHAR(10) NOT NULL,
    payment_date DATE,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_milk_sale_farm_date
    ON milk_sale (farm_id, sale_date DESC, id DESC);

CREATE INDEX idx_milk_sale_farm_payment
    ON milk_sale (farm_id, payment_status, due_date);
