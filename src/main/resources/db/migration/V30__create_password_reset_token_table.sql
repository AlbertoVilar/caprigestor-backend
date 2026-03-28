CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_password_reset_token_hash
    ON password_reset_token (token_hash);

CREATE INDEX idx_password_reset_token_user_created
    ON password_reset_token (user_id, created_at DESC, id DESC);

CREATE INDEX idx_password_reset_token_active
    ON password_reset_token (user_id, expires_at DESC, id DESC)
    WHERE used_at IS NULL AND revoked_at IS NULL;
