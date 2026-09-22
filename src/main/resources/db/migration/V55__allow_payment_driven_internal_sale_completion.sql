-- W13 internal sales complete when the seller confirms payment. Buyer
-- acceptance metadata remains optional only for INTERNAL_SALE; all other
-- ownership transfers retain the original acceptance requirement.
ALTER TABLE ownership_transfer
    DROP CONSTRAINT ck_ownership_transfer_completed_not_before_accepted;

ALTER TABLE ownership_transfer
    ADD CONSTRAINT ck_ownership_transfer_completed_not_before_accepted
        CHECK (completed_at IS NULL
            OR (completed_at >= requested_at
                AND (accepted_at IS NULL OR completed_at >= accepted_at)));

ALTER TABLE ownership_transfer
    DROP CONSTRAINT ck_ownership_transfer_completed_lifecycle;

ALTER TABLE ownership_transfer
    ADD CONSTRAINT ck_ownership_transfer_completed_lifecycle
        CHECK (state <> 'COMPLETED' OR (
            effective_at IS NOT NULL AND completed_at IS NOT NULL
            AND completed_by IS NOT NULL AND cancelled_at IS NULL
            AND effective_at = completed_at
            AND (
                (kind = 'INTERNAL_SALE' AND accepted_at IS NULL AND accepted_by IS NULL)
                OR (accepted_at IS NOT NULL AND accepted_by IS NOT NULL)
            )
        ));
