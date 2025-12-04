CREATE TABLE payment_transaction (
	id uuid NOT NULL,
	payment_id uuid NOT NULL,
	reference varchar(128) NULL,
	amount numeric(15, 2) NOT NULL,
	status varchar(16) DEFAULT 'SUCCEEDED'::character varying NOT NULL,
	transaction_date_time timestamptz NULL,
	currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
	account_number varchar(64) NULL,
	counter_account_number varchar(64) NULL,
	counter_account_name varchar(255) NULL,
	raw_payload jsonb DEFAULT '{}'::jsonb NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT payment_transaction_pkey PRIMARY KEY (id),
	CONSTRAINT uq_payment_transaction_reference UNIQUE (reference),
	CONSTRAINT fk_payment_txn_payment FOREIGN KEY (payment_id) REFERENCES payment(id) ON DELETE CASCADE
);
CREATE INDEX idx_payment_transaction_payment_id ON public.payment_transaction USING btree (payment_id);
CREATE INDEX idx_payment_transaction_status ON public.payment_transaction USING btree (status);
