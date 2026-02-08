#!/bin/bash

set -e

CONNECT_URL="${KAFKA_CONNECT_URL:-http://localhost:8083}"
CONNECTOR_NAME="per-postgres-connector"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

wait_for_connect() {
    log_info "Waiting for Kafka Connect to be ready at ${CONNECT_URL}..."
    local max_attempts=30
    local attempt=0
    
    until curl -s "${CONNECT_URL}/connectors" > /dev/null 2>&1; do
        attempt=$((attempt + 1))
        if [ $attempt -ge $max_attempts ]; then
            log_error "Kafka Connect not available after ${max_attempts} attempts"
            exit 1
        fi
        echo -n "."
        sleep 2
    done
    echo ""
    log_info "Kafka Connect is ready!"
}

check_status() {
    log_info "Checking connector status..."
    local status
    status=$(curl -s "${CONNECT_URL}/connectors/${CONNECTOR_NAME}/status" 2>/dev/null)
    
    if [ -z "$status" ] || echo "$status" | grep -q "error_code"; then
        log_warn "Connector '${CONNECTOR_NAME}' not found or error occurred"
        echo "$status" | jq . 2>/dev/null || echo "$status"
        return 1
    else
        echo "$status" | jq .
        return 0
    fi
}

delete_connector() {
    log_warn "Deleting connector '${CONNECTOR_NAME}'..."
    local response
    response=$(curl -s -X DELETE "${CONNECT_URL}/connectors/${CONNECTOR_NAME}" 2>/dev/null)
    
    if [ -z "$response" ]; then
        log_info "Connector deleted successfully"
    else
        echo "$response" | jq . 2>/dev/null || echo "$response"
    fi
}

register_connector() {
    log_info "Registering Debezium connector '${CONNECTOR_NAME}'..."
    
    if curl -s "${CONNECT_URL}/connectors/${CONNECTOR_NAME}" 2>/dev/null | grep -q "\"name\""; then
        log_warn "Connector already exists. Use --delete first to recreate."
        check_status
        return 0
    fi

    local response
    response=$(curl -s -X POST "${CONNECT_URL}/connectors" \
        -H "Content-Type: application/json" \
        -d @"${SCRIPT_DIR}/connector-postgres.json")
    
    if echo "$response" | grep -q "error_code"; then
        log_error "Failed to register connector:"
        echo "$response" | jq . 2>/dev/null || echo "$response"
        exit 1
    fi
    
    log_info "Connector registered successfully!"
    
    sleep 3
    check_status
}

case "${1:-}" in
    --delete)
        wait_for_connect
        delete_connector
        ;;
    --status)
        wait_for_connect
        check_status
        ;;
    --help|-h)
        echo "Usage: $0 [--delete|--status|--help]"
        echo ""
        echo "Options:"
        echo "  (none)    Register the Debezium connector"
        echo "  --delete  Delete the existing connector"
        echo "  --status  Check connector status"
        echo "  --help    Show this help message"
        echo ""
        echo "Environment Variables:"
        echo "  KAFKA_CONNECT_URL  Kafka Connect REST API URL (default: http://localhost:8083)"
        ;;
    *)
        wait_for_connect
        register_connector
        ;;
esac
