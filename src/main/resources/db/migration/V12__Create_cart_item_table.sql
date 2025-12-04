CREATE TABLE cart_item (
	id uuid NOT NULL,
	cart_id uuid NOT NULL,
	product_id uuid NOT NULL,
	variant_id uuid NOT NULL,
	quantity int4 DEFAULT 1 NOT NULL,
	price numeric(15, 2) DEFAULT 0 NOT NULL,
	sub_total_amount numeric(15, 2) DEFAULT 0 NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT cart_item_pkey PRIMARY KEY (id),
	CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
	CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product(id),
	CONSTRAINT fk_cart_item_variant FOREIGN KEY (variant_id) REFERENCES product_variant(id)
);
CREATE INDEX idx_cart_item_cart ON public.cart_item USING btree (cart_id);
CREATE INDEX idx_cart_item_product ON public.cart_item USING btree (product_id);
CREATE INDEX idx_cart_item_variant ON public.cart_item USING btree (variant_id);
CREATE UNIQUE INDEX uq_cart_item_cart_variant ON public.cart_item USING btree (cart_id, variant_id);
