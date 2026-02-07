-- Add coverage correction fields and stable linkage

ALTER TABLE reproductive_event
    ADD COLUMN IF NOT EXISTS related_event_id BIGINT;

ALTER TABLE reproductive_event
    ADD COLUMN IF NOT EXISTS corrected_event_date DATE;

CREATE INDEX IF NOT EXISTS idx_reproductive_event_related
    ON reproductive_event(related_event_id);

-- Ensure only one correction per coverage
CREATE UNIQUE INDEX IF NOT EXISTS ux_reproductive_event_single_correction
    ON reproductive_event(related_event_id)
    WHERE event_type = 'COVERAGE_CORRECTION';

ALTER TABLE pregnancy
    ADD COLUMN IF NOT EXISTS coverage_event_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_pregnancy_coverage_event
    ON pregnancy(coverage_event_id);
