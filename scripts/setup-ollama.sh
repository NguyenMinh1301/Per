#!/bin/bash

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default model
DEFAULT_CHAT_MODEL="gemma2:2b"

echo -e "${BLUE}=== Ollama Model Setup ===${NC}\n"

# Parse command line arguments
CHAT_MODEL="${1:-$DEFAULT_CHAT_MODEL}"

# Validate chat model selection
if [[ "$CHAT_MODEL" != "gemma2:2b" && "$CHAT_MODEL" != "llama3.2" ]]; then
    echo -e "${YELLOW}Invalid model specified. Choose 'gemma2:2b' or 'llama3.2'.${NC}"
    echo -e "Usage: $0 [gemma2:2b|llama3.2]"
    echo -e "Example: $0 gemma2:2b"
    echo -e "\nDefaulting to: ${GREEN}${DEFAULT_CHAT_MODEL}${NC}\n"
    CHAT_MODEL="$DEFAULT_CHAT_MODEL"
fi

# Check if Ollama container is running
if ! docker ps | grep -q ollama; then
    echo -e "${RED}Error: Ollama container is not running${NC}"
    echo -e "Please start it with: ${YELLOW}docker-compose up -d ollama${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} Ollama container is running\n"

# Pull embedding model (required for RAG)
echo -e "${BLUE}[1/2] Pulling embedding model: nomic-embed-text${NC}"
docker exec ollama ollama pull nomic-embed-text

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Embedding model pulled successfully\n"
else
    echo -e "${RED}✗${NC} Failed to pull embedding model\n"
    exit 1
fi

# Pull chat model
echo -e "${BLUE}[2/2] Pulling chat model: ${CHAT_MODEL}${NC}"
docker exec ollama ollama pull "$CHAT_MODEL"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Chat model pulled successfully\n"
else
    echo -e "${RED}✗${NC} Failed to pull chat model\n"
    exit 1
fi

# List available models
echo -e "${BLUE}Available models:${NC}"
docker exec ollama ollama list

# Update .env reminder
echo -e "\n${YELLOW}⚠ IMPORTANT: Update your .env file${NC}"
echo -e "Set the following variable:"
echo -e "${GREEN}OLLAMA_CHAT_MODEL=${CHAT_MODEL}${NC}"

if [[ "$CHAT_MODEL" == "gemma2:2b" ]]; then
    echo -e "\n${BLUE}Model Info: Gemma 2 2B${NC}"
    echo -e "- Size: ~1.6GB"
    echo -e "- Speed: Very fast (optimized for CPU)"
    echo -e "- Memory: ~2GB RAM"
    echo -e "- Best for: Development, testing, low-resource environments"
elif [[ "$CHAT_MODEL" == "llama3.2" ]]; then
    echo -e "\n${BLUE}Model Info: Llama 3.2 3B${NC}"
    echo -e "- Size: ~2GB"
    echo -e "- Speed: Fast"
    echo -e "- Memory: ~3GB RAM"
    echo -e "- Best for: Production, better reasoning"
fi

echo -e "\n${GREEN}✓ Setup completed successfully!${NC}"
