CREATE TABLE client
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255),
    email          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255),
    provider       VARCHAR(20)  NOT NULL,
    provider_id    VARCHAR(255),
    email_verified BOOLEAN      NOT NULL DEFAULT false,
    picture        TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_provider UNIQUE (provider, provider_id)
);

CREATE TABLE account
(
    id                 BIGSERIAL PRIMARY KEY,
    client_id          BIGINT         NOT NULL,
    account_number     VARCHAR(50)    NOT NULL UNIQUE,
    account_type       VARCHAR(50)    NOT NULL,
    status             VARCHAR(50)    NOT NULL,
    balance            NUMERIC(19, 2) NOT NULL DEFAULT 0,
    public_key         TEXT           NOT NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP      NOT NULL DEFAULT now(),
    next_free_artifact_at TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_account_client
        FOREIGN KEY (client_id)
            REFERENCES client (id)
);

CREATE TYPE verification_type AS ENUM (
    'EMAIL_VERIFICATION',
    'PASSWORD_RESET'
    );

CREATE TABLE email_verification
(
    id          BIGSERIAL PRIMARY KEY,
    client_id   BIGINT            NOT NULL,
    token       VARCHAR(255)      NOT NULL UNIQUE,
    type        verification_type NOT NULL,
    expires_at  TIMESTAMP         NOT NULL,
    verified_at TIMESTAMP,
    created_at  TIMESTAMP         NOT NULL DEFAULT now(),

    CONSTRAINT fk_email_verification_client
        FOREIGN KEY (client_id)
            REFERENCES client (id)
            ON DELETE CASCADE
);

CREATE TABLE transactions
(
    id                  BIGSERIAL PRIMARY KEY,
    from_account_id     BIGINT,
    from_account_number VARCHAR(50),
    to_account_id       BIGINT,
    to_account_number   VARCHAR(50),
    amount              NUMERIC(19, 2) NOT NULL,
    type                VARCHAR(50)    NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    signature           TEXT           NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE TABLE artifact
(
    id           BIGSERIAL PRIMARY KEY,
    metadata     JSONB     NOT NULL DEFAULT '{}',
    total_supply INT       NOT NULL CHECK (total_supply >= 0),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE artifact_bundle
(
    id         BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE artifact_bundle_item
(
    id        BIGSERIAL PRIMARY KEY,
    bundle_id BIGINT NOT NULL,
    artifact_id  BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_bundle_item_bundle
        FOREIGN KEY (bundle_id)
            REFERENCES artifact_bundle (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_bundle_item_asset
        FOREIGN KEY (artifact_id)
            REFERENCES artifact (id)
            ON DELETE CASCADE
);

CREATE TYPE artifact_unit_status AS ENUM (
    'AVAILABLE',
    'IN_MARKET',
    'RESERVED',
    'TRANSFERRING'
    );

CREATE TABLE artifact_unit
(
    id               BIGSERIAL PRIMARY KEY,
    artifact_id         BIGINT            NOT NULL,
    owner_account_id BIGINT            NOT NULL,
    status           artifact_unit_status NOT NULL DEFAULT 'AVAILABLE',
    locked_at        TIMESTAMP         NULL,
    created_at       TIMESTAMP         NOT NULL DEFAULT now(),

    CONSTRAINT fk_artifact_unit_asset
        FOREIGN KEY (artifact_id)
            REFERENCES artifact (id),

    CONSTRAINT fk_artifact_unit_owner
        FOREIGN KEY (owner_account_id)
            REFERENCES account (id)
);

CREATE TABLE artifact_transfer
(
    id              BIGSERIAL PRIMARY KEY,
    artifact_unit_id   BIGINT    NOT NULL,
    from_account_id BIGINT    NOT NULL,
    to_account_id   BIGINT    NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_artifact_transfer_unit
        FOREIGN KEY (artifact_unit_id)
            REFERENCES artifact_unit (id),

    CONSTRAINT fk_artifact_transfer_from
        FOREIGN KEY (from_account_id)
            REFERENCES account (id),

    CONSTRAINT fk_artifact_transfer_to
        FOREIGN KEY (to_account_id)
            REFERENCES account (id)
);

CREATE TABLE artifact_listing
(
    id                BIGSERIAL PRIMARY KEY,
    artifact_unit_id     BIGINT         NOT NULL,
    seller_account_id BIGINT         NOT NULL,
    price             NUMERIC(19, 2) NOT NULL CHECK (price > 0),
    status            VARCHAR(50)    NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_artifact_listing_unit
        FOREIGN KEY (artifact_unit_id)
            REFERENCES artifact_unit (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_artifact_listing_seller
        FOREIGN KEY (seller_account_id)
            REFERENCES account (id)
            ON DELETE RESTRICT
);

CREATE TABLE artifact_price_history
(
    id                    BIGSERIAL PRIMARY KEY,
    artifact_listing_id      BIGINT         NOT NULL,
    artifact_unit_id        BIGINT         NOT NULL,
    old_price             DECIMAL(19, 2),
    new_price             DECIMAL(19, 2) NOT NULL,
    changed_by_account_id BIGINT         NOT NULL,
    reason                VARCHAR(50)    NOT NULL,
    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_price_history_listing
        FOREIGN KEY (artifact_listing_id)
            REFERENCES artifact_listing (id),

    CONSTRAINT fk_price_history_unity
        FOREIGN KEY (artifact_unit_id)
            REFERENCES artifact_unit (id),

    CONSTRAINT fk_price_history_account
        FOREIGN KEY (changed_by_account_id)
            REFERENCES account (id)
);

CREATE INDEX idx_artifact_unit_owner
    ON artifact_unit (owner_account_id);

CREATE INDEX idx_artifact_listing_status
    ON artifact_listing (status);

CREATE INDEX idx_artifact_listing_unit
    ON artifact_listing (artifact_unit_id);

CREATE INDEX idx_artifact_transfer_unit
    ON artifact_transfer (artifact_unit_id);

CREATE TABLE credential
(
    id            BIGSERIAL PRIMARY KEY,
    client_id     BIGINT       NOT NULL,
    email         VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_credential_client
        FOREIGN KEY (client_id)
            REFERENCES client (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_credential_email
        UNIQUE (email),

    CONSTRAINT uk_credential_client
        UNIQUE (client_id)
);
