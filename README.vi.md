<p align="center">
   <a href="https://spring.io/" target="_blank"><img src="assets/img/springio.svg" height="140" alt="Spring Boot" /></a>
  &nbsp;&nbsp;
   <a href="https://www.postgresql.org/" target="_blank"><img src="assets/img/postgresql.svg" height="140" alt="PostgreSQL" /></a>
  &nbsp;&nbsp;
   <a href="https://redis.io/" target="_blank"><img src="assets/img/redis.svg" height="140" alt="Redis" /></a>
  &nbsp;&nbsp;
   <a href="https://kafka.apache.org/" target="_blank"><img src="assets/img/kafka.svg" height="140" alt="Kafka" /></a>
  &nbsp;&nbsp;
   <a href="https://www.elastic.co/" target="_blank"><img src="assets/img/elastic.svg" height="140" alt="Elasticsearch" /></a>
  &nbsp;&nbsp;
   <a href="https://www.docker.com/" target="_blank"><img src="assets/img/docker.svg" height="140" alt="Docker" /></a>
  &nbsp;&nbsp;
   <a href="https://prometheus.io/" target="_blank"><img src="assets/img/prometheus.svg" height="140" alt="Prometheus" /></a>
  &nbsp;&nbsp;
</p>

# Per - High-Performance E-Commerce Backend

[![English Version](https://img.shields.io/badge/Language-English-blue)](README.md)

**Per** là một hệ thống thương mại điện tử Modular Monolith sẵn sàng cho production (production-ready), được xây dựng với Java 21, Spring Boot 3.5 và kiến trúc hướng sự kiện (Event-Driven Architecture) mạnh mẽ.

## 📚 Module Documentation

| Module | Purpose | Tech Stack |
| :--- | :--- | :--- |
| **[Common](docs/modules/common/README.vi.md)** | Shared Utilities, Global Exceptions | Spring Web, Resilience4j |
| **[Auth](docs/modules/auth/README.vi.md)** | Security, JWT, Role Management | Spring Security 6, JJWT |
| **[User](docs/modules/user/README.vi.md)** | Identity & Profile Management | JPA, PostgreSQL |
| **[Product](docs/modules/product/README.vi.md)** | Catalog, Elasticsearch Sync | Elastic, Kafka |
| **[Media](docs/modules/media/README.vi.md)** | Cloudinary Asset Management | Cloudinary SDK |
| **[Cart](docs/modules/cart/README.vi.md)** | Persistent Shopping Cart | JPA (Persistent) |
| **[Order](docs/modules/order/README.vi.md)** | Transaction Lifecycle | State Machine |
| **[Payment](docs/modules/payment/README.vi.md)** | Gateways & Webhooks | PayOS SDK |
| **[RAG](docs/modules/rag/README.vi.md)** | AI Shopping Assistant | Spring AI, Qdrant |

## 🚀 Quick Start

### Prerequisites
*   Java 21
*   Docker Desktop

### Run Infrastructure
```bash
docker-compose up -d
```

### Run Application
```bash
./mvnw spring-boot:run
```

## 🏗 System Architecture

Ứng dụng tuân theo mô hình **Modular Monolith** với tính nhất quán **Event-Driven** theo mặc định.

```mermaid
graph TD
    Client[Client Apps] -->|REST API| Core[Spring Boot Core]
    
    subgraph "Data & Messaging"
        Core -->|Transactional| DB[(PostgreSQL)]
        Core -->|Cache| Redis[(Redis)]
        Core -->|Event Pub| Kafka{Kafka}
    end
    
    subgraph "Search & AI"
        Kafka -->|Consumer| Core
        Core -->|Query| ES[(Elasticsearch)]
        Core -->|RAG| Qdrant[(Qdrant Vector DB)]
    end
```

## License
MIT License
