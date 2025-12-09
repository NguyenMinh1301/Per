CREATE TABLE payment (
	id uuid NOT NULL,
	user_id uuid NULL,
	order_id uuid NOT NULL,
	order_code int8 NOT NULL,
	payment_link_id varchar(128) NOT NULL,
	amount numeric(15, 2) NOT NULL,
	currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
	description text NULL,
	checkout_url text NULL,
	status varchar(32) DEFAULT 'PENDING'::character varying NOT NULL,
	expired_at timestamptz NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT payment_order_code_key UNIQUE (order_code),
	CONSTRAINT payment_payment_link_id_key UNIQUE (payment_link_id),
	CONSTRAINT payment_pkey PRIMARY KEY (id),
	CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES "order"(id) ON DELETE CASCADE,
	CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_payment_order_id ON public.payment USING btree (order_id);
CREATE INDEX idx_payment_status ON public.payment USING btree (status);
