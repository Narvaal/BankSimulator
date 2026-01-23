CREATE TABLE client
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    document   VARCHAR(50)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE account
(
    id             BIGSERIAL PRIMARY KEY,
    client_id      BIGINT         NOT NULL,
    account_number VARCHAR(50)    NOT NULL UNIQUE,
    account_type   VARCHAR(50)    NOT NULL,
    status         VARCHAR(50)    NOT NULL,
    balance        NUMERIC(19, 2) NOT NULL DEFAULT 0,
    public_key     TEXT           NOT NULL,
    created_at     TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_account_client
        FOREIGN KEY (client_id)
            REFERENCES client (id)
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

CREATE TABLE asset
(
    id           BIGSERIAL PRIMARY KEY,
    text         TEXT      NOT NULL UNIQUE,
    total_supply INT       NOT NULL CHECK (total_supply > 0),
    created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE asset_unit
(
    id               BIGSERIAL PRIMARY KEY,
    asset_id         BIGINT    NOT NULL,
    owner_account_id BIGINT    NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_asset_unit_asset
        FOREIGN KEY (asset_id)
            REFERENCES asset (id),

    CONSTRAINT fk_asset_unit_owner
        FOREIGN KEY (owner_account_id)
            REFERENCES account (id)
);

CREATE TABLE asset_transfer
(
    id              BIGSERIAL PRIMARY KEY,
    asset_unit_id   BIGINT    NOT NULL,
    from_account_id BIGINT    NOT NULL,
    to_account_id   BIGINT    NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_asset_transfer_unit
        FOREIGN KEY (asset_unit_id)
            REFERENCES asset_unit (id),

    CONSTRAINT fk_asset_transfer_from
        FOREIGN KEY (from_account_id)
            REFERENCES account (id),

    CONSTRAINT fk_asset_transfer_to
        FOREIGN KEY (to_account_id)
            REFERENCES account (id)
);

CREATE TABLE asset_listing
(
    id                BIGSERIAL PRIMARY KEY,
    asset_unit_id     BIGINT         NOT NULL,
    seller_account_id BIGINT         NOT NULL,
    price             NUMERIC(19, 2) NOT NULL CHECK (price > 0),
    status            VARCHAR(50)    NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_asset_listing_unit
        FOREIGN KEY (asset_unit_id)
            REFERENCES asset_unit (id),

    CONSTRAINT fk_asset_listing_seller
        FOREIGN KEY (seller_account_id)
            REFERENCES account (id)
);

CREATE TABLE asset_price_history (
                                     id BIGSERIAL PRIMARY KEY,
                                     asset_listing_id BIGINT NOT NULL,
                                     asset_unity_id BIGINT NOT NULL,
                                     old_price DECIMAL(19, 2),
                                     new_price DECIMAL(19, 2) NOT NULL,
                                     changed_by_account_id BIGINT NOT NULL,
                                     reason VARCHAR(50) NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_price_history_listing
                                         FOREIGN KEY (asset_listing_id)
                                             REFERENCES asset_listing(id),

                                     CONSTRAINT fk_price_history_unity
                                         FOREIGN KEY (asset_unity_id)
                                             REFERENCES asset_unit(id),

                                     CONSTRAINT fk_price_history_account
                                         FOREIGN KEY (changed_by_account_id)
                                             REFERENCES account(id)
);

CREATE INDEX idx_asset_unit_owner
    ON asset_unit (owner_account_id);

CREATE INDEX idx_asset_listing_status
    ON asset_listing (status);

CREATE INDEX idx_asset_listing_unit
    ON asset_listing (asset_unit_id);

CREATE INDEX idx_asset_transfer_unit
    ON asset_transfer (asset_unit_id);

CREATE TABLE credential (
                            id BIGSERIAL PRIMARY KEY,
                            client_id BIGINT NOT NULL,
                            document VARCHAR(50) NOT NULL,
                            password_hash VARCHAR(255) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_credential_client
                                FOREIGN KEY (client_id)
                                    REFERENCES client(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT uk_credential_document
                                UNIQUE (document),

                            CONSTRAINT uk_credential_client
                                UNIQUE (client_id)
);
