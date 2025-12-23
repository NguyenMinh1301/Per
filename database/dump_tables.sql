/*
    Author: nguyenminh1301
    Date:   20/12/2025
*/

-- DROP SCHEMA public;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

-- DROP SEQUENCE public.roles_id_seq;

CREATE SEQUENCE public.roles_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;-- public.brand definition

-- Drop table

-- DROP TABLE public.brand;

CREATE TABLE public.brand (
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


-- public.category definition

-- Drop table

-- DROP TABLE public.category;

CREATE TABLE public.category (
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


-- public.flyway_schema_history definition

-- Drop table

-- DROP TABLE public.flyway_schema_history;

CREATE TABLE public.flyway_schema_history (
                                              installed_rank int4 NOT NULL,
                                              "version" varchar(50) NULL,
                                              description varchar(200) NOT NULL,
                                              "type" varchar(20) NOT NULL,
                                              script varchar(1000) NOT NULL,
                                              checksum int4 NULL,
                                              installed_by varchar(100) NOT NULL,
                                              installed_on timestamp DEFAULT now() NOT NULL,
                                              execution_time int4 NOT NULL,
                                              success bool NOT NULL,
                                              CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);
CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


-- public.made_id definition

-- Drop table

-- DROP TABLE public.made_id;

CREATE TABLE public.made_id (
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


-- public.media_assets definition

-- Drop table

-- DROP TABLE public.media_assets;

CREATE TABLE public.media_assets (
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


-- public.roles definition

-- Drop table

-- DROP TABLE public.roles;

CREATE TABLE public.roles (
                              id bigserial NOT NULL,
                              "name" varchar(32) NOT NULL,
                              description varchar(255) NULL,
                              created_at timestamptz DEFAULT now() NOT NULL,
                              updated_at timestamptz DEFAULT now() NOT NULL,
                              CONSTRAINT roles_name_key UNIQUE (name),
                              CONSTRAINT roles_pkey PRIMARY KEY (id)
);


-- public.users definition

-- Drop table

-- DROP TABLE public.users;

CREATE TABLE public.users (
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


-- public.cart definition

-- Drop table

-- DROP TABLE public.cart;

CREATE TABLE public.cart (
                             id uuid NOT NULL,
                             user_id uuid NOT NULL,
                             total_items int4 DEFAULT 0 NOT NULL,
                             subtotal_amount numeric(15, 2) DEFAULT 0 NOT NULL,
                             discount_amount numeric(15, 2) DEFAULT 0 NOT NULL,
                             total_amount numeric(15, 2) DEFAULT 0 NOT NULL,
                             status varchar(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
                             created_at timestamptz DEFAULT now() NOT NULL,
                             updated_at timestamptz DEFAULT now() NOT NULL,
                             CONSTRAINT cart_pkey PRIMARY KEY (id),
                             CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX uq_cart_user_active ON public.cart USING btree (user_id, status) WHERE ((status)::text = 'ACTIVE'::text);


-- public."order" definition

-- Drop table

-- DROP TABLE public."order";

CREATE TABLE public."order" (
                                id uuid NOT NULL,
                                user_id uuid NOT NULL,
                                order_code int8 NOT NULL,
                                total_items int4 DEFAULT 0 NOT NULL,
                                subtotal_amount numeric(15, 2) DEFAULT 0 NOT NULL,
                                discount_amount numeric(15, 2) DEFAULT 0 NOT NULL,
                                shipping_fee numeric(15, 2) DEFAULT 0 NOT NULL,
                                grand_total numeric(15, 2) DEFAULT 0 NOT NULL,
                                currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
                                receiver_name varchar(180) NULL,
                                receiver_phone varchar(32) NULL,
                                shipping_address text NULL,
                                note text NULL,
                                status varchar(32) DEFAULT 'PENDING_PAYMENT'::character varying NOT NULL,
                                created_at timestamptz DEFAULT now() NOT NULL,
                                updated_at timestamptz DEFAULT now() NOT NULL,
                                CONSTRAINT order_order_code_key UNIQUE (order_code),
                                CONSTRAINT order_pkey PRIMARY KEY (id),
                                CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE RESTRICT
);
CREATE INDEX idx_order_status ON public."order" USING btree (status);
CREATE INDEX idx_order_user ON public."order" USING btree (user_id);


-- public.payment definition

-- Drop table

-- DROP TABLE public.payment;

CREATE TABLE public.payment (
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
                                CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES public."order"(id) ON DELETE CASCADE,
                                CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL
);
CREATE INDEX idx_payment_order_id ON public.payment USING btree (order_id);
CREATE INDEX idx_payment_status ON public.payment USING btree (status);


-- public.payment_transaction definition

-- Drop table

-- DROP TABLE public.payment_transaction;

CREATE TABLE public.payment_transaction (
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
                                            CONSTRAINT fk_payment_txn_payment FOREIGN KEY (payment_id) REFERENCES public.payment(id) ON DELETE CASCADE
);
CREATE INDEX idx_payment_transaction_payment_id ON public.payment_transaction USING btree (payment_id);
CREATE INDEX idx_payment_transaction_status ON public.payment_transaction USING btree (status);


-- public.product definition

-- Drop table

-- DROP TABLE public.product;

CREATE TABLE public.product (
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
                                CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES public.brand(id),
                                CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.category(id),
                                CONSTRAINT fk_product_made_in FOREIGN KEY (made_in_id) REFERENCES public.made_id(id)
);
CREATE INDEX idx_product_brand ON public.product USING btree (brand_id);
CREATE INDEX idx_product_category ON public.product USING btree (category_id);
CREATE INDEX idx_product_is_active ON public.product USING btree (is_active);
CREATE INDEX idx_product_made_in ON public.product USING btree (made_in_id);


-- public.product_variant definition

-- Drop table

-- DROP TABLE public.product_variant;

CREATE TABLE public.product_variant (
                                        id uuid NOT NULL,
                                        product_id uuid NOT NULL,
                                        variant_sku varchar(64) NOT NULL,
                                        volume_ml numeric(6, 2) NOT NULL,
                                        package_type varchar(60) NULL,
                                        price numeric(15, 2) NOT NULL,
                                        compare_at_price numeric(15, 2) NULL,
                                        currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
                                        stock_quantity int4 DEFAULT 0 NOT NULL,
                                        low_stock_threshold int4 DEFAULT 0 NOT NULL,
                                        image_public_id varchar(255) NULL,
                                        image_url text NULL,
                                        is_active bool DEFAULT true NOT NULL,
                                        created_at timestamptz DEFAULT now() NOT NULL,
                                        updated_at timestamptz DEFAULT now() NOT NULL,
                                        CONSTRAINT product_variant_pkey PRIMARY KEY (id),
                                        CONSTRAINT product_variant_variant_sku_key UNIQUE (variant_sku),
                                        CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES public.product(id)
);
CREATE INDEX idx_variant_is_active ON public.product_variant USING btree (is_active);
CREATE INDEX idx_variant_product ON public.product_variant USING btree (product_id);


-- public.user_roles definition

-- Drop table

-- DROP TABLE public.user_roles;

CREATE TABLE public.user_roles (
                                   user_id uuid NOT NULL,
                                   role_id int8 NOT NULL,
                                   CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id),
                                   CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id),
                                   CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);


-- public.user_tokens definition

-- Drop table

-- DROP TABLE public.user_tokens;

CREATE TABLE public.user_tokens (
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
                                    CONSTRAINT user_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_tokens_token_type ON public.user_tokens USING btree (token, type);
CREATE INDEX idx_user_tokens_user_type ON public.user_tokens USING btree (user_id, type);


-- public.cart_item definition

-- Drop table

-- DROP TABLE public.cart_item;

CREATE TABLE public.cart_item (
                                  id uuid NOT NULL,
                                  cart_id uuid NOT NULL,
                                  product_id uuid NOT NULL,
                                  variant_id uuid NOT NULL,
                                  quantity int4 DEFAULT 1 NOT NULL,
                                  price numeric(15, 2) DEFAULT 0 NOT NULL,
                                  sub_total_amount numeric(15, 2) DEFAULT 0 NOT NULL,
                                  created_at timestamptz DEFAULT now() NOT NULL,
                                  updated_at timestamptz DEFAULT now() NOT NULL,
                                  CONSTRAINT cart_item_pkey PRIMARY KEY (id),
                                  CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES public.cart(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES public.product(id),
                                  CONSTRAINT fk_cart_item_variant FOREIGN KEY (variant_id) REFERENCES public.product_variant(id)
);
CREATE INDEX idx_cart_item_cart ON public.cart_item USING btree (cart_id);
CREATE INDEX idx_cart_item_product ON public.cart_item USING btree (product_id);
CREATE INDEX idx_cart_item_variant ON public.cart_item USING btree (variant_id);
CREATE UNIQUE INDEX uq_cart_item_cart_variant ON public.cart_item USING btree (cart_id, variant_id);


-- public.order_item definition

-- Drop table

-- DROP TABLE public.order_item;

CREATE TABLE public.order_item (
                                   id uuid NOT NULL,
                                   order_id uuid NOT NULL,
                                   product_id uuid NOT NULL,
                                   variant_id uuid NOT NULL,
                                   product_name text NOT NULL,
                                   variant_sku varchar(64) NOT NULL,
                                   quantity int4 NOT NULL,
                                   unit_price numeric(15, 2) NOT NULL,
                                   sub_total_amount numeric(15, 2) NOT NULL,
                                   currency_code varchar(10) DEFAULT 'VND'::character varying NOT NULL,
                                   created_at timestamptz DEFAULT now() NOT NULL,
                                   CONSTRAINT order_item_pkey PRIMARY KEY (id),
                                   CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES public."order"(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES public.product(id),
                                   CONSTRAINT fk_order_item_variant FOREIGN KEY (variant_id) REFERENCES public.product_variant(id)
);
CREATE INDEX idx_order_item_order ON public.order_item USING btree (order_id);
CREATE INDEX idx_order_item_variant ON public.order_item USING btree (variant_id);