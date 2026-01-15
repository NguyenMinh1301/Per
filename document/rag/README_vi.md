# Hệ Thống RAG Shopping Assistant - Hướng Dẫn Đầy Đủ

## Tổng Quan

Module RAG (Retrieval-Augmented Generation) cung cấp tính năng gợi ý nước hoa thông minh sử dụng semantic search và LLM chạy local. Trả về **JSON có cấu trúc** tối ưu cho Generative UI.

### Tính Năng Chính

- ✅ **Structured JSON Output** - Đối tượng ProductRecommendation với id, name, price, lý do
- ✅ **Semantic Search** - Tìm kiếm tương đồng bằng PgVector
- ✅ **Không Hallucination** - Chỉ gợi ý sản phẩm có thật trong kho
- ✅ **Generative UI Ready** - Tự động tạo product cards, nút CTA từ response
- ✅ **Bilingual** - Tự động detect câu hỏi Tiếng Việt/English

---

## Kiến Trúc

### Luồng RAG Pipeline

```
Câu Hỏi User
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
BeanOutputConverter parse JSON
    ↓
ShopAssistantResponse
```

### Luồng Indexing

```
Admin Trigger
    ↓
Lấy Products từ Database
    ↓
Convert sang Documents (id, name, description, metadata)
    ↓
Embed (nomic-embed-text, 768-chiều)
    ↓
Lưu vào bảng vector_store (PgVector)
```

---

## Cấu Trúc Response

### ShopAssistantResponse

```json
{
  "summary": "Câu trả lời ngắn gọn 1-2 câu",
  "detailedResponse": "**Gợi Ý Chính:** Dior Sauvage...",
  "products": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Dior Sauvage EDP 100ml",
      "price": 3500000,
      "reasonForRecommendation": "Hương tươi mát cho hè, độ bền cao"
    }
  ],
  "nextSteps": [
    "Bạn muốn kiểm tra tồn kho không?",
    "Tôi có thể gợi ý sản phẩm tương tự?",
    "Bạn muốn thêm vào giỏ hàng không?"
  ]
}
```

### Tích Hợp Frontend

```typescript
// Tự động tạo UI từ structured response
response.products.map(product => (
  <ProductCard 
    id={product.id}
    name={product.name}
    price={product.price}
    reason={product.reasonForRecommendation}
  />
))

// Nút CTA từ nextSteps
response.nextSteps.map(step => (
  <Button onClick={() => handleStep(step)}>{step}</Button>
))
```

---

## Stack Công Nghệ

| Thành Phần | Công Nghệ | Mục Đích |
|-----------|-----------|---------|
| **Vector DB** | PgVector | Lưu embeddings 768-chiều |
| **Embedding Model** | nomic-embed-text | Chuyển text → vector |
| **Chat Model** | llama3.2 | Sinh ngôn ngữ tự nhiên |
| **Framework** | Spring AI 1.0.0-M6 | Abstractions cho LLM |
| **Output Parser** | BeanOutputConverter | Ép JSON schema |

---

## Cấu Hình

### Biến Môi Trường (`.env`)

```bash
# Ollama Service
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_CHAT_MODEL=llama3.2
OLLAMA_EMBEDDING_MODEL=nomic-embed-text

# Tham Số RAG
RAG_SEARCH_TOP_K=5              # Số sản phẩm tương đồng lấy về
RAG_SIMILARITY_THRESHOLD=0.3    # Ngưỡng cosine similarity (0-1)
```

### Config Ứng Dụng (`application-dev.yml`)

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

## Hướng Dẫn Setup

### 1. Khởi Động Infrastructure

```bash
# Start PostgreSQL + Ollama
docker-compose up -d db ollama

# Pull models (lần đầu tiên)
./scripts/setup-ollama.sh
```

**Kiểm tra models:**

```bash
docker exec ollama ollama list
# Kết quả mong đợi:
# llama3.2:latest
# nomic-embed-text:latest
```

### 2. Khởi Động Ứng Dụng

```bash
./mvnw spring-boot:run
```

### 3. Index Products (Chỉ Admin)

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

## Tham Chiếu API

### POST `/per/rag/chat`

