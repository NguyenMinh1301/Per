# RAG Shopping Assistant - Complete Guide

## Overview

The RAG (Retrieval-Augmented Generation) module provides AI-powered fragrance recommendations using semantic search and local LLM inference. It returns **structured JSON responses** optimized for modern frontend frameworks (Generative UI).

### Key Features

- ✅ **Structured JSON Output** - ProductRecommendation objects with id, name, price, reason
- ✅ **Semantic Search** - PgVector-powered similarity matching  
- ✅ **No Hallucination** - Only recommends products from actual inventory
- ✅ **Generative UI Ready** - Auto-generate product cards, CTA buttons from response
- ✅ **Bilingual** - Auto-detect Vietnamese/English queries

---

## Architecture

### RAG Pipeline Flow

```
User Question
    ↓
Semantic Search (PgVector)
    ↓
Top-K Products (context)
    ↓
Load System Prompt (src/main/resources/prompt/system-prompt.txt)
    ↓
Build Prompt (context + question + JSON schema)
    ↓
Ollama LLM (llama3.2)
    ↓
BeanOutputConverter parses JSON
    ↓
ShopAssistantResponse
```

### Indexing Flow

```
Admin Trigger
    ↓
Fetch Products from Database
    ↓
Convert to Documents (id, name, description, metadata)
    ↓
Embed (nomic-embed-text, 768-dim)
    ↓
Store in vector_store table (PgVector)
```

---

## Response Structure

### ShopAssistantResponse

```json
{
  "summary": "Short 1-2 sentence answer",
  "detailedResponse": "**Hero Recommendation:** Dior Sauvage...",
  "products": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Dior Sauvage EDP 100ml",
      "price": 3500000,
      "reasonForRecommendation": "Fresh summer scent with excellent longevity"
    }
  ],
  "nextSteps": [
    "Would you like to check availability?",
    "Shall I show you similar products?",
    "Would you like to add this to your cart?"
  ]
}
```

### Frontend Integration

```typescript
// Auto-generate UI from structured response
response.products.map(product => (
  <ProductCard 
    id={product.id}
    name={product.name}
    price={product.price}
    reason={product.reasonForRecommendation}
  />
))

// CTA buttons from nextSteps
response.nextSteps.map(step => (
  <Button onClick={() => handleStep(step)}>{step}</Button>
))
```

---

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Vector DB** | PgVector | 768-dim embeddings storage |
| **Embedding Model** | nomic-embed-text | Text → Vector conversion |
| **Chat Model** | llama3.2 | Natural language generation |
| **Framework** | Spring AI 1.0.0-M6 | LLM abstractions |
| **Output Parser** | BeanOutputConverter | JSON schema enforcement |

---

## Configuration

### Environment Variables (`.env`)

```bash
# Ollama Service
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_CHAT_MODEL=llama3.2
OLLAMA_EMBEDDING_MODEL=nomic-embed-text

# RAG Parameters
RAG_SEARCH_TOP_K=5              # Number of similar products to retrieve
RAG_SIMILARITY_THRESHOLD=0.3    # Cosine similarity threshold (0-1)
```

### Application Config (`application-dev.yml`)

```yaml
spring:
  ai:
    ollama:
      base-url: ${OLLAMA_BASE_URL}
      chat:
        model: ${OLLAMA_CHAT_MODEL}
      embedding:
        model: ${OLLAMA_EMBEDDING_MODEL}

app:
  rag:
    search-top-k: ${RAG_SEARCH_TOP_K:5}
    similarity-threshold: ${RAG_SIMILARITY_THRESHOLD:0.3}
```

---

## Setup Instructions

### 1. Start Infrastructure

```bash
# Start PostgreSQL + Ollama
docker-compose up -d db ollama

# Pull required models (first time only)
./scripts/setup-ollama.sh
```

**Verify models:**

```bash
docker exec ollama ollama list
# Expected:
# llama3.2:latest
# nomic-embed-text:latest
```

### 2. Start Application

```bash
./mvnw spring-boot:run
```

### 3. Index Products (Admin Only)

```bash
curl -X POST http://localhost:8080/per/rag/index \
  -H "Authorization: Bearer <ADMIN_JWT>"
```

**Response:**

```json
{
  "success": true,
  "code": "RAG_INDEX_SUCCESS",
  "data": {
    "totalIndexed": 150,
    "timeMs": 2341
  }
}
```

---

## API Reference

### POST `/per/rag/chat`

**Chat with AI assistant (non-streaming)**

**Request:**

```json
{
  "question": "Nước hoa cho mùa hè?"
}
```

**Response:**

