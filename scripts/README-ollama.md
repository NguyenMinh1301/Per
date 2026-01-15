# Ollama Model Setup Script

This script pulls required Ollama models for the RAG system.

## Usage

### Default (Gemma 2 2B)

```bash
./scripts/setup-ollama.sh
```

### Specify Model

```bash
# Use Gemma 2 2B (faster, smaller)
./scripts/setup-ollama.sh gemma2:2b

# Use Llama 3.2 (better quality)
./scripts/setup-ollama.sh llama3.2
```

## Available Models

### gemma2:2b (Default)
- **Size:** ~1.6GB
- **RAM:** ~2GB
- **Speed:** Very fast (optimized for CPU)
- **Use Case:** Development, testing, low-resource environments
- **Quality:** Good for most use cases

### llama3.2
- **Size:** ~2GB
- **RAM:** ~3GB
- **Speed:** Fast
- **Use Case:** Production, better reasoning
- **Quality:** Excellent for complex queries

## Important

After pulling models, update your `.env` file:

```bash
OLLAMA_CHAT_MODEL=gemma2:2b  # or llama3.2
```

Then restart your Spring Boot application.
