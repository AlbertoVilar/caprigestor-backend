-- Event recording provenance is intentionally nullable for legacy rows.
-- New command-side writes always supply it; no legacy attribution is inferred.
ALTER TABLE eventos
    ADD COLUMN recording_farm_id BIGINT,
    ADD CONSTRAINT fk_eventos_recording_farm
        FOREIGN KEY (recording_farm_id) REFERENCES capril(id) ON DELETE RESTRICT;

CREATE INDEX idx_eventos_recording_farm_goat_technical
    ON eventos (recording_farm_id, goat_technical_id);