```json
{
  "success": true,
  "code": "RAG_CHAT_SUCCESS",
  "data": {
    "summary": "Tôi gợi ý Dior Sauvage - hoàn hảo cho mùa hè với hương tươi mát.",
    "detailedResponse": "**Hero Recommendation:**\nDior Sauvage Eau de Parfum...",
    "products": [
      {
        "id": "uuid-123",
        "name": "Dior Sauvage EDP 100ml",
        "price": 3500000,
        "reasonForRecommendation": "Fresh bergamot perfect for hot weather"
      }
    ],
    "nextSteps": [
      "Bạn muốn biết thêm về họ hương này không?",
      "Tôi có thể gợi ý sản phẩm tương tự?",
      "Bạn muốn thêm vào giỏ hàng không?"
    ]
  }
}
```

### GET `/per/rag/chat/stream`

**Streaming text response (SSE)**

```bash
curl -N http://localhost:8080/per/rag/chat/stream?question=Summer%20fragrance

# Response (text/event-stream):
data: I recommend
data: Dior Sauvage
data: ...
```

### POST `/per/rag/index` (Admin)

**Index all products into vector store**

### POST `/per/rag/index/knowledge` (Admin)

**Index prompt templates from `src/main/resources/prompt/`**

> **Note:** Now deprecated. Prompts are loaded directly from classpath, no indexing needed.

### DELETE `/per/rag/knowledge` (Admin)

**Clear knowledge base documents**

### GET `/per/rag/knowledge/status`

**Check knowledge base indexing status**

```json
{
  "totalDocuments": 150,
  "knowledgeDocuments": 0,
  "productDocuments": 150,
  "knowledgeSources": [],
  "isIndexed": false
}
```

---

## Prompt Architecture

### Prompt Files Location

```
src/main/resources/prompt/
├── system-prompt.txt           # Main RAG prompt (loaded via classpath)
├── ai-assistant-instructions.md
├── customer-service-guide.md
├── fragrance-guide.md
├── policies.md
└── fallback-response.md
```

### Prompt Structure

**system-prompt.txt** uses:

- `{context}` - Replaced with retrieved products
- `{question}` - User's question
- `{format}` - JSON schema from BeanOutputConverter

**Example:**

```
# ROLE
You are a smart fragrance Sales Assistant...

INVENTORY:
{context}

USER QUERY:
{question}

# OUTPUT FORMAT
{format}
```

### XML Tags in Prompt Files (Reference Only)

Prompt reference files (`.md`) use XML structure for better AI comprehension:

```xml
<context type="ai_instructions">
  # Content here (Markdown format)
</context>

<rules>
  ## Rules and constraints
</rules>

<data type="fragrance_knowledge">
  ## Knowledge base content
</data>
```

---

## Troubleshooting

### Issue: "No relevant products found"

**Cause:** Similarity threshold too high or no indexed products

**Fix:**

1. Check products are indexed: `GET /per/rag/knowledge/status`
2. Lower `RAG_SIMILARITY_THRESHOLD` to 0.2
3. Re-index: `POST /per/rag/index`

### Issue: "LLM returns invalid JSON"

**Cause:** Llama 3.2 wrapping JSON in markdown code blocks

**Fix:** `cleanMarkdownCodeBlocks()` method automatically strips ` ```json ` wrappers

### Issue: "Empty products array despite relevant inventory"

**Cause:** LLM filtering too strictly based on seasonality/occasion in prompt

**Fix:** Adjust filtering rules in `system-prompt.txt`

---

## Performance Tuning

### Vector Search

- **Top-K**: Higher = more context but slower (default: 5)
- **Threshold**: Lower = more lenient matching (default: 0.3)

```yaml
app:
  rag:
    search-top-k: 10          # More products in context
    similarity-threshold: 0.2  # More lenient matching
```

### LLM Response Time

- **Model size**: llama3.2 (~2GB, fast inference)
- **Streaming**: Use `/chat/stream` for perceived speed
- **GPU**: Ollama uses GPU if available (10x faster)

---

## FAQ

**Q: Can I use GPT-4 instead of Ollama?**

A: Yes, swap OllamaChatModel for OpenAiChatModel. Change `spring.ai.openai.api-key`.

**Q: How to add custom prompts?**

A: Edit files in `src/main/resources/prompt/`. Restart app (prompts loaded at runtime).

**Q: Why structured output instead of plain text?**

A: Enables Generative UI - frontend can auto-render ProductCards, CTAs, etc.

**Q: How to prevent hallucinations?**

A: Prompt explicitly states "Do NOT invent products". BeanOutputConverter enforces UUID format.

---

## Next Steps

1. **Customize Prompts**: Edit `system-prompt.txt` for your domain
2. **Tune Parameters**: Adjust top-K and threshold for your catalog size
3. **Monitor Performance**: Check LLM response times via logs
4. **A/B Test**: Compare structured vs streaming for user experience

---

## Related Documentation

- [Vietnamese README](./README_vi.md)
- [API Collection](../../postman/per-api-collection.json)
- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/)
