#!/bin/bash

# Start All Services Script
# Run from project root: ./scripts/start-all.sh

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🚀 Starting Task Management Application"
echo "========================================"
echo ""

# Start infrastructure
echo "📦 Starting infrastructure services..."
cd devops/docker
docker compose up -d

echo ""
echo "⏳ Waiting 30 seconds for services to initialize..."
sleep 30

# Configure Kong
echo ""
echo "🔧 Configuring Kong Gateway..."
cd ../kong
./kong-setup.sh

echo ""
echo "✅ Infrastructure ready!"
echo ""
echo "📋 Next steps:"
echo ""
echo "1. Start Task Service (Terminal 1):"
echo "   cd backend/task-service && ./mvnw spring-boot:run"
echo ""
echo "2. Start Notification Service (Terminal 2):"
echo "   cd backend/notification-service && ./mvnw spring-boot:run"
echo ""
echo "3. Start Frontend (Terminal 3):"
echo "   cd frontend/web && ng serve"
echo ""
echo "🌐 Access URLs:"
echo "   • Web App:      http://localhost:4200"
echo "   • API Gateway:  http://localhost:8000"
echo "   • Kong Manager: http://localhost:8002"
echo "   • Kibana:       http://localhost:5601"
echo "   • Kafka UI:     http://localhost:8090"
echo "   • MinIO:        http://localhost:9001"
echo "   • pgAdmin:      http://localhost:5050"
echo ""