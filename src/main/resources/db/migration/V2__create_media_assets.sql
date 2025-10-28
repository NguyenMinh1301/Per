CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    asset_id VARCHAR(150) NOT NULL,
    public_id VARCHAR(255) NOT NULL UNIQUE,
    folder VARCHAR(255),
    resource_type VARCHAR(32) NOT NULL,
    format VARCHAR(50),
    mime_type VARCHAR(100),
    url TEXT NOT NULL,
    secure_url TEXT NOT NULL,
    bytes BIGINT NOT NULL,
    width INTEGER,
    height INTEGER,
    duration DOUBLE PRECISION,
    original_filename VARCHAR(255),
    etag VARCHAR(100),
    signature VARCHAR(255),
    version VARCHAR(50),
    cloud_created_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_assets_public_id ON media_assets (public_id);
CREATE INDEX idx_media_assets_resource_type ON media_assets (resource_type);
