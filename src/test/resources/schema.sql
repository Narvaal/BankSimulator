CREATE TABLE client (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        document VARCHAR(20) NOT NULL UNIQUE,

                        created_at TIMESTAMP NOT NULL DEFAULT now(),
                        updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE account (
                         id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                         client_id BIGINT NOT NULL,

                         account_number VARCHAR(20) NOT NULL UNIQUE,
                         account_type VARCHAR(20) NOT NULL,
                         balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
                         status VARCHAR(20) NOT NULL,
                         public_key TEXT NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT now(),
                         updated_at TIMESTAMP NOT NULL DEFAULT now(),

                         CONSTRAINT fk_account_client
                             FOREIGN KEY (client_id)
                                 REFERENCES client(id)
);
CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',
    'WITHDRAW',
    'TRANSFERENCE'
    );

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'COMPLETE',
    'FAILED'
    );

CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,

                              from_account_id BIGINT,
                              to_account_id   BIGINT,

                              from_account_number VARCHAR(20),
                              to_account_number   VARCHAR(20),

                              amount NUMERIC(15,2) NOT NULL,

                              type VARCHAR(20) NOT NULL,
                              status VARCHAR(20) NOT NULL,

                              signature TEXT,

                              created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE asset (
                       id BIGSERIAL PRIMARY KEY,
                       text TEXT NOT NULL,
                       total_supply INT NOT NULL CHECK (total_supply > 0),
                       created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE asset_unit (
                            id BIGSERIAL PRIMARY KEY,
                            asset_id BIGINT NOT NULL,
                            owner_account_id BIGINT NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_asset_unit_asset_id
    ON asset_unit (asset_id);

CREATE INDEX idx_asset_unit_owner_account_id
    ON asset_unit (owner_account_id);
