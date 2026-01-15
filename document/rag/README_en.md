RAG Shopping Assistant Overview
================================

The RAG (Retrieval-Augmented Generation) module provides AI-powered product recommendations using semantic search and local LLM inference. It combines vector embeddings with natural language processing to deliver context-aware shopping assistance.

Architecture
------------

```
┌───────────────────────────────────────────────────────────────────────┐
│                         RAG Pipeline Flow                             │
│                                                                       │
│  User Question → VectorStoreService (Semantic Search, PgVector) →     │
│    Retrieve Top-K Products → Build Context → ChatService →            │
│    Inject into Prompt → Ollama LLM → Generate Response                │
└───────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                      Indexing Flow                                    │
│                                                                       │
│  Admin Trigger → VectorStoreService →                                 │
│    Fetch Products → Convert to Documents → Embed (nomic-embed-text) → │
│    Store in PgVector                                                  │
└───────────────────────────────────────────────────────────────────────┘
```

Key Components
--------------

| File | Purpose |
| --- | --- |
| `RagController.java` | REST endpoints for chat and indexing |
| `VectorStoreService.java` | Product indexing and semantic search |
| `ChatService.java` | RAG pipeline orchestration and LLM generation |
| `ChatRequest/Response.java` | DTOs for AI interactions |

Technology Stack
----------------

| Component | Technology | Purpose |
| --- | --- | --- |
| Vector Database | PgVector (PostgreSQL extension) | Store and search 768-dim embeddings |
| Embedding Model | nomic-embed-text (Ollama) | Convert text to vectors |
| Chat Model | llama3.2 (Ollama) | Generate natural language responses |
| Framework | Spring AI | LLM integration abstractions |

Configuration
-------------

### Environment Variables

```yaml
# Ollama LLM Service
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_CHAT_MODEL=llama3.2
OLLAMA_EMBEDDING_MODEL=nomic-embed-text

# RAG Parameters (optional, has defaults)
RAG_SEARCH_TOP_K=5
RAG_SIMILARITY_THRESHOLD=0.7
```

Setup Instructions
------------------

### 1. Start Infrastructure

```bash
# Start PostgreSQL with pgvector and Ollama
docker-compose up -d db ollama

# Wait for Ollama to be healthy
docker ps | grep ollama

# Pull required models (first time only)
./scripts/setup-ollama.sh
```

### 2. Verify Models

```bash
docker exec ollama ollama list
# Expected output:
# llama3.2          latest   ...
# nomic-embed-text  latest   ...
```

### 3. Start Application

```bash
./mvnw spring-boot:run
```

### 4. Index Products

Admin authentication required:

```bash
curl -X POST http://localhost:8080/per/rag/index \
  -H "Authorization: Bearer <ADMIN_JWT_TOKEN>"
```

Response:
```json
{
  "success": true,
  "code": "RAG_INDEX_SUCCESS",
  "data": {
    "status": "Indexing completed successfully",
    "documentsIndexed": 0
  }
}
```

REST API Reference
------------------

### POST /per/rag/chat

Get AI product recommendations (non-streaming).

**Auth:** None (public endpoint)  
**Rate Limit:** `highTraffic`

**Request:**
```json
{
  "question": "What perfumes do you recommend for summer?"
}
```

**Response:**
```json
{
  "success": true,
  "code": "RAG_CHAT_SUCCESS",
  "data": {
    "answer": "For summer, I recommend light and fresh fragrances...",
    "sourceDocuments": [
      "Dior Sauvage",
      "Versace Dylan Blue",
      "Acqua di Gio"
    ]
  }
}
```

### GET /per/rag/chat/stream

Stream AI responses in real-time using Server-Sent Events.

**Auth:** None  
**Rate Limit:** `highTraffic`

**Query Params:**
- `question` (string, required): User question

**Response:** SSE stream

```bash
curl -N http://localhost:8080/per/rag/chat/stream?question=Tell%20me%20about%20floral%20perfumes
```

### POST /per/rag/index

Rebuild vector database with all active products.

**Auth:** `hasRole('ADMIN')`  
**Rate Limit:** None

**Response:**
```json
{
  "success": true,
  "code": "RAG_INDEX_SUCCESS",
  "data": {
    "status": "Indexing completed successfully",
    "documentsIndexed": 0
  }
}
```

Indexing Strategy
-----------------

### Product-to-Document Conversion

Each active product with variants is converted to a `Document`:

**Content includes:**
- Product name, brand, category, origin
- Short description and full description
- Perfume attributes (gender, fragrance family, sillage, longevity, seasonality, occasion)
- Price range (min-max from variants)
- Available volumes (from product variants)

**Metadata includes:**
- `productId`: UUID
- `productName`: String
- `brandName`: String
- `categoryName`: String

### Vector Dimensions

Embeddings are 768-dimensional vectors matching the `nomic-embed-text` model output.

### Index Type

PgVector uses HNSW (Hierarchical Navigable Small World) index for fast approximate nearest neighbor search with cosine distance metric.

Semantic Search Flow
--------------------

1. **Embed Query**: User question → nomic-embed-text → 768-dim vector
2. **Search PgVector**: Find top-K similar documents (default K=5) with similarity > threshold (default 0.7)
3. **Filter Results**: Only return documents meeting similarity threshold
4. **Build Context**: Concatenate document contents with separator `\n\n---\n\n`
5. **Return Documents**: Extract `productName` from metadata for source attribution

Chat Generation Flow
--------------------

