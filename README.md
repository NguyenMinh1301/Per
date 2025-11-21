# Per E-commerce Backend

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Latest-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Security-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)

## 📖 Introduction

**Per** is a robust, high-performance E-commerce backend application engineered with **Java 21** and **Spring Boot 3.5**. Designed with a **Modular Monolith** architecture, it ensures scalability, maintainability, and clean separation of concerns. This project demonstrates a production-ready implementation of modern backend standards, featuring secure authentication, efficient data management, and seamless third-party integrations.

## 🚀 Key Features

*   **Advanced Authentication**: Secure JWT-based authentication with Access/Refresh token rotation, Redis-backed token management, and comprehensive flows for login, registration, and password recovery.
*   **Modular Architecture**: Distinct modules for Auth, User, Product, Order, Payment, Cart, and more, promoting code decoupling and domain-driven design.
*   **High-Performance Caching**: Integrated **Redis** caching for session management and frequently accessed data to minimize database load.
*   **Database Migrations**: Automated schema management using **Flyway** ensures consistent database states across environments.
*   **Payment Integration**: Seamless payment processing via **PayOS**.
*   **Media Management**: Cloud-based asset storage and optimization using **Cloudinary**.
*   **Email Services**: Asynchronous email delivery system with **MailHog** support for local development testing.
*   **API Documentation**: Auto-generated, interactive API documentation with **OpenAPI/Swagger**.

## 🛠 Technology Stack

### Core & Frameworks
*   **Language**: Java 21
*   **Framework**: Spring Boot 3.5.6
*   **Security**: Spring Security, JJWT (0.11.5)
*   **ORM**: Spring Data JPA (Hibernate)

### Data & Storage
*   **Database**: PostgreSQL 16
*   **Caching**: Redis (Lettuce Client)
*   **Migration**: Flyway 11.10.0
*   **Cloud Storage**: Cloudinary

### DevOps & Tools
*   **Containerization**: Docker, Docker Compose
*   **Build Tool**: Maven
*   **Code Quality**: Spotless (Google Java Format), JaCoCo (Code Coverage)
*   **Testing**: JUnit 5, Mockito, Testcontainers
*   **API Documentation**: SpringDoc OpenAPI (Swagger UI)

## 🏗 Architecture Overview

The project follows a **Modular Monolith** approach. Each domain is encapsulated within its own package in `src/main/java/com/per`, ensuring that business logic remains cohesive.

*   **`auth`**: Authentication, token management, and security configurations.
*   **`user`**: User profile management and role-based access control.
*   **`product`**: Product catalog, inventory, and categorization.
*   **`brand`**: Brand management and association with products.
*   **`category`**: Product category hierarchy and management.
*   **`made_in`**: Product origin/manufacturing location management.
*   **`order`**: Order processing, status tracking, and history.
*   **`payment`**: Payment gateway integration (PayOS) with webhook support.
*   **`cart`**: Shopping cart management.
*   **`media`**: Image and file upload handling.

## ⚡ Getting Started

### Prerequisites
Ensure you have the following installed on your local machine:
*   **Java 21 SDK**
*   **Docker & Docker Compose**
*   **Maven** (optional, wrapper included)

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/NguyenMinh1301/Per.git
    cd Per
    ```

2.  **Configure Environment Variables**
    Duplicate the `.env.example` file to create `.env` and populate it with your credentials.
    ```bash
    cp .env.example .env
    ```
    *Ensure you configure your PostgreSQL, Redis, Mail, and Cloudinary credentials in the `.env` file.*

3.  **Start Infrastructure**
    Use Docker Compose to spin up the required services (PostgreSQL, Redis, MailHog).
    ```bash
    docker-compose up -d
    ```

4.  **Build and Run the Application**
    ```bash
    ./mvnw spring-boot:run
    ```
    *Alternatively, you can build the JAR and run it:*
    ```bash
    ./mvnw clean package -DskipTests
    java -jar target/per-0.0.3.jar
    ```

### Accessing the Application
*   **API Base URL**: `http://localhost:8080/api/v1`
*   **Swagger UI**: `http://localhost:8080/swagger-ui.html`
*   **MailHog UI**: `http://localhost:8025` (For viewing test emails)

## 🧪 Development Workflow

### Code Formatting
This project uses **Spotless** with **Google Java Format** to maintain code consistency.
```bash
./mvnw spotless:apply
```

### Running Tests
Execute the test suite to ensure system stability.
```bash
./mvnw test
```
*Code coverage reports are generated by JaCoCo.*

## 📂 Project Structure

```
Per/
├── document/               # Module-specific documentation
├── src/
│   ├── main/
│   │   ├── java/com/per/
│   │   │   ├── auth/       # Authentication Module
│   │   │   ├── product/    # Product Module
│   │   │   ├── order/      # Order Module
│   │   │   └── ...
│   │   └── resources/
│   │       ├── db/migration/ # Flyway SQL Migrations
│   │       └── application.yml
├── docker-compose.yml      # Infrastructure orchestration
├── pom.xml                 # Maven dependencies
└── README.md               # Project documentation
```

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
**Author**: Nguyen Minh
