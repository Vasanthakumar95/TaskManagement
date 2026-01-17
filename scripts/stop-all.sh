#!/bin/bash

# Stop All Services Script
# Run from project root: ./scripts/stop-all.sh

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🛑 Stopping Task Management Application"
echo "========================================"
echo ""

cd devops/docker

read -p "Do you want to remove volumes (delete all data)? (y/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️  Stopping and removing containers with volumes..."
    docker compose down -v
    echo "✅ All containers and data removed"
else
    echo "📦 Stopping containers (keeping data)..."
    docker compose down
    echo "✅ All containers stopped (data preserved)"
fi

echo ""
echo "💡 Tip: To start again, run: ./scripts/start-all.sh"