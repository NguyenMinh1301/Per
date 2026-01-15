#!/bin/bash

echo "=========================================="
echo " Ollama Model Setup for RAG"
echo "=========================================="

# Wait for Ollama service to be ready
echo "Waiting for Ollama service to start..."
sleep 10

# Check if Ollama is running
if ! docker ps | grep -q ollama; then
    echo "ERROR: Ollama container is not running!"
    echo "Please start it first with: docker-compose up -d ollama"
    exit 1
fi

# Pull chat model
echo ""
echo "Pulling llama3.2 model (for chat)..."
docker exec ollama ollama pull llama3.2

# Pull embedding model  
echo ""
echo "Pulling nomic-embed-text model (for embeddings)..."
docker exec ollama ollama pull nomic-embed-text

# List loaded models
echo ""
echo "=========================================="
echo " Installed Models:"
echo "=========================================="
docker exec ollama ollama list

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "1. Start your Spring Boot application"
echo "2. Login as admin and get JWT token"
echo "3. Call POST /per/rag/index to index products"
echo "4. Test chat: POST /per/rag/chat with question"
