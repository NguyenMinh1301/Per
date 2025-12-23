Elasticsearch Product Search
============================

The application uses Elasticsearch for advanced product search with full-text search, fuzzy matching, and multi-field filtering.

Architecture
------------

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Search Architecture                                  │
│                                                                             │
│  User ──► GET /products/search ──► ProductSearchService ──► Elasticsearch   │
│                                                                             │
│  ProductServiceImpl ──► Kafka(product-index-topic) ──► ProductIndexConsumer │
│       (create/update/delete)                              │                 │
│                                                           ▼                 │
│                                                    Elasticsearch            │
│                                                    (sync document)          │
└─────────────────────────────────────────────────────────────────────────────┘
```

Key Features
------------

| Feature | Description |
| --- | --- |
| **Multi-field Search** | Searches across name, description, brand, category |
| **Fuzzy Matching** | Finds results with typos (e.g., "savge" → "Sauvage") |
| **Relevance Scoring** | Results sorted by relevance with field boosting |
| **Filters** | Brand, category, gender, fragrance family, sillage, longevity, seasonality, occasion, price range |
| **Async Sync** | Data synced via Kafka for eventual consistency |

Key Components
--------------

| File | Purpose |
| --- | --- |
| `ProductDocument.java` | Elasticsearch document mapping |
| `ProductSearchRepository.java` | Spring Data ES repository |
| `ProductDocumentMapper.java` | Entity to Document conversion |
| `ProductSearchService.java` | Search service interface |
| `ProductSearchServiceImpl.java` | Search implementation with fuzzy queries |
| `ProductIndexEvent.java` | Kafka event for index sync |
| `ProductIndexConsumer.java` | Kafka consumer for ES sync |

API Endpoints
-------------

### Search Products

```
GET /products/search
```

**Query Parameters:**

| Parameter | Type | Description |
| --- | --- | --- |
| `query` | String | Full-text search query (fuzzy) |
| `brandId` | UUID | Filter by brand |
| `categoryId` | UUID | Filter by category |
| `gender` | Enum | MALE, FEMALE, UNISEX |
| `fragranceFamily` | Enum | WOODY, FLORAL, ORIENTAL, etc. |
| `sillage` | Enum | SOFT, LIGHT, MODERATE, STRONG, HEAVY |
| `longevity` | Enum | SHORT, MODERATE, LONG_LASTING, VERY_LONG_LASTING |
| `seasonality` | Enum | SPRING, SUMMER, FALL, WINTER, ALL_SEASONS |
| `occasion` | Enum | DAILY, EVENING, FORMAL, CASUAL, PARTY |
| `minPrice` | BigDecimal | Minimum price filter |
| `maxPrice` | BigDecimal | Maximum price filter |
| `isActive` | Boolean | Active status (default: true) |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

**Example Requests:**

```bash
# Simple text search
curl "/products/search?query=dior"

# Fuzzy search (typo tolerance)
curl "/products/search?query=savge"   # finds "Sauvage"

# Search with filters
curl "/products/search?query=eau&gender=MALE&minPrice=100000&maxPrice=2000000"

# Filter only (no text search)
curl "/products/search?brandId=xxx&fragranceFamily=WOODY"
```

**Response:**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "Dior Sauvage",
        "shortDescription": "A radically fresh composition...",
        "brandName": "Dior",
        "categoryName": "Eau de Parfum",
        "gender": "MALE",
        "minPrice": 2500000,
        "maxPrice": 4500000,
        "imageUrl": "https://..."
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

### Reindex All Products

```
POST /products/reindex
```

Admin operation to rebuild the Elasticsearch index from PostgreSQL.

```bash
curl -X POST "/products/reindex"
```

Search Relevance
----------------

Query results are ranked by relevance score with field boosting:

| Field | Boost | Priority |
| --- | --- | --- |
| `name` | 3x | Highest |
| `brandName` | 2x | High |
| `shortDescription` | 2x | High |
| `categoryName` | 1x | Normal |
| `description` | 1x | Normal |

**Fuzzy Matching:**

The search uses `fuzziness: AUTO` which allows:
- 1-2 character words: exact match
- 3-5 character words: 1 edit allowed
- 6+ character words: 2 edits allowed

Examples:
- `dior` → "Dior", "DIOR"
- `savge` → "Sauvage"
- `bluu` → "Bleu"

Data Synchronization
--------------------

Products are synced to Elasticsearch via Kafka events:

### Event Flow

```
ProductServiceImpl ──► KafkaTemplate.send() ──► [product-index-topic]
                                                        │
                                                        ▼
                                               ProductIndexConsumer
                                                        │
                                                        ▼
                                               ProductSearchRepository.save()
```

### Events

| Action | When | Result |
| --- | --- | --- |
| `INDEX` | Product created/updated | Document indexed |
| `DELETE` | Product deleted | Document removed |

### Retry & DLQ

Index events use the same retry pattern as email:
- 3 retries with exponential backoff (1s, 2s, 4s)
- Failed events go to `product-index-topic-dlt`

Configuration
-------------

### application.yml

```yaml
spring:
  elasticsearch:
    uris: ${ELASTICSEARCH_URI:http://localhost:9200}
```

### Environment Variables

| Variable | Default | Description |
| --- | --- | --- |
| `ELASTICSEARCH_URI` | `http://localhost:9200` | Elasticsearch server URL |

Development Notes
-----------------

### Local Development

Start Elasticsearch via Docker Compose:

```bash
docker compose up -d elasticsearch
```

### Initial Indexing

After starting the application, call reindex to populate ES:

```bash
curl -X POST http://localhost:8080/products/reindex
```

### Monitoring

Access Kibana at `http://localhost:5601` to:
- View product index
- Analyze search queries
- Debug relevance scoring

### Testing Search

```bash
# Test fuzzy matching
curl "/products/search?query=savge"

# Test filters
curl "/products/search?gender=MALE&fragranceFamily=WOODY"

# Test combined
curl "/products/search?query=fresh&gender=UNISEX&minPrice=500000"
```

Extending Search
----------------

### Adding New Searchable Fields

1. Add field to `ProductDocument.java`:
   ```java
   @Field(type = FieldType.Text, analyzer = "standard")
   private String newField;
   ```

2. Update `ProductDocumentMapper.java` to map the field

3. Add field to search query in `ProductSearchServiceImpl.java`:
   ```java
   .fields("name^3", "newField^2", ...)
   ```

4. Reindex all products

### Adding New Filters

1. Add field to `ProductSearchRequest.java`

2. Add filter logic in `ProductSearchServiceImpl.buildSearchQuery()`:
   ```java
   if (request.getNewFilter() != null) {
       boolQuery.filter(f -> f.term(t -> t.field("newField").value(...)));
   }
   ```
