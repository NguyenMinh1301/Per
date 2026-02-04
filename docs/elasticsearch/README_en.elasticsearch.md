# Search Engine (Elasticsearch)

## 1. Overview
**Elasticsearch** acts as the **Read Model** in our CQRS (Command Query Responsibility Segregation) architecture. It enables high-performance full-text search, filtering, and aggregation that PostgreSQL cannot handle efficiently.

## 2. Indexing Strategy

### Dual-Write Problem
We do **not** write to Elasticsearch in the same transaction as PostgreSQL. Instead, we use **Eventual Consistency** via Kafka.
1.  **Write**: `ProductService` saves to Postgres (Transactional).
2.  **Publish**: `ProductService` publishes `ProductIndexEvent`.
3.  **Consume**: `ProductIndexConsumer` receives event and updates Elasticsearch.

### Re-indexing
In case of data drift/corruption, we provide a **Reindex API** (Admin only) that:
1.  Iterates all records in PostgreSQL.
2.  Bulk upserts them into Elasticsearch.

## 3. Querying
We use **Spring Data Elasticsearch** repositories (`ProductSearchRepository`) for basic queries and `ElasticsearchClient` for complex aggregations.

### Analyzers
*   **Standard Analyzer**: Used for exact matches (IDs, Keys).
*   **Vietnamese Analyzer** (Optional): Can be configured for better VNmese text tokenization.

## 4. Document Structure
We flatten relational data into a single JSON document for speed.
*   **Product Index**: Contains nested `Brand` name, `Category` name, and `MadeIn` origin to avoid join-equivalent operations at query time.
