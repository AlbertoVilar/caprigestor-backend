-- W2: additive persistence foundation for the approved Goat ownership domain.
-- No existing data is backfilled and no legacy table is modified in this wave.

CREATE TABLE goat_creator_reference (
    goat_id BIGINT NOT NULL,
    creator_tod VARCHAR(5),
    creator_farm_id BIGINT,
    creator_name_snapshot VARCHAR(255),
    source VARCHAR(32) NOT NULL,
    evidence_reference VARCHAR(500),
    recorded_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_goat_creator_reference PRIMARY KEY (goat_id),
    CONSTRAINT fk_goat_creator_reference_goat
        FOREIGN KEY (goat_id) REFERENCES cabras (id) ON DELETE RESTRICT,
    CONSTRAINT fk_goat_creator_reference_farm
        FOREIGN KEY (creator_farm_id) REFERENCES capril (id) ON DELETE RESTRICT,
    CONSTRAINT ck_goat_creator_reference_tod_nonblank
        CHECK (creator_tod IS NULL OR btrim(creator_tod) <> ''),
    CONSTRAINT ck_goat_creator_reference_farm_requires_tod
        CHECK (creator_farm_id IS NULL OR creator_tod IS NOT NULL),
    CONSTRAINT ck_goat_creator_reference_source
        CHECK (source IN ('BIRTH', 'ABCC', 'OFFICIAL_DOCUMENT', 'MANUAL_DECLARATION', 'UNKNOWN')),
    CONSTRAINT ck_goat_creator_reference_unknown
        CHECK (source <> 'UNKNOWN' OR (creator_farm_id IS NULL AND creator_tod IS NULL))
);

CREATE TABLE goat_ownership_period (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    goat_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    entry_type VARCHAR(32) NOT NULL,
    exit_type VARCHAR(32),
    source VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_goat_ownership_period PRIMARY KEY (id),
    CONSTRAINT fk_goat_ownership_period_goat
        FOREIGN KEY (goat_id) REFERENCES cabras (id) ON DELETE RESTRICT,
    CONSTRAINT fk_goat_ownership_period_farm
        FOREIGN KEY (farm_id) REFERENCES capril (id) ON DELETE RESTRICT,
    CONSTRAINT ck_goat_ownership_period_interval
        CHECK (ended_at IS NULL OR started_at < ended_at),
    CONSTRAINT ck_goat_ownership_period_exit_pair
        CHECK ((ended_at IS NULL AND exit_type IS NULL)
            OR (ended_at IS NOT NULL AND exit_type IS NOT NULL)),
    CONSTRAINT ck_goat_ownership_period_entry_type
        CHECK (entry_type IN (
            'BIRTH', 'MANUAL_IMPORT', 'ABCC_IMPORT', 'PURCHASE',
            'TRANSFER_IN', 'RETURN', 'EXTERNAL_CLAIM'
        )),
    CONSTRAINT ck_goat_ownership_period_exit_type
        CHECK (exit_type IS NULL OR exit_type IN (
            'TRANSFER_OUT', 'EXTERNAL_SALE', 'DONATION', 'DEATH', 'RETIREMENT'
        )),
    CONSTRAINT ck_goat_ownership_period_source_nonblank
        CHECK (btrim(source) <> ''),
    CONSTRAINT ck_goat_ownership_period_version_nonnegative
        CHECK (version >= 0)
);

CREATE UNIQUE INDEX ux_goat_ownership_period_open_goat
    ON goat_ownership_period (goat_id)
    WHERE ended_at IS NULL;

CREATE INDEX idx_goat_ownership_period_goat_started
    ON goat_ownership_period (goat_id, started_at);

CREATE INDEX idx_goat_ownership_period_farm_started
    ON goat_ownership_period (farm_id, started_at);

