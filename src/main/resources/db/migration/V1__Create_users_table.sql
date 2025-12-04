CREATE TABLE users (
	id uuid NOT NULL,
	username varchar(50) NOT NULL,
	email varchar(254) NOT NULL,
	password_hash varchar(255) NOT NULL,
	first_name varchar(100) NULL,
	last_name varchar(100) NULL,
	is_email_verified bool DEFAULT false NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	last_login_at timestamptz NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT users_email_key UNIQUE (email),
	CONSTRAINT users_pkey PRIMARY KEY (id),
	CONSTRAINT users_username_key UNIQUE (username)
);
