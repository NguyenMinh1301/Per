CREATE TABLE brand (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    website_url TEXT,
    founded_year INTEGER,
    image_public_id VARCHAR(255),
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_brand_is_active ON brand (is_active);

CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) UNIQUE,
    description TEXT,
    descriptions TEXT,
    image_public_id VARCHAR(255),
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_category_is_active ON category (is_active);

CREATE TABLE made_id (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    iso_code VARCHAR(10),
    region VARCHAR(120),
    description TEXT,
    image_public_id VARCHAR(255),
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_made_id_is_active ON made_id (is_active);

CREATE TABLE product (
    id UUID PRIMARY KEY,
    brand_id UUID NOT NULL,
    category_id UUID NOT NULL,
    made_in_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_description VARCHAR(600),
    description TEXT,
    launch_year INTEGER,
    image_public_id VARCHAR(255),
    image_url TEXT,
    fragrance_family VARCHAR(80),
    gender VARCHAR(20),
    sillage VARCHAR(30),
    longevity VARCHAR(30),
    seasonality VARCHAR(80),
    occasion VARCHAR(120),
    is_limited_edition BOOLEAN NOT NULL DEFAULT FALSE,
    is_discontinued BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand (id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_made_in FOREIGN KEY (made_in_id) REFERENCES made_id (id)
);

CREATE INDEX idx_product_brand ON product (brand_id);
CREATE INDEX idx_product_category ON product (category_id);
CREATE INDEX idx_product_made_in ON product (made_in_id);
CREATE INDEX idx_product_is_active ON product (is_active);

CREATE TABLE product_variant (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    variant_sku VARCHAR(64) NOT NULL UNIQUE,
    volume_ml NUMERIC(6, 2) NOT NULL,
    package_type VARCHAR(60),
    price NUMERIC(15, 2) NOT NULL,
    compare_at_price NUMERIC(15, 2),
    currency_code VARCHAR(10) NOT NULL DEFAULT 'VND',
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 0,
    image_public_id VARCHAR(255),
    image_url TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE INDEX idx_variant_product ON product_variant (product_id);
CREATE INDEX idx_variant_is_active ON product_variant (is_active);
