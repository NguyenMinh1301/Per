/*
    Author: nguyenminh1301
    Date:   09/11/2025
*/

create table public.flyway_schema_history
(
    installed_rank integer                 not null
        constraint flyway_schema_history_pk
            primary key,
    version        varchar(50),
    description    varchar(200)            not null,
    type           varchar(20)             not null,
    script         varchar(1000)           not null,
    checksum       integer,
    installed_by   varchar(100)            not null,
    installed_on   timestamp default now() not null,
    execution_time integer                 not null,
    success        boolean                 not null
);

alter table public.flyway_schema_history
    owner to postgres;

create index flyway_schema_history_s_idx
    on public.flyway_schema_history (success);

create table public.roles
(
    id          bigserial
        primary key,
    name        varchar(32)                            not null
        unique,
    description varchar(255),
    created_at  timestamp with time zone default now() not null,
    updated_at  timestamp with time zone default now() not null
);

alter table public.roles
    owner to postgres;

create table public.users
(
    id                uuid                                   not null
        primary key,
    username          varchar(50)                            not null
        unique,
    email             varchar(254)                           not null
        unique,
    password_hash     varchar(255)                           not null,
    first_name        varchar(100),
    last_name         varchar(100),
    is_email_verified boolean                  default false not null,
    is_active         boolean                  default true  not null,
    last_login_at     timestamp with time zone,
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone default now() not null
);

alter table public.users
    owner to postgres;

create table public.user_roles
(
    user_id uuid   not null
        references public.users
            on delete cascade,
    role_id bigint not null
        references public.roles,
    primary key (user_id, role_id)
);

alter table public.user_roles
    owner to postgres;

create table public.user_tokens
(
    id          uuid                                   not null
        primary key,
    token       varchar(255)                           not null
        unique,
    type        varchar(50)                            not null,
    user_id     uuid                                   not null
        references public.users
            on delete cascade,
    expires_at  timestamp with time zone               not null,
    consumed_at timestamp with time zone,
    revoked     boolean                  default false not null,
    created_at  timestamp with time zone default now() not null,
    updated_at  timestamp with time zone default now() not null
);

alter table public.user_tokens
    owner to postgres;

create index idx_user_tokens_token_type
    on public.user_tokens (token, type);

create index idx_user_tokens_user_type
    on public.user_tokens (user_id, type);

create table public.media_assets
(
    id                uuid                                   not null
        primary key,
    asset_id          varchar(150)                           not null,
    public_id         varchar(255)                           not null
        unique,
    folder            varchar(255),
    resource_type     varchar(32)                            not null,
    format            varchar(50),
    mime_type         varchar(100),
    url               text                                   not null,
    secure_url        text                                   not null,
    bytes             bigint                                 not null,
    width             integer,
    height            integer,
    duration          double precision,
    original_filename varchar(255),
    etag              varchar(100),
    signature         varchar(255),
    version           varchar(50),
    cloud_created_at  timestamp with time zone,
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone default now() not null
);

alter table public.media_assets
    owner to postgres;

create index idx_media_assets_public_id
    on public.media_assets (public_id);

create index idx_media_assets_resource_type
    on public.media_assets (resource_type);

create table public.brand
(
    id              uuid                                   not null
        primary key,
    name            varchar(150)                           not null
        constraint uq_brand_name
            unique,
    description     text,
    website_url     text,
    founded_year    integer,
    image_public_id varchar(255),
    image_url       text,
    is_active       boolean                  default true  not null,
    created_at      timestamp with time zone default now() not null,
    updated_at      timestamp with time zone default now() not null
);

alter table public.brand
    owner to postgres;

create index idx_brand_is_active
    on public.brand (is_active);

create table public.category
(
    id              uuid                                   not null
        primary key,
    name            varchar(150)                           not null
        constraint uq_category_name
            unique,
    description     text,
    descriptions    text,
    image_public_id varchar(255),
    image_url       text,
    is_active       boolean                  default true  not null,
    created_at      timestamp with time zone default now() not null,
    updated_at      timestamp with time zone default now() not null
);

alter table public.category
    owner to postgres;

create index idx_category_is_active
    on public.category (is_active);

