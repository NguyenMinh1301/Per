CREATE TABLE brand (
	id uuid NOT NULL,
	"name" varchar(150) NOT NULL,
	description text NULL,
	website_url text NULL,
	founded_year int4 NULL,
	image_public_id varchar(255) NULL,
	image_url text NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT brand_pkey PRIMARY KEY (id),
	CONSTRAINT uq_brand_name UNIQUE (name)
);
CREATE INDEX idx_brand_is_active ON public.brand USING btree (is_active);
