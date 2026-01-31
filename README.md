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

## Introduction

Per is a production-ready e-commerce backend application built with Java 21 and Spring Boot 3.5. The system follows a modular monolith architecture, providing separation of concerns while maintaining deployment simplicity. It features JWT-based authentication, Elasticsearch full-text search, Redis caching with transactional consistency, event-driven processing via Kafka, AI-powered RAG (Retrieval-Augmented Generation) shopping assistant via OpenAI, and resilience patterns for external service integration.

<p align="center">
  <a href="./">
    <img src="/assets/img/Per.svg" alt="Per" width="160">
  </a>
</p>

## System Architecture

The application is architected as a **Modular Monolith** driven by Domain-Driven Design (DDD) principles. It leverages an event-driven backbone (Kafka) to synchronize state between the transactional core (PostgreSQL) and the search capability (Elasticsearch).

```mermaid
graph TD
    Client[Client Apps] -->|REST API| LB[Load Balancer]
    LB -->|HTTPS| Core[Spring Boot Core]
    
    subgraph "Data Persistence"
        Core -->|Transactional| DB[(PostgreSQL)]
        Core -->|Cache-Aside| Redis[(Redis)]
    end
    
    subgraph "Search & Analytics"
        Core -->|Query| ES[(Elasticsearch)]
        Core -.->|Event Pub| Kafka{Apache Kafka}
        Kafka -.->|Log Compacted| ES
    end
    
    subgraph "External Integration"
        Core -->|Upload| Cloudinary[Cloudinary]
        Core -->|Payment| PayOS[PayOS Gateway]
        Core -->|Email| SMTP[Google SMTP]
    end
```

### Core Capabilities

*   **Transactional Integrity**: ACID compliance via PostgreSQL for critical paths (Order, Payment).
*   **High-Performance Search**: CQRS implementation offloading complex queries to Elasticsearch.
*   **Resilience**: Circuit Breakers and Rate Limiters (Resilience4j) protecting against external outages.
*   **Scalability**: Stateless authentication (JWT) and distributed caching (Redis) enable horizontal scaling.

---

## Technical Stack

| Category | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Runtime** | Java | 21 (LTS) | Core Platform |
| **Framework** | Spring Boot | 3.5.x | Application Skeleton |
| **Database** | PostgreSQL | 16 | Primary Data Store |
| **Cache** | Redis | 7.x | L2 Cache & Session Store |
| **Search** | Elasticsearch | 7.x | Full-text Engine |
| **Messaging** | Kafka | 3.x | Event Bus |
## Getting Started

### Prerequisites

*   **Docker Desktop** (or Engine + Compose)
*   **Java 21**
*   **Maven**

### Infrastructure Provisioning

The entire dependent infrastructure (DB, Cache, Broker, Search) is containerized.

```bash
# Start all dependencies
docker-compose up -d

# Verify container status
docker-compose ps
```

### Configuration

Copy the example configuration to a local environment file.

```bash
cp .env.example .env
```
> **Note**: Populate critical secrets (Payment Keys, Cloudinary Credentials, SMTP credentials) in `.env` before starting the application.

### SMTP Configuration

The application uses Google SMTP for sending transactional emails (welcome, password reset, etc.).

#### Generate Google App Password

1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable **2-Step Verification** if not already enabled
3. Navigate to **App Passwords** ([direct link](https://myaccount.google.com/apppasswords))
4. Select **Mail** and your device, then click **Generate**
5. Copy the 16-character password

#### Environment Variables

```bash
# .env
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx  # App Password (not your Gmail password)
MAIL_FROM=your-gmail@gmail.com
```

> **Important**: Never use your regular Gmail password. App Passwords are required for SMTP access.

### Build & Run

```bash
# Verify code quality and build
./mvnw clean verify

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/per`.

---

## Developer Guide

### Quality Assurance

The project enforces strict code style guidelines using **Spotless**.

```bash
# Apply formatting
./mvnw spotless:apply

# Run Unit & Integration Tests
./mvnw test
```

### API Documentation

Interactive OpenAPI 3.0 documentation is available at:
`http://localhost:8080/per/api-docs`

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
