CREATE TABLE "order" (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    order_code BIGINT NOT NULL UNIQUE,
    total_items INTEGER NOT NULL DEFAULT 0,
    subtotal_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    shipping_fee NUMERIC(15, 2) NOT NULL DEFAULT 0,
    grand_total NUMERIC(15, 2) NOT NULL DEFAULT 0,
    currency_code VARCHAR(10) NOT NULL DEFAULT 'VND',
    receiver_name VARCHAR(180),
    receiver_phone VARCHAR(32),
    shipping_address TEXT,
    note TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_order_user ON "order" (user_id);
CREATE INDEX idx_order_status ON "order" (status);

CREATE TABLE order_item (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID NOT NULL,
    product_name TEXT NOT NULL,
    variant_sku VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    sub_total_amount NUMERIC(15, 2) NOT NULL,
    currency_code VARCHAR(10) NOT NULL DEFAULT 'VND',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_order_item_variant FOREIGN KEY (variant_id) REFERENCES product_variant (id)
);

CREATE INDEX idx_order_item_order ON order_item (order_id);
CREATE INDEX idx_order_item_variant ON order_item (variant_id);
