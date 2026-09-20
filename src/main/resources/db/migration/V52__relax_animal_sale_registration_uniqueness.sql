-- Ownership-sale history is append-only: a rejected/cancelled request may be
-- retried and a living goat may be sold again after a completed A->B handoff.
-- Active workflow uniqueness is enforced by the ownership transfer status query
-- under the canonical goat lock, not by a global registration-number constraint.
ALTER TABLE animal_sale DROP CONSTRAINT IF EXISTS uk_animal_sale_goat_registration;

CREATE INDEX IF NOT EXISTS idx_animal_sale_farm_goat_target
    ON animal_sale (farm_id, goat_technical_id, target_farm_id);
