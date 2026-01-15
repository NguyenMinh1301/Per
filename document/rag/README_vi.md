# Trợ Lý Mua Sắm AI - Tài Liệu Tiếng Việt

## Tổng Quan

Module Trợ Lý AI cung cấp gợi ý sản phẩm thông minh sử dụng công nghệ RAG (Retrieval-Augmented Generation), kết hợp tìm kiếm ngữ nghĩa với mô hình ngôn ngữ lớn (LLM) để trả lời câu hỏi khách hàng một cách chính xác và theo ngữ cảnh.

## Kiến Trúc

```
┌───────────────────────────────────────────────────────────────────────┐
│                      Luồng Xử Lý AI Assistant                         │
│                                                                       │
│  Câu Hỏi User → Tìm Kiếm Ngữ Nghĩa (PgVector) →                       │
│    Lấy Top-K Sản Phẩm → Xây Dựng Context → ChatService →             │
│    Tạo Prompt → Ollama LLM → Sinh Câu Trả Lời                        │
└───────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                      Luồng Indexing                                   │
│                                                                       │
│  Admin Kích Hoạt → VectorStoreService →                               │
│    Lấy Sản Phẩm → Chuyển Thành Documents → Embedding →               │
│    Lưu Vào PgVector                                                  │
└───────────────────────────────────────────────────────────────────────┘
```

## Thành Phần Chính

| File | Chức Năng |
| --- | --- |
| `RagController.java` | REST endpoints cho chat và indexing |
| `VectorStoreService.java` | Indexing sản phẩm và tìm kiếm ngữ nghĩa |
| `DocumentIndexService.java` | Indexing tài liệu markdown (FAQ, chính sách) |
| `ChatService.java` | Tổ chức pipeline RAG và sinh câu trả lời từ LLM |

## Công Nghệ Sử Dụng

| Component | Công Nghệ | Mục Đích |
| --- | --- | --- |
| Vector Database | PgVector (PostgreSQL extension) | Lưu trữ và tìm kiếm embeddings 768 chiều |
| Embedding Model | nomic-embed-text (Ollama) | Chuyển đổi text thành vectors |
| Chat Model | llama3.2 (Ollama) | Sinh câu trả lời ngôn ngữ tự nhiên |
| Framework | Spring AI | Tích hợp LLM và abstractions |

## Cấu Hình

### Biến Môi Trường

Thêm vào file `.env`:

```bash
# Ollama LLM Service
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_CHAT_MODEL=llama3.2
OLLAMA_EMBEDDING_MODEL=nomic-embed-text

# Tham Số RAG (tùy chọn)
RAG_SEARCH_TOP_K=5
RAG_SIMILARITY_THRESHOLD=0.3
```

## Hướng Dẫn Cài Đặt

### 1. Khởi Động Infrastructure

```bash
# Start PostgreSQL và Ollama
docker-compose up -d db ollama

# Kiểm tra Ollama đã sẵn sàng
docker ps | grep ollama

# Pull models (chỉ lần đầu tiên)
chmod +x scripts/setup-ollama.sh
./scripts/setup-ollama.sh
```

### 2. Xác Minh Models

```bash
docker exec ollama ollama list
# Output mong muốn:
# llama3.2          latest   ...
# nomic-embed-text  latest   ...
```

### 3. Khởi Động Application

```bash
./mvnw spring-boot:run
```

### 4. Index Sản Phẩm

Cần JWT token admin:

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
    "status": "Product catalog indexed successfully",
    "documentsIndexed": 0
  }
}
```

### 5. Index Knowledge Base (Tùy Chọn)

Để AI có thể trả lời câu hỏi về chính sách, hướng dẫn:

```bash
curl -X POST http://localhost:8080/per/rag/index/knowledge \
  -H "Authorization: Bearer <ADMIN_JWT_TOKEN>"
