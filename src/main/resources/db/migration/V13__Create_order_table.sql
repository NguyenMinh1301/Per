CREATE TABLE "order" (
	id uuid NOT NULL,
	user_id uuid NOT NULL,
	order_code int8 NOT NULL,
	total_items int4 DEFAULT 0 NOT NULL,
	subtotal_amount numeric(15, 2) DEFAULT 0 NOT NULL,
	discount_amount numeric(15, 2) DEFAULT 0 NOT NULL,
	shipping_fee numeric(15, 2) DEFAULT 0 NOT NULL,
	grand_total numeric(15, 2) DEFAULT 0 NOT NULL,
	currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
	receiver_name varchar(180) NULL,
	receiver_phone varchar(32) NULL,
	shipping_address text NULL,
	note text NULL,
	status varchar(32) DEFAULT 'PENDING_PAYMENT'::character varying NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT order_order_code_key UNIQUE (order_code),
	CONSTRAINT order_pkey PRIMARY KEY (id),
	CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);
CREATE INDEX idx_order_status ON public."order" USING btree (status);
CREATE INDEX idx_order_user ON public."order" USING btree (user_id);
