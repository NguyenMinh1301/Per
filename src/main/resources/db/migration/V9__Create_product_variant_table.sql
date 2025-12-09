CREATE TABLE product_variant (
	id uuid NOT NULL,
	product_id uuid NOT NULL,
	variant_sku varchar(64) NOT NULL,
	volume_ml numeric(6, 2) NOT NULL,
	package_type varchar(60) NULL,
	price numeric(15, 2) NOT NULL,
	compare_at_price numeric(15, 2) NULL,
	currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
	stock_quantity int4 DEFAULT 0 NOT NULL,
	low_stock_threshold int4 DEFAULT 0 NOT NULL,
	image_public_id varchar(255) NULL,
	image_url text NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT product_variant_pkey PRIMARY KEY (id),
	CONSTRAINT product_variant_variant_sku_key UNIQUE (variant_sku),
	CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES product(id)
);
CREATE INDEX idx_variant_is_active ON public.product_variant USING btree (is_active);
CREATE INDEX idx_variant_product ON public.product_variant USING btree (product_id);