1. **Semantic Search**: Call `VectorStoreService.searchSimilar(question, topK, threshold)`
2. **Check Results**: If no documents found, return "no relevant products" message
3. **Build Prompt**: Replace `{context}` and `{question}` in system prompt template
4. **LLM Call**: Send prompt to Ollama Chat Model (streaming or non-streaming)
5. **Return Response**: Package answer + source document names

Error Handling
--------------

All RAG operations use `ApiException` with custom error codes:

| Error Code | HTTP Status | Cause |
| --- | --- | --- |
| `RAG_INDEXING_FAILED` | 500 | Database or embedding error during indexing |
| `RAG_SEARCH_FAILED` | 500 | Vector search query failure |
| `RAG_CHAT_FAILED` | 500 | LLM generation error |
| `RAG_OLLAMA_UNAVAILABLE` | 503 | Ollama service not reachable |

Monitoring and Debugging
------------------------

### Check Vector Data

```sql
-- Connect to PostgreSQL
\c <database_name>

-- Verify pgvector extension
\dx vector

-- Count indexed documents
SELECT COUNT(*) FROM vector_store;

-- View sample documents
SELECT id, metadata->>'productName' as product, 
       metadata->>'brandName' as brand 
FROM vector_store LIMIT 5;

-- Check embedding dimensions
SELECT id, array_length(embedding, 1) as dimensions 
FROM vector_store LIMIT 1;
```

### Test Ollama Directly

```bash
# Test chat model
curl http://localhost:11434/api/generate -d '{
  "model": "llama3.2",
  "prompt": "Hello, recommend a perfume",
  "stream": false
}'

# Test embedding model
curl http://localhost:11434/api/embeddings -d '{
  "model": "nomic-embed-text",
  "prompt": "Dior Sauvage perfume"
}'
```

### Application Logs

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.per.rag: DEBUG
    org.springframework.ai: DEBUG
```

Performance Tuning
------------------

### Search Parameters

| Parameter | Default | Impact |
| --- | --- | --- |
| `search-top-k` | 5 | Higher = more context, slower search |
| `similarity-threshold` | 0.7 | Lower = more results, less relevant |

### Model Selection

Change chat model via environment variable:

```bash
OLLAMA_CHAT_MODEL=mistral  # 7B params, better reasoning
OLLAMA_CHAT_MODEL=llama3   # 8B params, higher quality
OLLAMA_CHAT_MODEL=gemma2   # 9B params, strong instruction following
```

Pull models: `docker exec ollama ollama pull <model-name>`

### Context Window

Default Ollama context: 2048 tokens. Adjust if needed:

```bash
docker exec ollama ollama show llama3.2 --modelfile
# Add: PARAMETER num_ctx 4096
```

Extending the Module
--------------------

### Indexing External Documents

To add customer service documents (FAQs, policies):

**Option 1: Extend VectorStoreService**

```java
public interface VectorStoreService {
    void indexCustomDocument(String content, Map<String, Object> metadata);
}
```

**Option 2: Create DocumentService**

```java
@Service
public class DocumentService {
    
    public void indexMarkdownFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        Map<String, Object> metadata = Map.of(
            "type", "policy",
            "filename", filePath.getFileName().toString()
        );
        
        Document doc = new Document(
            filePath.getFileName().toString(),
            content,
            metadata
        );
        vectorStore.add(List.of(doc));
    }
}
```

**Option 3: Admin Upload Endpoint**

```java
@PostMapping(ApiConstants.Rag.UPLOAD_DOCUMENT)
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> uploadDocument(
    @RequestParam("file") MultipartFile file
) {
    // Read file, create Document, add to vector store
}
```

### Conversation History

To add chat history tracking:

1. Create `Conversation` entity with user, messages, timestamps
2. Store in PostgreSQL
3. Inject previous turns into prompt context
4. Implement conversation ID in `ChatRequest`

Security Considerations
-----------------------

| Aspect | Implementation |
| --- | --- |
| Rate Limiting | Applied via `@RateLimiter(name = "highTraffic")` |
| Admin Endpoints | Index operation requires `ADMIN` role |
| Input Validation | `@NotBlank` on question field |
| Prompt Injection | System prompt separates context from user input |
| Public Access | Chat endpoints are public (no auth required) |

Troubleshooting
---------------

### Ollama Not Responding

```bash
# Check logs
docker logs ollama

# Verify service
curl http://localhost:11434/api/tags

# Restart if needed
docker-compose restart ollama
```

### No Search Results

Possible causes:
- Products not indexed (call `/per/rag/index`)
- Similarity threshold too high (lower `RAG_SIMILARITY_THRESHOLD`)
- Empty vector_store table (check with SQL query)

### Poor Response Quality

Improvements:
- Increase `search-top-k` for more context
- Lower `similarity-threshold` for more candidates
- Enhance system prompt with specific instructions
- Use larger model (e.g., `llama3:70b`)

### SerializationException

Ensure DTOs have `@NoArgsConstructor` for Jackson deserialization.

Testing Recommendations
-----------------------

- Unit test `VectorStoreService` with mock repositories
- Integration test with embedded PgVector (Testcontainers)
- Mock `OllamaChatModel` for `ChatService` unit tests
- End-to-end test full pipeline: index → search → chat

Run tests:
```bash
mvn -Dtest=VectorStoreServiceImplTest,ChatServiceImplTest test
```

Operational Notes
-----------------

- Ollama models stored in volume: `ollama_data:/root/.ollama`
- Vector data persists in PostgreSQL volume: `db:/var/lib/postgresql/data`
- First model pull can take 5-10 minutes depending on network
- HNSW index builds automatically when documents exceed threshold
- Rebuild index via `/per/rag/index` after bulk product updates
