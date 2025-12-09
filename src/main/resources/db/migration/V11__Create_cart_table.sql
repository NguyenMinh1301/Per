CREATE TABLE cart (
	id uuid NOT NULL,
	user_id uuid NOT NULL,
	total_items int4 DEFAULT 0 NOT NULL,
	subtotal_amount numeric(15, 2) DEFAULT 0 NOT NULL,
	discount_amount numeric(15, 2) DEFAULT 0 NOT NULL,
	total_amount numeric(15, 2) DEFAULT 0 NOT NULL,
	status varchar(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT cart_pkey PRIMARY KEY (id),
	CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_cart_user_active ON public.cart USING btree (user_id, status) WHERE ((status)::text = 'ACTIVE'::text);
