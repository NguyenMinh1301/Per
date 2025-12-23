# Elasticsearch Search

The application uses Elasticsearch for full-text search across Products, Brands, Categories, and Made In entities. Search features include fuzzy matching, prefix support, and multi-field filtering.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Search Architecture                                  │
│                                                                              │
│  User ──► GET /search ──► SearchService ──► Elasticsearch                   │
│                                                                              │
│  ServiceImpl ──► Kafka(index-topic) ──► IndexConsumer ──► Elasticsearch     │
│  (create/update/delete)                                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Supported Modules

| Module | Search Fields | Search Endpoint | Reindex Endpoint |
| --- | --- | --- | --- |
| Product | name, description, brandName, categoryName | `GET /per/products/search` | `POST /per/products/reindex` |
| Brand | name, description | `GET /per/brands/search` | `POST /per/brands/reindex` |
| Category | name, description | `GET /per/categories/search` | `POST /per/categories/reindex` |
| Made In | name, region, description | `GET /per/made-in/search` | `POST /per/made-in/reindex` |

## Key Features

| Feature | Description |
| --- | --- |
| Multi-field Search | Searches across multiple fields with relevance boosting |
| Fuzzy Matching | Finds results with typos (e.g., "savge" matches "Sauvage") |
| Prefix Matching | Partial input matches (e.g., "parf" matches "Parfum") |
| Relevance Scoring | Results sorted by relevance with field boosting |
| Real-time Sync | Data synchronized via Kafka for eventual consistency |

## Search Query Strategy

The search implementation combines multiple query types for optimal results:

1. **Prefix Query**: Highest priority for exact prefix matches
2. **Wildcard Query**: Flexible partial matching
3. **Fuzzy Multi-Match**: Typo tolerance across multiple fields

```
Query: "parf"
├── 1. Prefix: parf* → name (boost 3x)
├── 2. Wildcard: parf* → name (boost 2x)
├── 3. Fuzzy: parf → name, description (AUTO fuzziness)
└── 4. Wildcard: parf* → description
```

Fuzzy matching uses `fuzziness: AUTO`:
- 1-2 character words: exact match only
- 3-5 character words: 1 edit allowed
- 6+ character words: 2 edits allowed

## Product Search API

### Endpoint

```
GET /per/products/search
```

### Query Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `query` | String | Full-text search query |
| `brandId` | UUID | Filter by brand |
| `categoryId` | UUID | Filter by category |
| `gender` | Enum | MALE, FEMALE, UNISEX |
| `fragranceFamily` | Enum | WOODY, FLORAL, ORIENTAL, FRESH, etc. |
| `sillage` | Enum | SOFT, LIGHT, MODERATE, STRONG, HEAVY |
| `longevity` | Enum | SHORT, MODERATE, LONG_LASTING, VERY_LONG_LASTING |
| `seasonality` | Enum | SPRING, SUMMER, FALL, WINTER, ALL_SEASONS |
| `occasion` | Enum | DAILY, EVENING, FORMAL, CASUAL, PARTY |
| `minPrice` | BigDecimal | Minimum price filter |
| `maxPrice` | BigDecimal | Maximum price filter |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

### Example Requests

```bash
# Text search
curl "/per/products/search?query=dior"

# Fuzzy search (typo tolerance)
curl "/per/products/search?query=savge"

# Prefix search
curl "/per/products/search?query=parf"

# Search with filters
curl "/per/products/search?query=eau&gender=MALE&minPrice=100000"

# Filter only
curl "/per/products/search?brandId=xxx&fragranceFamily=WOODY"
```

## Brand, Category, Made In Search API

### Endpoint

```
GET /per/brands/search?q=<query>
GET /per/categories/search?q=<query>
GET /per/made-in/search?q=<query>
```

### Query Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `q` | String | Full-text search query |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

### Example Requests

```bash
# Brand search
curl "/per/brands/search?q=dior"

# Category search
curl "/per/categories/search?q=parfum"

# Made In search
curl "/per/made-in/search?q=france"
```

## Data Synchronization

Entities are synchronized to Elasticsearch via Kafka events:

### Event Flow

```
ServiceImpl ──► KafkaTemplate.send() ──► [index-topic]
                                               │
                                               ▼
                                         IndexConsumer
                                               │
                                               ▼
                                         SearchRepository.save()
```

### Kafka Topics

| Module | Topic |
| --- | --- |
| Product | `product-index-topic` |
| Brand | `brand-index-topic` |
| Category | `category-index-topic` |
| Made In | `made-in-index-topic` |

### Event Types

| Action | Trigger | Result |
| --- | --- | --- |
| INDEX | Entity created/updated | Document indexed |
| DELETE | Entity deleted | Document removed |

### Retry and DLQ

Failed index events follow the retry pattern:
- 3 retries with exponential backoff (1s, 2s, 4s)
- Failed events sent to Dead Letter Topic (`*-index-topic-dlt`)

## Auto-Reindex on Startup

The application can automatically reindex empty Elasticsearch indexes on startup:

- Enabled by default in development profile
- Disabled by default in production profile
- Configurable via environment variable

Only triggers when an index is empty, preventing unnecessary reindexing on normal restarts.

## Key Components

| File | Purpose |
| --- | --- |
| `*Document.java` | Elasticsearch document mapping |
| `*SearchRepository.java` | Spring Data Elasticsearch repository |
| `*DocumentMapper.java` | Entity to Document conversion |
| `*SearchService.java` | Search service interface |
| `*SearchServiceImpl.java` | Search implementation |
| `*IndexEvent.java` | Kafka event for index sync |
| `*IndexConsumer.java` | Kafka consumer for Elasticsearch sync |
| `ElasticsearchInitializer.java` | Auto-reindex on startup |

## Development

### Local Development

Start Elasticsearch via Docker Compose:

```bash
docker compose up -d elasticsearch
```

### Initial Indexing

After first startup, call reindex endpoints to populate Elasticsearch:

```bash
curl -X POST "/per/products/reindex"
curl -X POST "/per/brands/reindex"
curl -X POST "/per/categories/reindex"
curl -X POST "/per/made-in/reindex"
```

### Monitoring

Access Kibana at `http://localhost:5601` to:
- View indexes
- Analyze search queries
- Debug relevance scoring

## Extending Search

### Adding New Searchable Fields

1. Add field to `*Document.java`:
   ```java
   @Field(type = FieldType.Text, analyzer = "standard")
   private String newField;
   ```

2. Update `*DocumentMapper.java` to map the field

3. Add field to search query in `*SearchServiceImpl.java`:
   ```java
   .fields("name^3", "newField^2", ...)
   ```

4. Reindex all entities

### Adding New Filters

1. Add field to search request DTO

2. Add filter logic in `buildSearchQuery()`:
   ```java
   if (request.getNewFilter() != null) {
       boolQuery.filter(f -> f.term(t -> t.field("newField").value(...)));
   }
   ```
