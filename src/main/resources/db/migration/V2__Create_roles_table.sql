CREATE TABLE roles (
	id bigserial NOT NULL,
	"name" varchar(32) NOT NULL,
	description varchar(255) NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT roles_name_key UNIQUE (name),
	CONSTRAINT roles_pkey PRIMARY KEY (id)
);

INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator role');
INSERT INTO roles (name, description) VALUES ('USER', 'User role');