**Chat với AI assistant (non-streaming)**

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
    "detailedResponse": "**Gợi Ý Chính:**\nDior Sauvage Eau de Parfum...",
    "products": [
      {
        "id": "uuid-123",
        "name": "Dior Sauvage EDP 100ml",
        "price": 3500000,
        "reasonForRecommendation": "Hương bergamot tươi mát hoàn hảo cho trời nóng"
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
curl -N http://localhost:8080/per/rag/chat/stream?question=Nước%20hoa%20mùa%20hè

# Response (text/event-stream):
data: Tôi gợi ý
data: Dior Sauvage
data: ...
```

### POST `/per/rag/index` (Admin)

**Index toàn bộ products vào vector store**

### POST `/per/rag/index/knowledge` (Admin)

**Index prompt templates từ `src/main/resources/prompt/`**

> **Lưu ý:** Hiện đã deprecated. Prompts được load trực tiếp từ classpath, không cần index.

### DELETE `/per/rag/knowledge` (Admin)

**Xóa knowledge base documents**

### GET `/per/rag/knowledge/status`

**Kiểm tra trạng thái indexing knowledge base**

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

## Kiến Trúc Prompt

### Vị Trí Files Prompt

```
src/main/resources/prompt/
├── system-prompt.txt           # Main RAG prompt (load qua classpath)
├── ai-assistant-instructions.md
├── customer-service-guide.md
├── fragrance-guide.md
├── policies.md
└── fallback-response.md
```

### Cấu Trúc Prompt

**system-prompt.txt** sử dụng:

- `{context}` - Thay bằng products đã retrieve
- `{question}` - Câu hỏi của user
- `{format}` - JSON schema từ BeanOutputConverter

**Ví dụ:**

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

### XML Tags trong Prompt Files (Reference)

Các file prompt reference (`.md`) dùng XML structure để AI hiểu rõ hơn:

```xml
<context type="ai_instructions">
  # Content (định dạng Markdown)
</context>

<rules>
  ## Rules và constraints
</rules>

<data type="fragrance_knowledge">
  ## Knowledge base content
</data>
```

---

## Xử Lý Sự Cố

### Vấn Đề: "No relevant products found"

**Nguyên nhân:** Threshold quá cao hoặc chưa index products

**Giải pháp:**

1. Check products đã index: `GET /per/rag/knowledge/status`
2. Giảm `RAG_SIMILARITY_THRESHOLD` xuống 0.2
3. Re-index: `POST /per/rag/index`

### Vấn Đề: "LLM trả về JSON không hợp lệ"

**Nguyên nhân:** Llama 3.2 wrap JSON trong markdown code blocks

**Giải pháp:** Method `cleanMarkdownCodeBlocks()` tự động loại bỏ ` ```json `

### Vấn Đề: "Products array rỗng dù có sản phẩm phù hợp"

**Nguyên nhân:** LLM filter quá strict theo seasonality/occasion trong prompt

**Giải pháp:** Điều chỉnh filtering rules trong `system-prompt.txt`

---

## Tối Ưu Performance

### Vector Search

- **Top-K**: Cao hơn = nhiều context hơn nhưng chậm hơn (mặc định: 5)
- **Threshold**: Thấp hơn = matching "dễ tính" hơn (mặc định: 0.3)

```yaml
app:
  rag:
    search-top-k: 10          # Nhiều products trong context
    similarity-threshold: 0.2  # Matching dễ tính hơn
```

### Thời Gian Response LLM

- **Model size**: llama3.2 (~2GB, inference nhanh)
- **Streaming**: Dùng `/chat/stream` cho perceived speed
- **GPU**: Ollama tự dùng GPU nếu có (nhanh gấp 10x)

---

## Câu Hỏi Thường Gặp

**Q: Có thể dùng GPT-4 thay Ollama không?**

A: Có, thay OllamaChatModel bằng OpenAiChatModel. Đổi `spring.ai.openai.api-key`.

**Q: Làm sao thêm custom prompts?**

A: Edit files trong `src/main/resources/prompt/`. Restart app (prompts load lúc runtime).

**Q: Tại sao dùng structured output thay plain text?**

A: Cho phép Generative UI - frontend tự động render ProductCards, CTAs, v.v.

**Q: Làm sao prevent hallucinations?**

A: Prompt nói rõ "KHÔNG invent products". BeanOutputConverter ép định dạng UUID.

---

## Bước Tiếp Theo

1. **Customize Prompts**: Edit `system-prompt.txt` cho domain của bạn
2. **Tune Parameters**: Điều chỉnh top-K và threshold theo catalog size
3. **Monitor Performance**: Check LLM response times qua logs
4. **A/B Test**: So sánh structured vs streaming cho UX

---

## Tài Liệu Liên Quan

- [English README](./README_en.md)
- [API Collection](../../postman/per-api-collection.json)
- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/)
