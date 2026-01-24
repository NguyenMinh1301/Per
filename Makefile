.PHONY: run stop clean

# Check if required tools are installed
CHECK_DEPS := $(shell command -v java >/dev/null 2>&1 && command -v docker >/dev/null 2>&1 && echo "ok" || echo "missing")
CHECK_ENV := $(shell [ -f .env ] && echo "ok" || echo "missing")

run:
	@if [ "$(CHECK_DEPS)" = "missing" ]; then \
		echo "Error: java or docker is not installed"; exit 1; \
	fi
	@if [ "$(CHECK_ENV)" = "missing" ]; then \
		echo "Error: .env file is missing"; exit 1; \
	fi
	@echo "Starting infra..."
	@docker compose up -d || docker-compose up -d
	@echo "Wait for database (5s)..."
	@sleep 5
	@echo "Starting application..."
	@./mvnw spring-boot:run

stop:
	@docker compose down || docker-compose down

clean:
	@./mvnw clean
	@docker compose down -v || docker-compose down -v
