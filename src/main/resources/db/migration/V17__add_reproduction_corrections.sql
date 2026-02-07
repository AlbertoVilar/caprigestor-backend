-- Add coverage correction fields and stable linkage

ALTER TABLE reproductive_event
    ADD COLUMN related_event_id BIGINT;

ALTER TABLE reproductive_event
    ADD COLUMN corrected_event_date DATE;

CREATE INDEX idx_reproductive_event_related
    ON reproductive_event(related_event_id);

-- Ensure only one correction per coverage
CREATE UNIQUE INDEX ux_reproductive_event_single_correction
    ON reproductive_event(related_event_id)
    WHERE event_type = 'COVERAGE_CORRECTION';

ALTER TABLE pregnancy
    ADD COLUMN coverage_event_id BIGINT;

CREATE INDEX idx_pregnancy_coverage_event
    ON pregnancy(coverage_event_id);
