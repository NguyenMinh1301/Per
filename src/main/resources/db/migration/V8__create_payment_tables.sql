CREATE TABLE payment (
    id UUID PRIMARY KEY,
    user_id UUID,
    order_id UUID NOT NULL,
    order_code BIGINT NOT NULL UNIQUE,
    payment_link_id VARCHAR(128) NOT NULL UNIQUE,
    amount NUMERIC(15, 2) NOT NULL,
    currency_code VARCHAR(10) NOT NULL DEFAULT 'VND',
    description TEXT,
    checkout_url TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    expired_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_order_id ON payment (order_id);
CREATE INDEX idx_payment_status ON payment (status);

CREATE TABLE payment_transaction (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    reference VARCHAR(128),
    amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'SUCCEEDED',
    transaction_date_time TIMESTAMPTZ,
    currency_code VARCHAR(10) NOT NULL DEFAULT 'VND',
    account_number VARCHAR(64),
    counter_account_number VARCHAR(64),
    counter_account_name VARCHAR(255),
    raw_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_payment_transaction_reference UNIQUE (reference),
    CONSTRAINT fk_payment_txn_payment FOREIGN KEY (payment_id) REFERENCES payment (id) ON DELETE CASCADE
);

CREATE INDEX idx_payment_transaction_payment_id ON payment_transaction (payment_id);
CREATE INDEX idx_payment_transaction_status ON payment_transaction (status);
