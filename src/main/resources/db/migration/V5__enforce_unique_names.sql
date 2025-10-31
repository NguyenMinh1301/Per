ALTER TABLE brand
    ADD CONSTRAINT uq_brand_name UNIQUE (name);

ALTER TABLE category
    ADD CONSTRAINT uq_category_name UNIQUE (name);

ALTER TABLE made_id
    ADD CONSTRAINT uq_made_id_name UNIQUE (name);
