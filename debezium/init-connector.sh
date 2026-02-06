#!/bin/bash
# ============================================================
# Init Kafka Connect - Force Update Connector Configuration
# ============================================================
# This script waits for Kafka Connect to be ready and then
# FORCE UPDATES the Debezium connector config using PUT.
# This ensures new config is ALWAYS applied, even if a
# broken connector already exists.
# ============================================================

set -e

KAFKA_CONNECT_URL="${KAFKA_CONNECT_URL:-http://debezium-kafka-connect:8083}"
CONNECTOR_CONFIG="${CONNECTOR_CONFIG:-/config/connector-postgres.json}"
MAX_RETRIES="${MAX_RETRIES:-60}"
RETRY_INTERVAL="${RETRY_INTERVAL:-5}"

log_info() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] $1"; }
log_error() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [ERROR] $1" >&2; }
log_success() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [SUCCESS] $1"; }

wait_for_kafka_connect() {
    log_info "Waiting for Kafka Connect at $KAFKA_CONNECT_URL..."
    
    for i in $(seq 1 $MAX_RETRIES); do
        if curl -sf "$KAFKA_CONNECT_URL/connectors" > /dev/null 2>&1; then
            log_success "Kafka Connect is ready!"
            return 0
        fi
        log_info "Attempt $i/$MAX_RETRIES - Kafka Connect not ready, waiting ${RETRY_INTERVAL}s..."
        sleep $RETRY_INTERVAL
    done
    
    log_error "Kafka Connect did not become ready after $MAX_RETRIES attempts"
    return 1
}

get_connector_name() {
    jq -r '.name' "$CONNECTOR_CONFIG"
}

force_update_connector() {
    local name=$(get_connector_name)
    
    # Extract config to a temp file to avoid shell escaping issues
    jq '.config' "$CONNECTOR_CONFIG" > /tmp/connector_payload.json
    
    log_info "Force updating connector '$name' configuration..."
    log_info "Using PUT to ensure config is applied even if connector exists"
    
    # Log the key config values for verification
    local pub_mode=$(jq -r '."publication.autocreate.mode" // "not set"' /tmp/connector_payload.json)
    log_info "Applying config with publication.autocreate.mode = $pub_mode"
    
    # PUT /connectors/{name}/config - Creates OR Updates connector
    # This is idempotent and will overwrite any existing config
    local response
    local http_code
    
    http_code=$(curl -s -o /tmp/response.txt -w "%{http_code}" -X PUT \
        -H "Content-Type: application/json" \
        -d @/tmp/connector_payload.json \
        "$KAFKA_CONNECT_URL/connectors/$name/config")
    
    response=$(cat /tmp/response.txt 2>/dev/null || echo "")
    
    case $http_code in
        200)
            log_success "Connector '$name' updated successfully!"
            ;;
        201)
            log_success "Connector '$name' created successfully!"
            ;;
        *)
            log_error "Failed to update connector. HTTP $http_code"
            log_error "Response: $response"
            return 1
            ;;
    esac
}

restart_connector_tasks() {
    local name=$(get_connector_name)
    
    log_info "Restarting connector tasks to ensure clean state..."
    
    # Restart the connector and tasks
    curl -sf -X POST "$KAFKA_CONNECT_URL/connectors/$name/restart?includeTasks=true&onlyFailed=false" > /dev/null 2>&1 || true
    
    sleep 3
}

check_connector_status() {
    local name=$(get_connector_name)
    log_info "Checking connector status..."
    
    sleep 5
    
    local status=$(curl -sf "$KAFKA_CONNECT_URL/connectors/$name/status" 2>/dev/null || echo '{"connector":{"state":"UNKNOWN"}}')
    local connector_state=$(echo "$status" | jq -r '.connector.state // "UNKNOWN"')
    local task_state=$(echo "$status" | jq -r '.tasks[0].state // "NO_TASK"')
    
    log_info "Connector state: $connector_state"
    log_info "Task state: $task_state"
    
    if [ "$connector_state" = "RUNNING" ]; then
        if [ "$task_state" = "RUNNING" ]; then
            log_success "Connector is fully operational!"
        elif [ "$task_state" = "FAILED" ]; then
             local trace=$(echo "$status" | jq -r '.tasks[0].trace // "No trace"' | head -c 300)
             log_info "Task failed. Trace: $trace..."
             log_info "Note: This is expected if tables don't exist yet, but publication mode should now be 'all_tables'."
        else
             log_info "Task state: $task_state"
        fi
    else
        log_error "Connector state: $connector_state"
        # Since we just updated it, fail if connector itself is not running
        exit 1
    fi
}

main() {
    log_info "=========================================="
    log_info "Debezium Connector Force Init"
    log_info "=========================================="
    
    if [ ! -f "$CONNECTOR_CONFIG" ]; then
        log_error "Connector config not found: $CONNECTOR_CONFIG"
        exit 1
    fi
    
    # Show current config source
    log_info "Config file: $CONNECTOR_CONFIG"
    
    wait_for_kafka_connect || exit 1
    force_update_connector || exit 1
    restart_connector_tasks
    check_connector_status
    
    log_success "Init completed. Container will exit."
}

main "$@"
