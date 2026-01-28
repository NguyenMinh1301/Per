#!/bin/bash

# ==============================
# Restart Docker Environment
# ==============================

echo -e "\n\033[1;34m[1/4] Stopping containers...\033[0m"
docker stop per postgres redis kafka >/dev/null 2>&1

echo -e "\n\033[1;34m[2/4] Removing containers...\033[0m"
docker rm per postgres redis kafka >/dev/null 2>&1

echo -e "\n\033[1;34m[3/4] Removing images...\033[0m"
docker rmi nguyenminh1301/per:latest \
  pgvector/pgvector:pg16 \
  redis:7-alpine \
  confluentinc/cp-kafka:7.6.1 >/dev/null 2>&1

echo -e "\n\033[1;34m[4/4] Starting Docker Compose...\033[0m"
docker compose up -d

echo -e "\n\033[1;32m Restart completed. Docker Compose stack is now running.\033[0m\n"
