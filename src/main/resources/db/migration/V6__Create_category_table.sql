CREATE TABLE category (
	id uuid NOT NULL,
	"name" varchar(150) NOT NULL,
	description text NULL,
	descriptions text NULL,
	image_public_id varchar(255) NULL,
	image_url text NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT category_pkey PRIMARY KEY (id),
	CONSTRAINT uq_category_name UNIQUE (name)
);
CREATE INDEX idx_category_is_active ON public.category USING btree (is_active);
