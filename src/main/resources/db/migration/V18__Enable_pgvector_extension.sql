-- Enable pgvector extension for vector embeddings
-- Enable pgvector extension for vector similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Create vector_store table for RAG document embeddings
CREATE TABLE IF NOT EXISTS vector_store (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSON,
    embedding vector(768) NOT NULL
);

-- Create HNSW index for fast similarity search using cosine distance
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx 
ON vector_store 
USING hnsw (embedding vector_cosine_ops);


-- Note: Spring AI will auto-create the vector_store table
-- with the following structure:
-- - id UUID PRIMARY KEY
-- - content TEXT
-- - metadata JSONB
-- - embedding VECTOR(768)
