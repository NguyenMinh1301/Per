CREATE TABLE product (
	id uuid NOT NULL,
	brand_id uuid NOT NULL,
	category_id uuid NOT NULL,
	made_in_id uuid NOT NULL,
	"name" varchar(255) NOT NULL,
	short_description varchar(600) NULL,
	description text NULL,
	launch_year int4 NULL,
	image_public_id varchar(255) NULL,
	image_url text NULL,
	fragrance_family varchar(80) NULL,
	gender varchar(20) NULL,
	sillage varchar(30) NULL,
	longevity varchar(30) NULL,
	seasonality varchar(80) NULL,
	occasion varchar(120) NULL,
	is_limited_edition bool DEFAULT false NOT NULL,
	is_discontinued bool DEFAULT false NOT NULL,
	is_active bool DEFAULT true NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT product_pkey PRIMARY KEY (id),
	CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
	CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
	CONSTRAINT fk_product_made_in FOREIGN KEY (made_in_id) REFERENCES made_id(id)
);
CREATE INDEX idx_product_brand ON public.product USING btree (brand_id);
CREATE INDEX idx_product_category ON public.product USING btree (category_id);
CREATE INDEX idx_product_is_active ON public.product USING btree (is_active);
CREATE INDEX idx_product_made_in ON public.product USING btree (made_in_id);
