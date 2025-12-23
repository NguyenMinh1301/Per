<p align="center">
  <a href="https://spring.io/" target="_blank"><img src="document/img/springio.svg" height="100" alt="Spring Boot" /></a>
  &nbsp;&nbsp;
  <a href="https://www.postgresql.org/" target="_blank"><img src="document/img/postgresql.svg" height="100" alt="PostgreSQL" /></a>
  &nbsp;&nbsp;
  <a href="https://redis.io/" target="_blank"><img src="document/img/redis.svg" height="100" alt="Redis" /></a>
  &nbsp;&nbsp;
  <a href="https://kafka.apache.org/" target="_blank"><img src="document/img/kafka.svg" height="100" alt="Kafka" /></a>
  &nbsp;&nbsp;
  <a href="https://www.elastic.co/" target="_blank"><img src="document/img/elastic.svg" height="100" alt="Elasticsearch" /></a>
  &nbsp;&nbsp;
  <a href="https://www.docker.com/" target="_blank"><img src="document/img/docker.svg" height="100" alt="Docker" /></a>
</p>

## Introduction

Per is a production-ready e-commerce backend application built with Java 21 and Spring Boot 3.5. The system follows a modular monolith architecture, providing separation of concerns while maintaining deployment simplicity. It features JWT-based authentication, Elasticsearch full-text search, Redis caching with transactional consistency, event-driven processing via Kafka, and resilience patterns for external service integration.

<p align="center">
  <a href="./">
    <img src="/document/img/Per.svg" alt="Per" width="160">
  </a>
</p>

## Key Features

- **Full-Text Search**: Elasticsearch-powered search across Products, Brands, Categories, and Made In with fuzzy matching and prefix support.
- **Real-Time Sync**: Kafka-based event-driven synchronization between PostgreSQL and Elasticsearch for CUD operations.
- **JWT Authentication**: Access and refresh token rotation with Redis-backed session management. Supports registration, login, email verification, and password recovery.
- **Redis Caching**: Cache-Aside pattern with post-commit eviction ensures data consistency between cache and database.
- **Resilience Patterns**: Rate limiting and circuit breaker patterns protect endpoints from abuse and external service failures.
- **Payment Integration**: PayOS integration with webhook support for order checkout and payment status synchronization.
- **Media Management**: Cloudinary-backed file uploads with metadata persistence.
- **Database Migrations**: Flyway manages schema versioning across environments.
- **API Documentation**: OpenAPI/Swagger UI provides interactive API exploration.

## Technology Stack

### Core

| Technology | Version | Purpose |
| --- | --- | --- |
| Java | 21 | Language runtime |
| Spring Boot | 3.5.6 | Application framework |
| Spring Security | 6.x | Authentication and authorization |
| Spring Data JPA | 3.x | ORM and data access |
| Spring Data Elasticsearch | 3.x | Full-text search |

### Data and Messaging

| Technology | Version | Purpose |
| --- | --- | --- |
| PostgreSQL | 16 | Primary database |
| Redis | 7 | Caching and session storage |
| Elasticsearch | 7.x | Full-text search engine |
| Kafka | 3.9 | Asynchronous event processing |
| Flyway | 11.10 | Database migrations |

### External Services

| Technology | Purpose |
| --- | --- |
| Cloudinary | Media storage and delivery |
| PayOS | Payment gateway |
| SMTP | Email delivery |

### Infrastructure

| Technology | Purpose |
| --- | --- |
| Docker Compose | Local development environment |
| Maven | Build and dependency management |
| Spotless | Code formatting (Google Java Format) |
| JUnit 5 / Mockito | Testing framework |

## Architecture Overview

The application follows a modular monolith structure. Each domain module resides in `src/main/java/com/per` with its own controller, service, repository, and DTO layers.

| Module | Responsibility |
| --- | --- |
| `auth` | Authentication, JWT management, email verification |
| `user` | User profile and role management (admin) |
| `product` | Product catalog, variant inventory, search |
| `brand` | Brand master data, search |
| `category` | Product categorization, search |
| `made_in` | Product origin metadata, search |
| `cart` | Shopping cart management |
| `order` | Order snapshot and lifecycle |
| `payment` | PayOS integration and checkout |
| `media` | File upload and Cloudinary integration |
| `common` | Shared utilities, caching, resilience, configuration |

### Cross-Cutting Concerns

- **Search**: Elasticsearch indexes for Products, Brands, Categories, and Made In with real-time Kafka sync.
- **Caching**: Redis-based caching for products and master data with configurable TTL per cache type.
- **Rate Limiting**: Resilience4j rate limiters protect authentication, media, and API endpoints.
- **Circuit Breaker**: Prevents cascading failures when external services are unavailable.
- **Exception Handling**: Global exception handler provides consistent API error responses.

## Getting Started

### Prerequisites

- Java 21 SDK
- Docker and Docker Compose
- Maven (optional, wrapper included)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/NguyenMinh1301/Per.git
   cd Per
   ```

2. Configure environment variables:
   ```bash
   cp .env.example .env
   ```
   Update `.env` with your configuration values.

3. Start infrastructure services:
   ```bash
   docker-compose up -d
   ```

4. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Endpoints

| Endpoint | Description |
| --- | --- |
| `http://localhost:8080/per` | API base URL |
| `http://localhost:8080/per/api-docs` | Swagger UI |

### Search API

| Module | Search Endpoint | Reindex Endpoint |
| --- | --- | --- |
| Product | `GET /per/products/search` | `POST /per/products/reindex` |
| Brand | `GET /per/brands/search` | `POST /per/brands/reindex` |
| Category | `GET /per/categories/search` | `POST /per/categories/reindex` |
| Made In | `GET /per/made-in/search` | `POST /per/made-in/reindex` |

## Development

### Code Formatting

The project uses Spotless with Google Java Format:
```bash
./mvnw spotless:apply
```

### Running Tests

```bash
./mvnw test
```

### Build Validation

```bash
./mvnw clean verify
```

## Documentation

Detailed documentation is available in the `document/` directory:

- [Elasticsearch Search](document/elasticsearch/README_en.md) - Full-text search implementation
- [Cache Module](document/cache/README_en.md) - Redis caching strategy and implementation
- [Kafka Messaging](document/kafka/README_en.md) - Asynchronous event processing
- [Rate Limiting](document/resilience-patterns/rate-limit/README_en.md) - Request quota management
- [Circuit Breaker](document/resilience-patterns/circuit-breaker/README_en.md) - External service protection
- [Module](document/module) - External service protection

Module-specific documentation is available under `document/module/`.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
