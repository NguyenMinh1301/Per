# Spring Auth Boilerplate

## Overview
This project provides a production-ready Spring Boot boilerplate for authentication-centric services. It includes JWT-based security, user lifecycle flows (registration, login, logout, refresh, email verification, forgot/reset password), role-based authorization, and an admin-only user management module with pagination and search. The infrastructure is ready for PostgreSQL, Redis, SMTP mail delivery, Flyway migrations, and containerised deployments.

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?logo=springboot&logoColor=white)]()
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)]()
[![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)]()
[![JWT](https://img.shields.io/badge/JWT-Security-000000?logo=jsonwebtokens&logoColor=white)]()
[![Spotless](https://img.shields.io/badge/Code%20Style-Spotless-1f6feb)]()
[![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)]()
[![Flyway](https://img.shields.io/badge/Flyway-FF0000?logo=flyway&logoColor=white)]()
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)]()

## Key Features
- **JWT Security**: Access tokens (15 minutes) and refresh tokens (30 days) secured with HS256, refresh tokens persisted in Redis.
- **Auth Flows**: Sign-up, login, logout, email verification, forgot password, reset password, refresh token exchange.
- **Email Delivery**: SMTP integration with templated verification and password reset emails (MailHog for local testing or Gmail app password for production).
- **Role Management**: Built-in `ADMIN` and `USER` roles with Flyway seeding.
- **User Module**: Admin-only CRUD and search (username, email, first name, last name) with pageable responses.
- **API Response Contract**: Unified success/error envelopes backed by enumerated codes.
- **Database Migrations**: Flyway migrations manage schema evolution (UUID-based users, relational roles, verification tokens).
- **Docker-ready**: Compose file for PostgreSQL/Redis/MailHog; standalone Dockerfile for the Spring service.

## Technology Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.x (Web, Security, Data JPA, Validation, Mail)
- **Database**: PostgreSQL (Flyway migrations)
- **Cache / Token Store**: Redis
- **Messaging**: SMTP (MailHog by default)
- **Build Tool**: Maven (wrapper included)
- **JWT**: JJWT 0.11.5
- **Containerisation**: Docker, Docker Compose

## Project Structure
```
auth/
├── README.md
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.auth
│   │   │       ├── AuthApplication.java
│   │   │       ├── auth                     # Authentication domain
│   │   │       │   ├── configuration        # Spring config, security, properties
│   │   │       │   ├── controller           # Auth REST endpoints
│   │   │       │   ├── dto                  # Auth request/response DTOs
│   │   │       │       └── request / response
│   │   │       │   ├── entity               # JPA entities (User, Role, UserToken)
│   │   │       │   ├── mapper               # Mapping utilities
│   │   │       │   ├── repository           # Auth repositories
│   │   │       │   ├── security             # JWT filter, service, principals
│   │   │       │   └── service              # Auth service layer + mail/token helpers
│   │   │       ├── common                   # Shared constants, responses, exceptions
│   │   │       └── user                     # Admin user management module
│   │   │           ├── controller
│   │   │           ├── dto                  # User module DTOs
│   │   │           ├── mapper
│   │   │           ├── repository
│   │   │           └── service
│   │   └── resources
│   │       ├── application.yml              # Externalised configuration
│   │       └── db/migration                 # Flyway migrations
│   └── test                                 # Spring Boot smoke tests
└── .env.example                             # Environment variable template
```

## Getting Started
### Prerequisites
- Java 21 (Temurin recommended)
- Maven 3.9+ (or use the bundled wrapper `./mvnw`)
- Docker & Docker Compose (optional, required for containerised dev)

### Environment Variables
Copy `.env.example` to `.env` and adjust as needed.
- **Database**: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- **Redis**: `REDIS_HOST`, `REDIS_PORT` (and `REDIS_PASSWORD` if applicable)
- **JWT**: `JWT_SECRET` (base64-encoded 256-bit key), `ACCESS_TOKEN_TTL`, `REFRESH_TOKEN_TTL`, `JWT_ISSUER`
- **Mail**: Configure MailHog (local) or SMTP credentials (e.g., Gmail app password)
- **Application**: `SERVER_PORT`, `CORS_ALLOWED_ORIGINS`, `APP_BASE_URL`
- **PayOS**: `PAY_OS_CLIENT_ID`, `PAY_OS_API_KEY`, `PAY_OS_CHECKSUM_KEY`

### Running Locally (with Docker Compose)
```bash
docker compose up -d
./mvnw spring-boot:run
```
Docker Compose provisions PostgreSQL, Redis, and MailHog:
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- MailHog UI: `http://localhost:8025`

### Running Locally (without Docker)
Provision PostgreSQL, Redis, and an SMTP server manually, then:
```bash
./mvnw spring-boot:run
```

### Building & Testing
```bash
./mvnw clean verify
```

### Packaging
```bash
./mvnw clean package -DskipTests
```
The runnable JAR is produced at `target/auth-0.0.1-SNAPSHOT.jar`.

## API Highlights
| Module | Endpoint | Description |
|--------|----------|-------------|
| Auth | `POST /api/v1/auth/register` | User registration |
|      | `POST /api/v1/auth/login` | Login with username/email + password |
|      | `POST /api/v1/auth/refresh` | Rotate access/refresh tokens |
|      | `POST /api/v1/auth/logout` | Revoke refresh token |
|      | `POST /api/v1/auth/verify-email` / `GET /api/v1/auth/verify-email?token=` | Email verification |
|      | `POST /api/v1/auth/forgot-password` | Issue reset email |
|      | `POST /api/v1/auth/reset-password` / `GET /api/v1/auth/reset-password?token=` | Reset password flow |
| User (Admin only) | `GET /api/v1/users` | Paginated list |
|      | `GET /api/v1/users/search?q=` | Search by username/email/first name/last name |
|      | `GET /api/v1/users/{id}` | Retrieve user |
|      | `POST /api/v1/users` | Create user |
|      | `PUT /api/v1/users/{id}` | Update user |
|      | `DELETE /api/v1/users/{id}` | Delete user |
| Payment | `POST /api/v1/payments/checkout` | Create PayOS checkout link from cart items |
|         | `POST /api/v1/payments/payos/webhook` | PayOS webhook endpoint |
|         | `GET /api/v1/payments/payos/return` | PayOS return URL (status summary) |

All responses follow the unified `ApiResponse` contract with standardised `code` values defined in `ApiSuccessCode` and `ApiErrorCode`.

## Deployment with Docker
Build the application image:
```bash
docker build -t auth-service:latest .
```
Run with environment variables (example using `.env`):
```bash
docker run --env-file .env --network host auth-service:latest
```
Ensure PostgreSQL, Redis, and SMTP services are reachable from the container.

## Flyway Migrations
Database migrations run automatically on startup. The baseline migration `V1__init_schema.sql` seeds roles (`ADMIN`, `USER`). Additional migrations should follow semantic naming (`V2__description.sql`).

## Mail Configuration
- **Local**: Use MailHog via Docker Compose (default `.env` settings).
- **Production**: Supply SMTP credentials (e.g., Gmail) and enable `MAIL_SMTP_AUTH=true`, `MAIL_SMTP_STARTTLS=true`, `MAIL_SMTP_SSL_TRUST=smtp.gmail.com`.

## Refresh Token Policy
Refresh tokens are persisted per token in Redis, enabling multi-device sessions. Logout or refresh will revoke the specific token supplied.
