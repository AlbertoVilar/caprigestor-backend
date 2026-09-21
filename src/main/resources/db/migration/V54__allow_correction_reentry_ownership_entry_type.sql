-- Align the persisted ownership entry domain with the CORRECTION_REENTRY
-- value already supported by the application model.

ALTER TABLE goat_ownership_period
    DROP CONSTRAINT ck_goat_ownership_period_entry_type;

ALTER TABLE goat_ownership_period
    ADD CONSTRAINT ck_goat_ownership_period_entry_type
    CHECK (entry_type IN (
        'BIRTH', 'MANUAL_IMPORT', 'ABCC_IMPORT', 'PURCHASE',
        'TRANSFER_IN', 'RETURN', 'EXTERNAL_CLAIM', 'CORRECTION_REENTRY'
    ));
