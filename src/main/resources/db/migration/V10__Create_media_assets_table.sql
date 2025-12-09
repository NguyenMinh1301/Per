CREATE TABLE media_assets (
	id uuid NOT NULL,
	asset_id varchar(150) NOT NULL,
	public_id varchar(255) NOT NULL,
	folder varchar(255) NULL,
	resource_type varchar(32) NOT NULL,
	format varchar(50) NULL,
	mime_type varchar(100) NULL,
	url text NOT NULL,
	secure_url text NOT NULL,
	bytes int8 NOT NULL,
	width int4 NULL,
	height int4 NULL,
	duration float8 NULL,
	original_filename varchar(255) NULL,
	etag varchar(100) NULL,
	signature varchar(255) NULL,
	"version" varchar(50) NULL,
	cloud_created_at timestamptz NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT media_assets_pkey PRIMARY KEY (id),
	CONSTRAINT media_assets_public_id_key UNIQUE (public_id)
);
CREATE INDEX idx_media_assets_public_id ON public.media_assets USING btree (public_id);
CREATE INDEX idx_media_assets_resource_type ON public.media_assets USING btree (resource_type);