create table public.made_id
(
    id              uuid                                   not null
        primary key,
    name            varchar(120)                           not null
        constraint uq_made_id_name
            unique,
    iso_code        varchar(10),
    region          varchar(120),
    description     text,
    image_public_id varchar(255),
    image_url       text,
    is_active       boolean                  default true  not null,
    created_at      timestamp with time zone default now() not null,
    updated_at      timestamp with time zone default now() not null
);

alter table public.made_id
    owner to postgres;

create index idx_made_id_is_active
    on public.made_id (is_active);

create table public.product
(
    id                 uuid                                   not null
        primary key,
    brand_id           uuid                                   not null
        constraint fk_product_brand
            references public.brand,
    category_id        uuid                                   not null
        constraint fk_product_category
            references public.category,
    made_in_id         uuid                                   not null
        constraint fk_product_made_in
            references public.made_id,
    name               varchar(255)                           not null,
    short_description  varchar(600),
    description        text,
    launch_year        integer,
    image_public_id    varchar(255),
    image_url          text,
    fragrance_family   varchar(80),
    gender             varchar(20),
    sillage            varchar(30),
    longevity          varchar(30),
    seasonality        varchar(80),
    occasion           varchar(120),
    is_limited_edition boolean                  default false not null,
    is_discontinued    boolean                  default false not null,
    is_active          boolean                  default true  not null,
    created_at         timestamp with time zone default now() not null,
    updated_at         timestamp with time zone default now() not null
);

alter table public.product
    owner to postgres;

create index idx_product_brand
    on public.product (brand_id);

create index idx_product_category
    on public.product (category_id);

create index idx_product_made_in
    on public.product (made_in_id);

create index idx_product_is_active
    on public.product (is_active);

create table public.product_variant
(
    id                  uuid                                                      not null
        primary key,
    product_id          uuid                                                      not null
        constraint fk_variant_product
            references public.product,
    variant_sku         varchar(64)                                               not null
        unique,
    volume_ml           numeric(6, 2)                                             not null,
    package_type        varchar(60),
    price               numeric(15, 2)                                            not null,
    compare_at_price    numeric(15, 2),
    currency_code       varchar(10)              default 'VND'::character varying not null,
    stock_quantity      integer                  default 0                        not null,
    low_stock_threshold integer                  default 0                        not null,
    image_public_id     varchar(255),
    image_url           text,
    is_active           boolean                  default true                     not null,
    created_at          timestamp with time zone default now()                    not null,
    updated_at          timestamp with time zone default now()                    not null
);

alter table public.product_variant
    owner to postgres;

create index idx_variant_product
    on public.product_variant (product_id);

create index idx_variant_is_active
    on public.product_variant (is_active);

create table public.cart
(
    id              uuid                                                         not null
        primary key,
    user_id         uuid                                                         not null
        constraint fk_cart_user
            references public.users
            on delete cascade,
    total_items     integer                  default 0                           not null,
    subtotal_amount numeric(15, 2)           default 0                           not null,
    discount_amount numeric(15, 2)           default 0                           not null,
    total_amount    numeric(15, 2)           default 0                           not null,
    status          varchar(20)              default 'ACTIVE'::character varying not null,
    created_at      timestamp with time zone default now()                       not null,
    updated_at      timestamp with time zone default now()                       not null
);

alter table public.cart
    owner to postgres;

create unique index uq_cart_user_active
    on public.cart (user_id, status)
    where ((status)::text = 'ACTIVE'::text);

create table public.cart_item
(
    id               uuid                                   not null
        primary key,
    cart_id          uuid                                   not null
        constraint fk_cart_item_cart
            references public.cart
            on delete cascade,
    product_id       uuid                                   not null
        constraint fk_cart_item_product
            references public.product,
    variant_id       uuid                                   not null
        constraint fk_cart_item_variant
            references public.product_variant,
    quantity         integer                  default 1     not null,
    price            numeric(15, 2)           default 0     not null,
    sub_total_amount numeric(15, 2)           default 0     not null,
    created_at       timestamp with time zone default now() not null,
    updated_at       timestamp with time zone default now() not null
);

alter table public.cart_item
    owner to postgres;

