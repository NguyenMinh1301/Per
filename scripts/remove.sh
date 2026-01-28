#!/bin/bash

# ==============================
# Reset Docker Environment
# ==============================

echo -e "\n\033[1;34m[1/3] Stopping containers...\033[0m"
docker stop per postgres redis kafka >/dev/null 2>&1

echo -e "\n\033[1;34m[2/3] Removing containers...\033[0m"
docker rm per postgres redis kafka >/dev/null 2>&1

echo -e "\n\033[1;34m[3/3] Removing images...\033[0m"
docker rmi nguyenminh1301/per:latest \
  pgvector/pgvector:pg16 \
  redis:7-alpine \
  confluentinc/cp-kafka:7.6.1 >/dev/null 2>&1

echo -e "\n\033[1;32m Reset completed. All selected containers and images have been removed.\033[0m\n"
