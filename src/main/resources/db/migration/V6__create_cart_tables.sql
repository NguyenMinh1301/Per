CREATE TABLE cart (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    total_items INTEGER NOT NULL DEFAULT 0,
    subtotal_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_cart_user_active ON cart (user_id, status) WHERE status = 'ACTIVE';

CREATE TABLE cart_item (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    price NUMERIC(15, 2) NOT NULL DEFAULT 0,
    sub_total_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_cart_item_variant FOREIGN KEY (variant_id) REFERENCES product_variant (id)
);

CREATE UNIQUE INDEX uq_cart_item_cart_variant ON cart_item (cart_id, variant_id);
CREATE INDEX idx_cart_item_cart ON cart_item (cart_id);
CREATE INDEX idx_cart_item_product ON cart_item (product_id);
CREATE INDEX idx_cart_item_variant ON cart_item (variant_id);
