CREATE TABLE order_item (
	id uuid NOT NULL,
	order_id uuid NOT NULL,
	product_id uuid NOT NULL,
	variant_id uuid NOT NULL,
	product_name text NOT NULL,
	variant_sku varchar(64) NOT NULL,
	quantity int4 NOT NULL,
	unit_price numeric(15, 2) NOT NULL,
	sub_total_amount numeric(15, 2) NOT NULL,
	currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT order_item_pkey PRIMARY KEY (id),
	CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE,
	CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id),
	CONSTRAINT fk_order_item_variant FOREIGN KEY (variant_id) REFERENCES product_variant(id)
);
CREATE INDEX idx_order_item_order ON public.order_item USING btree (order_id);
CREATE INDEX idx_order_item_variant ON public.order_item USING btree (variant_id);
