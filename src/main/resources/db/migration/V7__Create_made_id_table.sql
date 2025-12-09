CREATE TABLE made_id (
	id uuid NOT NULL,
	"name" varchar(120) NOT NULL,
	iso_code varchar(10) NULL,
	region varchar(120) NULL,
	description text NULL,
	image_public_id varchar(255) NULL,
	image_url text NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT made_id_pkey PRIMARY KEY (id),
	CONSTRAINT uq_made_id_name UNIQUE (name)
);
CREATE INDEX idx_made_id_is_active ON public.made_id USING btree (is_active);
