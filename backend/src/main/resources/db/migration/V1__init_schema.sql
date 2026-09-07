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

CREATE TABLE orders (
                        id              BIGSERIAL PRIMARY KEY,
                        merchant_id     VARCHAR(64) NOT NULL,
                        customer_id     VARCHAR(64),

                        link_token      VARCHAR(128) UNIQUE,
                        clover_order_id VARCHAR(64) UNIQUE,

                        title           VARCHAR(255),
                        status          VARCHAR(32) NOT NULL DEFAULT 'DRAFT', -- 'DRAFT', 'OPEN', 'PAID', 'EXPIRED', 'CANCELED'

                        currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
                        subtotal_amount BIGINT NOT NULL DEFAULT 0,       -- before taxes/discounts in cents
                        tax_amount      BIGINT NOT NULL DEFAULT 0,
                        total_amount    BIGINT NOT NULL DEFAULT 0,

                        expires_at      TIMESTAMPTZ,
                        paid_at         TIMESTAMPTZ,
                        created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                        CONSTRAINT fk_orders_merchant
                            FOREIGN KEY (merchant_id)
                                REFERENCES merchants(merchant_id)
                                ON DELETE CASCADE
);

CREATE TABLE order_items (
                             id              BIGSERIAL PRIMARY KEY,
                             order_id        BIGINT NOT NULL,

                             clover_item_id  VARCHAR(64),
                             name            VARCHAR(255) NOT NULL,
                             price           BIGINT NOT NULL,
                             quantity        INT NOT NULL DEFAULT 1,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE
);

CREATE INDEX idx_orders_merchant_status ON orders(merchant_id, status);
CREATE INDEX idx_orders_link_token ON orders(link_token);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);