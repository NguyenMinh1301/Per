CREATE TABLE user_tokens (
	id uuid NOT NULL,
	"token" varchar(255) NOT NULL,
	"type" varchar(50) NOT NULL,
	user_id uuid NOT NULL,
	expires_at timestamptz NOT NULL,
	consumed_at timestamptz NULL,
	revoked bool DEFAULT false NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT user_tokens_pkey PRIMARY KEY (id),
	CONSTRAINT user_tokens_token_key UNIQUE (token),
	CONSTRAINT user_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_tokens_token_type ON public.user_tokens USING btree (token, type);
CREATE INDEX idx_user_tokens_user_type ON public.user_tokens USING btree (user_id, type);