CREATE TABLE ownership_transfer (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    goat_id BIGINT NOT NULL,
    source_farm_id BIGINT,
    target_farm_id BIGINT NOT NULL,
    kind VARCHAR(32) NOT NULL,
    state VARCHAR(16) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    effective_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    requested_by BIGINT NOT NULL,
    accepted_by BIGINT,
    completed_by BIGINT,
    sale_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_ownership_transfer PRIMARY KEY (id),
    CONSTRAINT fk_ownership_transfer_goat
        FOREIGN KEY (goat_id) REFERENCES cabras (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ownership_transfer_source_farm
        FOREIGN KEY (source_farm_id) REFERENCES capril (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ownership_transfer_target_farm
        FOREIGN KEY (target_farm_id) REFERENCES capril (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ownership_transfer_requested_by
        FOREIGN KEY (requested_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ownership_transfer_accepted_by
        FOREIGN KEY (accepted_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ownership_transfer_completed_by
        FOREIGN KEY (completed_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ownership_transfer_sale
        FOREIGN KEY (sale_id) REFERENCES animal_sale (id) ON DELETE RESTRICT,
    CONSTRAINT ck_ownership_transfer_kind
        CHECK (
            (kind = 'EXTERNAL_CLAIM' AND source_farm_id IS NULL)
            OR (kind IN ('INTERNAL_TRANSFER', 'INTERNAL_SALE', 'RETURN')
                AND source_farm_id IS NOT NULL
                AND source_farm_id <> target_farm_id)
        ),
    CONSTRAINT ck_ownership_transfer_kind_allowed
        CHECK (kind IN ('INTERNAL_TRANSFER', 'INTERNAL_SALE', 'RETURN', 'EXTERNAL_CLAIM')),
    CONSTRAINT ck_ownership_transfer_state_allowed
        CHECK (state IN ('REQUESTED', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT ck_ownership_transfer_reason_nonblank
        CHECK (btrim(reason) <> ''),
    CONSTRAINT ck_ownership_transfer_idempotency_nonblank
        CHECK (btrim(idempotency_key) <> ''),
    CONSTRAINT ck_ownership_transfer_version_nonnegative
        CHECK (version >= 0),
    CONSTRAINT ck_ownership_transfer_accepted_not_before_requested
        CHECK (accepted_at IS NULL OR accepted_at >= requested_at),
    CONSTRAINT ck_ownership_transfer_completed_not_before_accepted
        CHECK (completed_at IS NULL
            OR (accepted_at IS NOT NULL AND completed_at >= accepted_at)),
    CONSTRAINT ck_ownership_transfer_cancelled_not_before_previous
        CHECK (cancelled_at IS NULL
            OR (cancelled_at >= requested_at
                AND (accepted_at IS NULL OR cancelled_at >= accepted_at))),
    CONSTRAINT ck_ownership_transfer_effective_equals_completed
        CHECK (effective_at IS NULL
            OR (completed_at IS NOT NULL AND effective_at = completed_at)),
    CONSTRAINT ck_ownership_transfer_requested_lifecycle
        CHECK (state <> 'REQUESTED' OR (
            accepted_at IS NULL AND effective_at IS NULL AND completed_at IS NULL
            AND cancelled_at IS NULL AND accepted_by IS NULL AND completed_by IS NULL
        )),
    CONSTRAINT ck_ownership_transfer_accepted_lifecycle
        CHECK (state <> 'ACCEPTED' OR (
            accepted_at IS NOT NULL AND accepted_by IS NOT NULL
            AND effective_at IS NULL AND completed_at IS NULL
            AND cancelled_at IS NULL AND completed_by IS NULL
        )),
    CONSTRAINT ck_ownership_transfer_completed_lifecycle
        CHECK (state <> 'COMPLETED' OR (
            accepted_at IS NOT NULL AND accepted_by IS NOT NULL
            AND effective_at IS NOT NULL AND completed_at IS NOT NULL
            AND completed_by IS NOT NULL AND cancelled_at IS NULL
            AND effective_at = completed_at
        )),
    CONSTRAINT ck_ownership_transfer_rejected_lifecycle
        CHECK (state <> 'REJECTED' OR (
            accepted_at IS NULL AND effective_at IS NULL AND completed_at IS NULL
            AND cancelled_at IS NULL AND accepted_by IS NULL AND completed_by IS NULL
        )),
    CONSTRAINT ck_ownership_transfer_cancelled_lifecycle
        CHECK (state <> 'CANCELLED' OR (
            cancelled_at IS NOT NULL AND effective_at IS NULL
            AND completed_at IS NULL AND completed_by IS NULL
            AND ((accepted_at IS NULL AND accepted_by IS NULL)
                OR (accepted_at IS NOT NULL AND accepted_by IS NOT NULL))
        )),
    CONSTRAINT uk_ownership_transfer_request_idempotency
        UNIQUE (requested_by, idempotency_key)
);

CREATE UNIQUE INDEX ux_ownership_transfer_pending_goat
    ON ownership_transfer (goat_id)
    WHERE state IN ('REQUESTED', 'ACCEPTED');

CREATE INDEX idx_ownership_transfer_source_state
    ON ownership_transfer (source_farm_id, state);

CREATE INDEX idx_ownership_transfer_target_state
    ON ownership_transfer (target_farm_id, state);

CREATE INDEX idx_ownership_transfer_goat
    ON ownership_transfer (goat_id);
