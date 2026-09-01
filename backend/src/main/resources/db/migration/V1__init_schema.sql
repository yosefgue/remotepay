CREATE TABLE merchants (
    id            BIGSERIAL PRIMARY KEY,
    merchant_id   VARCHAR(64) NOT NULL,
    merchant_name VARCHAR(255),
    CONSTRAINT uq_merchants_merchant_id UNIQUE (merchant_id)
);

CREATE TABLE oauth_tokens (
    id                       BIGSERIAL PRIMARY KEY,
    merchant_id              VARCHAR(64) NOT NULL,
    access_token             VARCHAR(2048) NOT NULL,
    refresh_token            VARCHAR(2048) NOT NULL,
    access_token_expires_at  TIMESTAMPTZ NOT NULL,
    refresh_token_expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_oauth_tokens_merchant_id UNIQUE (merchant_id),
    CONSTRAINT fk_oauth_tokens_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(merchant_id)
        ON DELETE CASCADE
);

CREATE TABLE customers (
                           id           BIGSERIAL PRIMARY KEY,
                           customer_id  VARCHAR(64) NOT NULL,
                           merchant_id  VARCHAR(64) NOT NULL,
                           first_name   VARCHAR(100),
                           last_name    VARCHAR(100),
                           email        VARCHAR(255),
                           phone_number VARCHAR(50),
                           CONSTRAINT uq_customers_merchant_customer UNIQUE (merchant_id, customer_id),
                           CONSTRAINT fk_customers_merchant
                               FOREIGN KEY (merchant_id)
                                   REFERENCES merchants(merchant_id)
                                   ON DELETE CASCADE
);

CREATE INDEX idx_customers_merchant_id ON customers (merchant_id);

CREATE TABLE items (
                       id                   BIGSERIAL PRIMARY KEY,
                       item_id              VARCHAR(64) NOT NULL,
                       merchant_id          VARCHAR(64) NOT NULL,
                       name                 VARCHAR(255) NOT NULL,
                       price                BIGINT NOT NULL,              -- Price in cents (e.g. 1050 = $10.50)
                       available            BOOLEAN NOT NULL DEFAULT true,
                       stock_quantity       DOUBLE PRECISION,             -- NULL = untracked / unlimited
                       clover_modified_time BIGINT,
                       CONSTRAINT uq_items_merchant_item UNIQUE (merchant_id, item_id),
                       CONSTRAINT fk_items_merchant
                           FOREIGN KEY (merchant_id)
                               REFERENCES merchants(merchant_id)
                               ON DELETE CASCADE
);

CREATE INDEX idx_items_merchant_id ON items (merchant_id);