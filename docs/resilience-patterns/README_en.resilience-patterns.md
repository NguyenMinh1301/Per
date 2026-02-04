# Resilience Patterns

## 1. Overview
In a distributed modular monolith, failures in one module or external service should not cascade to crash the entire system. We apply standard resilience patterns to ensure stability.

## 2. Rate Limiting (`@RateLimiter`)

### Purpose
Protects the API from abuse and DDoS attacks.
-   **Implementation**: Resilience4j using Redis/In-memory.
-   **Scope**: Applied to public endpoints like `/auth/login` (to prevent brute force) and generic public APIs.
-   **Config**: limit-refreshes every 1s, max 10 requests per user (customizable in `application.yml`).

## 3. Circuit Breaker (`@CircuitBreaker`)

### Purpose
Fails fast when a downstream service is struggling, preventing resource exhaustion.
-   **Target**:
    -   **PayOS**: If payment gateway times out repeatedly.
    -   **Cloudinary**: If media upload fails consistently.
    -   **AI Service (OpenAI/Qdrant)**: If RAG service is down.

### Fallbacks
When a circuit opens, we provide a **Fallback**:
-   **Search**: If ES is down -> Fallback to basic DB search (LIKE query) or return empty with "Service Degradation" warning.
-   **AI Chat**: If AI is down -> Return "I'm currently resting, please browse the catalog manually."

## 4. Bulkhead
Isolates thread pools for different resource-intensive tasks (e.g., Image Processing vs Email Sending) so that one full pool doesn't block the other.
