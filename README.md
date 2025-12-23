# Per E-commerce Backend

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-3.9-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)

<p align="center">
  <a href="./">
    <img src="/document/Per.svg" alt="Per" width="160">
  </a>
</p>

## Introduction

Per is a production-ready e-commerce backend application built with Java 21 and Spring Boot 3.5. The system follows a modular monolith architecture, providing clean separation of concerns while maintaining deployment simplicity. It features JWT-based authentication, Redis caching with transactional consistency, event-driven email processing via Kafka, and resilience patterns for external service integration.

## Key Features

- **JWT Authentication**: Access and refresh token rotation with Redis-backed session management. Supports registration, login, email verification, and password recovery flows.
- **Redis Caching**: Cache-Aside pattern with post-commit eviction ensures data consistency between cache and database under concurrent writes.
- **Event-Driven Email**: Asynchronous email delivery via Kafka decouples authentication flows from SMTP latency.
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

### Data and Messaging

| Technology | Version | Purpose |
| --- | --- | --- |
| PostgreSQL | 16 | Primary database |
| Redis | 7 | Caching and session storage |
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
| `product` | Product catalog and variant inventory |
| `brand` | Brand master data |
| `category` | Product categorization |
| `made_in` | Product origin metadata |
| `cart` | Shopping cart management |
| `order` | Order snapshot and lifecycle |
| `payment` | PayOS integration and checkout |
| `media` | File upload and Cloudinary integration |
| `common` | Shared utilities, caching, resilience, configuration |

### Cross-Cutting Concerns

- **Caching**: Redis-based caching for products and master data with configurable TTL per cache type.
- **Rate Limiting**: Resilience4j rate limiters protect authentication and media endpoints.
- **Circuit Breaker**: Prevents cascading failures when Cloudinary or other external services are unavailable.
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
   Update `.env` with your PostgreSQL, Redis, Kafka, Mail, Cloudinary, and PayOS credentials.

3. Start infrastructure services:
   ```bash
   docker-compose up -d
   ```

4. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Alternatively, build and run the JAR:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/per-0.0.3.jar
   ```

### Endpoints

| Endpoint | Description |
| --- | --- |
| `http://localhost:8080/api/v1` | API base URL |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI |

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

## Project Structure

```
Per/
├── document/                    # Documentation
│   ├── cache/                   # Cache module docs
│   ├── kafka/                   # Kafka messaging docs
│   ├── resilience-patterns/     # Rate limiting, circuit breaker docs
│   └── module/                  # Per-module documentation
├── src/main/
│   ├── java/com/per/
│   │   ├── auth/                # Authentication module
│   │   ├── product/             # Product catalog module
│   │   ├── order/               # Order management module
│   │   ├── payment/             # Payment integration module
│   │   ├── common/              # Shared components
│   │   │   ├── cache/           # Redis caching infrastructure
│   │   │   ├── config/          # Application configuration
│   │   │   └── exception/       # Global exception handling
│   │   └── ...
│   └── resources/
│       ├── db/migration/        # Flyway SQL migrations
│       └── application.yml      # Application configuration
├── docker-compose.yml           # Infrastructure services
├── pom.xml                      # Maven dependencies
└── README.md
```

## Documentation

Detailed documentation is available in the `document/` directory:

- [Cache Module](document/cache/README_en.md) - Redis caching strategy and implementation
- [Kafka Messaging](document/kafka/README_en.md) - Asynchronous event processing
- [Rate Limiting](document/resilience-patterns/rate-limit/README_en.md) - Request quota management
- [Circuit Breaker](document/resilience-patterns/circuit-breaker/README_en.md) - External service protection

Module-specific documentation is available under `document/module/`.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

**Author**: Nguyen Minh
