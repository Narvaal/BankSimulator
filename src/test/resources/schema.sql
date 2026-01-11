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