```

## REST API

### POST /per/rag/chat

Nhận gợi ý sản phẩm từ AI (không streaming).

**Auth:** Không cần  
**Rate Limit:** `highTraffic`

**Request:**
```json
{
  "question": "Nước hoa nào phù hợp cho mùa hè?"
}
```

**Response:**
```json
{
  "success": true,
  "code": "RAG_CHAT_SUCCESS",
  "data": {
    "answer": "Cho mùa hè, tôi gợi ý các hương nước hoa tươi mát...",
    "sourceDocuments": [
      "Dior Sauvage",
      "Versace Dylan Blue"
    ]
  }
}
```

### GET /per/rag/chat/stream

Stream câu trả lời AI real-time (Server-Sent Events).

**Auth:** Không cần  
**Query Params:**
- `question` (string, required)

**Example:**
```bash
curl -N "http://localhost:8080/per/rag/chat/stream?question=Nước%20hoa%20hoa%20cỏ"
```

### POST /per/rag/index

Rebuild vector database với tất cả sản phẩm active.

**Auth:** `hasRole('ADMIN')`

### POST /per/rag/index/knowledge

Index tất cả file markdown từ `document/rag/knowledge/`.

**Auth:** `hasRole('ADMIN')`

### DELETE /per/rag/knowledge

Xóa tất cả knowledge base documents khỏi vector store.

**Auth:** `hasRole('ADMIN')`

## Chiến Lược Indexing

### Product → Document

Mỗi sản phẩm active được chuyển thành `Document`:

**Content bao gồm:**
- Tên sản phẩm, thương hiệu, danh mục, xuất xứ
- Mô tả ngắn và mô tả đầy đủ
- Thuộc tính nước hoa (giới tính, họ hương, độ tỏa, độ bền, mùa, dịp)
- Khoảng giá (min-max từ variants)
- Dung tích có sẵn

**Metadata bao gồm:**
- `productId`, `productName`, `brandName`, `categoryName`
- `type: "product"` (để phân biệt với knowledge base)

### Knowledge Base → Document

File `.md` từ `document/rag/knowledge/` được chuyển thành `Document`:

**Metadata bao gồm:**
- `type: "knowledge"`
- `source: "filename.md"`
- `category: "general"` hoặc tên subdirectory
- Deterministic UUID từ filename

### Vector Dimensions

Embeddings là vectors 768 chiều phù hợp với model `nomic-embed-text`.

### Index Type

PgVector sử dụng HNSW (Hierarchical Navigable Small World) index cho tìm kiếm xấp xỉ nhanh với metric khoảng cách cosine.

## Knowledge Base

### Cấu Trúc Thư Mục

```
document/rag/knowledge/
├── customer-service-guide.md
├── policies.md
├── fragrance-guide.md
└── [custom-documents].md
```

### Cách Thêm Tài Liệu Mới

1. Tạo file `.md` trong `document/rag/knowledge/`
2. Viết nội dung bằng Markdown
3. Call `POST /per/rag/index/knowledge` với admin token
4. AI sẽ tự động sử dụng tài liệu khi trả lời

**Best Practices:**
- File ngắn gọn, tập trung một chủ đề
- Sử dụng heading rõ ràng
- Nội dung đúng format markdown chuẩn
- Tránh thông tin trùng lặp

### Ví Dụ Knowledge Base Document

`policies.md`:
```markdown
# Chính Sách Vận Chuyển

## Phí Ship
- Miễn phí cho đơn từ 2,000,000 VNĐ
- Đơn dưới 2M: 30,000 VNĐ

## Thời Gian Giao
- Nội thành HN, HCM: 24 giờ
- Tỉnh khác: 2-3 ngày
```

Sau khi index, AI có thể trả lời: "Chính sách vận chuyển của bạn như thế nào?"

## Luồng Tìm Kiếm Ngữ Nghĩa

1. **Embed Query**: Câu hỏi → nomic-embed-text → vector 768 chiều
2. **Search PgVector**: Tìm top-K documents tương tự (K=5) với similarity > threshold (0.3)
3. **Filter Results**: Chỉ giữ documents đạt similarity threshold
4. **Build Context**: Ghép nội dung documents với separator `\n\n---\n\n`
5. **Return**: Trích xuất `productName`/`source` từ metadata

## Luồng Sinh Câu Trả Lời

1. **Semantic Search**: Tìm documents liên quan
2. **Check Results**: Nếu không có → trả về "không tìm thấy thông tin"
3. **Build Prompt**: Thay `{context}` và `{question}` trong system prompt
4. **LLM Call**: Gửi prompt tới Ollama (streaming hoặc no streaming)
5. **Return**: Package answer + source documents

## Xử Lý Lỗi

| Error Code | HTTP Status | Nguyên Nhân |
| --- | --- | --- |
| `RAG_INDEXING_FAILED` | 500 | Lỗi database hoặc embedding khi indexing |
| `RAG_SEARCH_FAILED` | 500 | Lỗi vector search query |
| `RAG_CHAT_FAILED` | 500 | Lỗi LLM generation |
| `RAG_OLLAMA_UNAVAILABLE` | 503 | Ollama service không truy cập được |
| `RAG_KNOWLEDGE_INDEX_FAILED` | 500 | Lỗi indexing knowledge base |

## Monitoring và Debug

### Kiểm Tra Vector Data

```sql
-- Kết nối PostgreSQL
\c <database_name>

-- Đếm documents đã index
SELECT COUNT(*) FROM vector_store;

-- Xem sample documents
SELECT id, metadata->>'productName' as product,
       metadata->>'type' as type
FROM vector_store LIMIT 10;

-- Kiểm tra dimensions
SELECT id, array_length(embedding, 1) as dimensions
FROM vector_store LIMIT 1;

-- List knowledge base documents
SELECT metadata->>'source' as filename
FROM vector_store
WHERE metadata->>'type' = 'knowledge';
```

### Test Ollama Trực Tiếp

```bash
# Test chat model
curl http://localhost:11434/api/generate -d '{
  "model": "llama3.2",
  "prompt": "Gợi ý nước hoa cho mùa hè",
  "stream": false
}'