create unique index uq_cart_item_cart_variant
    on public.cart_item (cart_id, variant_id);

create index idx_cart_item_cart
    on public.cart_item (cart_id);

create index idx_cart_item_product
    on public.cart_item (product_id);

create index idx_cart_item_variant
    on public.cart_item (variant_id);

create table public."order"
(
    id               uuid                                                                  not null
        primary key,
    user_id          uuid                                                                  not null
        constraint fk_order_user
            references public.users
            on delete restrict,
    order_code       bigint                                                                not null
        unique,
    total_items      integer                  default 0                                    not null,
    subtotal_amount  numeric(15, 2)           default 0                                    not null,
    discount_amount  numeric(15, 2)           default 0                                    not null,
    shipping_fee     numeric(15, 2)           default 0                                    not null,
    grand_total      numeric(15, 2)           default 0                                    not null,
    currency_code    varchar(10)              default 'VND'::character varying             not null,
    receiver_name    varchar(180),
    receiver_phone   varchar(32),
    shipping_address text,
    note             text,
    status           varchar(32)              default 'PENDING_PAYMENT'::character varying not null,
    created_at       timestamp with time zone default now()                                not null,
    updated_at       timestamp with time zone default now()                                not null
);

alter table public."order"
    owner to postgres;

create index idx_order_user
    on public."order" (user_id);

create index idx_order_status
    on public."order" (status);

create table public.order_item
(
    id               uuid                                                      not null
        primary key,
    order_id         uuid                                                      not null
        constraint fk_order_item_order
            references public."order"
            on delete cascade,
    product_id       uuid                                                      not null
        constraint fk_order_item_product
            references public.product,
    variant_id       uuid                                                      not null
        constraint fk_order_item_variant
            references public.product_variant,
    product_name     text                                                      not null,
    variant_sku      varchar(64)                                               not null,
    quantity         integer                                                   not null,
    unit_price       numeric(15, 2)                                            not null,
    sub_total_amount numeric(15, 2)                                            not null,
    currency_code    varchar(10)              default 'VND'::character varying not null,
    created_at       timestamp with time zone default now()                    not null
);

alter table public.order_item
    owner to postgres;

create index idx_order_item_order
    on public.order_item (order_id);

create index idx_order_item_variant
    on public.order_item (variant_id);

create table public.payment
(
    id              uuid                                                          not null
        primary key,
    user_id         uuid
        constraint fk_payment_user
            references public.users
            on delete set null,
    order_id        uuid                                                          not null
        constraint fk_payment_order
            references public."order"
            on delete cascade,
    order_code      bigint                                                        not null
        unique,
    payment_link_id varchar(128)                                                  not null
        unique,
    amount          numeric(15, 2)                                                not null,
    currency_code   varchar(10)              default 'VND'::character varying     not null,
    description     text,
    checkout_url    text,
    status          varchar(32)              default 'PENDING'::character varying not null,
    expired_at      timestamp with time zone,
    created_at      timestamp with time zone default now()                        not null,
    updated_at      timestamp with time zone default now()                        not null
);

alter table public.payment
    owner to postgres;

create index idx_payment_order_id
    on public.payment (order_id);

create index idx_payment_status
    on public.payment (status);

create table public.payment_transaction
(
    id                     uuid                                                            not null
        primary key,
    payment_id             uuid                                                            not null
        constraint fk_payment_txn_payment
            references public.payment
            on delete cascade,
    reference              varchar(128)
        constraint uq_payment_transaction_reference
            unique,
    amount                 numeric(15, 2)                                                  not null,
    status                 varchar(16)              default 'SUCCEEDED'::character varying not null,
    transaction_date_time  timestamp with time zone,
    currency_code          varchar(10)              default 'VND'::character varying       not null,
    account_number         varchar(64),
    counter_account_number varchar(64),
    counter_account_name   varchar(255),
    raw_payload            jsonb                    default '{}'::jsonb                    not null,
    created_at             timestamp with time zone default now()                          not null
);

alter table public.payment_transaction
    owner to postgres;

create index idx_payment_transaction_payment_id
    on public.payment_transaction (payment_id);

create index idx_payment_transaction_status
    on public.payment_transaction (status);

