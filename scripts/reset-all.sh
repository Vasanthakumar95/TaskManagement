#!/bin/bash

# Reset All Services Script - Clean slate
# Run from project root: ./scripts/reset-all.sh

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🔄 Resetting Task Management Application"
echo "========================================"
echo "⚠️  WARNING: This will delete all data!"
echo ""

read -p "Are you sure you want to reset everything? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Reset cancelled"
    exit 1
fi

echo ""
echo "🧹 Cleaning up..."

# Stop and remove containers
cd devops/docker
docker compose down -v

# Remove log files
echo "🗑️  Removing log files..."
rm -rf "$PROJECT_ROOT/backend/task-service/logs"
rm -rf "$PROJECT_ROOT/backend/notification-service/logs"

# Clean Maven builds
echo "🧹 Cleaning Maven builds..."
cd "$PROJECT_ROOT/backend/task-service"
./mvnw clean

cd "$PROJECT_ROOT/backend/notification-service"
./mvnw clean

# Clean Angular build
echo "🧹 Cleaning Angular build..."
cd "$PROJECT_ROOT/frontend/web"
rm -rf dist/ .angular/

echo ""
echo "✅ Reset complete!"
echo ""
echo "📋 Next steps:"
echo "1. Run: ./scripts/start-all.sh"
echo "2. Start your backend and frontend services"
echo ""