# Test embedding model
curl http://localhost:11434/api/embeddings -d '{
  "model": "nomic-embed-text",
  "prompt": "Dior Sauvage perfume"
}'
```

### Application Logs

Enable debug logging:

```yaml
logging:
  level:
    com.per.rag: DEBUG
    org.springframework.ai: DEBUG
```

## Tối Ưu Hiệu Năng

### Tham Số Tìm Kiếm

| Tham Số | Mặc Định | Ảnh Hưởng |
| --- | --- | --- |
| `search-top-k` | 5 | Cao hơn = nhiều context, chậm hơn |
| `similarity-threshold` | 0.3 | Thấp hơn = nhiều kết quả, kém liên quan |

**Khuyến nghị:**
- `top-k = 5-10` cho câu hỏi chung
- `threshold = 0.3-0.5` cho balance precision/recall
- Giảm threshold nếu không tìm thấy kết quả

### Chọn Model

Thay đổi chat model qua environment variable:

```bash
OLLAMA_CHAT_MODEL=mistral  # 7B params, reasoning tốt
OLLAMA_CHAT_MODEL=llama3   # 8B params, chất lượng cao
OLLAMA_CHAT_MODEL=gemma2   # 9B params, instruction following mạnh
```

Pull models: `docker exec ollama ollama pull <model-name>`

### Context Window

Ollama mặc định context: 2048 tokens. Điều chỉnh nếu cần:

```bash
docker exec ollama ollama show llama3.2 --modelfile
# Thêm: PARAMETER num_ctx 4096
```

## Bảo Mật

| Khía Cạnh | Triển Khai |
| --- | --- |
| Rate Limiting | `@RateLimiter(name = "highTraffic")` |
| Admin Endpoints | Indexing cần `ADMIN` role |
| Input Validation | `@NotBlank` trên question field |
| Prompt Injection | System prompt tách biệt context và user input |
| Public Access | Chat endpoints public (không cần auth) |

## Troubleshooting

### Ollama Không Phản Hồi

```bash
# Kiểm tra logs
docker logs ollama

# Verify service
curl http://localhost:11434/api/tags

# Restart nếu cần
docker-compose restart ollama
```

### Không Có Kết Quả Tìm Kiếm

Nguyên nhân:
- Sản phẩm chưa được index → gọi `/per/rag/index`
- Similarity threshold quá cao → giảm `RAG_SIMILARITY_THRESHOLD`
- Bảng `vector_store` rỗng → check SQL

### Chất Lượng Câu Trả Lời Kém

Cải thiện:
- Tăng `search-top-k` để nhiều context hơn
- Giảm `similarity-threshold` để nhiều candidates
- Cải thiện system prompt với instructions cụ thể hơn
- Dùng model lớn hơn (e.g., `llama3:70b`)

### Lỗi UUID Invalid

Nếu gặp `invalid UUID string` khi index knowledge base:
- Code đã sửa để generate deterministic UUID từ filename
- Restart app sau khi update code

## Testing

### Manual Testing

```bash
# 1. Index products
curl -X POST http://localhost:8080/per/rag/index \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 2. Index knowledge base
curl -X POST http://localhost:8080/per/rag/index/knowledge \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. Test product question
curl -X POST http://localhost:8080/per/rag/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Nước hoa cho nam giới?"}'

# 4. Test knowledge base question
curl -X POST http://localhost:8080/per/rag/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Chính sách đổi trả của bạn?"}'

# 5. Test streaming
curl -N "http://localhost:8080/per/rag/chat/stream?question=Gợi%20ý%20hương%20hoa"
```

## Operational Notes

- Ollama models lưu trong volume: `ollama_data:/root/.ollama`
- Vector data persist trong PostgreSQL: `db:/var/lib/postgresql/data`
- Pull model lần đầu mất 5-10 phút tùy network
- HNSW index tự build khi documents vượt threshold
- Rebuild index sau khi bulk update products

## FAQ

**Q: Làm sao để AI trả lời bằng tiếng Việt?**  
A: Thay đổi system prompt trong `application-dev.yml` sang tiếng Việt. Model `llama3.2` hỗ trợ multilingual.

**Q: Có thể dùng embedding model khác?**  
A: Có, thay `OLLAMA_EMBEDDING_MODEL`. Lưu ý phải match với dimensions trong DB (768 cho nomic-embed-text).

**Q: Knowledge base có giới hạn dung lượng?**  
A: Không giới hạn cứng, nhưng context window của LLM giới hạn (2048 tokens mặc định). Nên giữ mỗi document ngắn gọn.

**Q: Làm sao để disable knowledge base?**  
A: `DELETE /per/rag/knowledge` với admin token để xóa tất cả. Hoặc filter theo metadata `type != "knowledge"` trong code.

**Q: Performance impact khi index nhiều documents?**  
A: HNSW index hiệu quả với hàng triệu vectors. Indexing ban đầu chậm, nhưng search rất nhanh sau khi có index.

## Tham Khảo

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Ollama Models](https://ollama.com/library)
- [PgVector Documentation](https://github.com/pgvector/pgvector)
- [HNSW Index](https://arxiv.org/abs/1603.09320)
