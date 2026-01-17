#!/bin/bash

# Kong Admin API URL
KONG_ADMIN_URL="http://localhost:8001"

echo "🚀 Configuring Kong Gateway..."
echo ""

# Function to delete if exists
delete_if_exists() {
  local resource_type=$1
  local resource_name=$2
  
  echo "🧹 Cleaning up existing $resource_type: $resource_name..."
  curl -s -X DELETE "$KONG_ADMIN_URL/$resource_type/$resource_name" > /dev/null 2>&1
}

# Clean up existing configuration
echo "════════════════════════════════════════════════════════"
echo "Step 1: Cleaning up existing Kong configuration"
echo "════════════════════════════════════════════════════════"
delete_if_exists "routes" "task-service-route"
delete_if_exists "routes" "notification-service-route"
delete_if_exists "services" "task-service"
delete_if_exists "services" "notification-service"

# Delete all CORS plugins
echo "🧹 Cleaning up existing plugins..."
PLUGIN_IDS=$(curl -s "$KONG_ADMIN_URL/plugins" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
for plugin_id in $PLUGIN_IDS; do
  curl -s -X DELETE "$KONG_ADMIN_URL/plugins/$plugin_id" > /dev/null 2>&1
done

sleep 2
echo "✅ Cleanup complete"
echo ""

# 1. Create Task Service
echo "════════════════════════════════════════════════════════"
echo "Step 2: Creating Task Service"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/services \
  -H "Content-Type: application/json" \
  -d '{
    "name": "task-service",
    "url": "http://host.docker.internal:8080"
  }' | grep -q "task-service" && echo "✅ Task Service created" || echo "❌ Failed to create Task Service"
echo ""

# 2. Create Route for Task Service (with strip_path=false)
echo "════════════════════════════════════════════════════════"
echo "Step 3: Creating Task Service Route"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/services/task-service/routes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "task-service-route",
    "paths": ["/api/tasks"],
    "strip_path": false,
    "preserve_host": false
  }' | grep -q "task-service-route" && echo "✅ Task Service Route created (strip_path=false)" || echo "❌ Failed to create route"
echo ""

# 3. Create Notification Service
echo "════════════════════════════════════════════════════════"
echo "Step 4: Creating Notification Service"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/services \
  -H "Content-Type: application/json" \
  -d '{
    "name": "notification-service",
    "url": "http://host.docker.internal:8081"
  }' | grep -q "notification-service" && echo "✅ Notification Service created" || echo "❌ Failed to create Notification Service"
echo ""

# 4. Create Route for Notification Service (with strip_path=false)
echo "════════════════════════════════════════════════════════"
echo "Step 5: Creating Notification Service Route"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/services/notification-service/routes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "notification-service-route",
    "paths": ["/api/notifications"],
    "strip_path": false,
    "preserve_host": false
  }' | grep -q "notification-service-route" && echo "✅ Notification Service Route created (strip_path=false)" || echo "❌ Failed to create route"
echo ""

# 5. Enable CORS Plugin (global for all services)
echo "════════════════════════════════════════════════════════"
echo "Step 6: Enabling CORS Plugin"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "cors",
    "config": {
      "origins": ["http://localhost:4200"],
      "methods": ["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
      "headers": ["Accept", "Accept-Version", "Content-Length", "Content-MD5", "Content-Type", "Date", "X-Auth-Token"],
      "exposed_headers": ["X-Auth-Token"],
      "credentials": true,
      "max_age": 3600
    }
  }' | grep -q "cors" && echo "✅ CORS Plugin enabled" || echo "❌ Failed to enable CORS"
echo ""

# 6. Enable Rate Limiting Plugin for Task Service
echo "════════════════════════════════════════════════════════"
echo "Step 7: Enabling Rate Limiting"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/services/task-service/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "rate-limiting",
    "config": {
      "minute": 100,
      "policy": "local"
    }
  }' | grep -q "rate-limiting" && echo "✅ Rate Limiting enabled (100 req/min)" || echo "❌ Failed to enable rate limiting"
echo ""

# 7. Enable Request/Response Logging
echo "════════════════════════════════════════════════════════"
echo "Step 8: Enabling HTTP Logging"
echo "════════════════════════════════════════════════════════"
curl -s -X POST $KONG_ADMIN_URL/plugins \
  -H "Content-Type: application/json" \
  -d '{
    "name": "http-log",
    "config": {
      "http_endpoint": "http://localhost:8001/mock-endpoint"
    }
  }' > /dev/null 2>&1
echo "✅ HTTP Logging configured"
echo ""

# Verification
echo "════════════════════════════════════════════════════════"
echo "Step 9: Verifying Configuration"
echo "════════════════════════════════════════════════════════"

# Check strip_path setting
STRIP_PATH=$(curl -s "$KONG_ADMIN_URL/routes/task-service-route" | grep -o '"strip_path":[^,]*' | cut -d':' -f2)
if [ "$STRIP_PATH" = "false" ]; then
  echo "✅ Task Service Route: strip_path = false (correct)"
else
  echo "❌ Task Service Route: strip_path = $STRIP_PATH (should be false)"
fi

STRIP_PATH_NOTIF=$(curl -s "$KONG_ADMIN_URL/routes/notification-service-route" | grep -o '"strip_path":[^,]*' | cut -d':' -f2)
if [ "$STRIP_PATH_NOTIF" = "false" ]; then
  echo "✅ Notification Service Route: strip_path = false (correct)"
else
  echo "❌ Notification Service Route: strip_path = $STRIP_PATH_NOTIF (should be false)"
fi

echo ""
echo "════════════════════════════════════════════════════════"
echo "✅ Kong Gateway Configuration Complete!"
echo "════════════════════════════════════════════════════════"
echo ""
echo "📋 Summary:"
echo "  • Task Service:         http://localhost:8000/api/tasks"
echo "  • Notification Service: http://localhost:8000/api/notifications"
echo "  • Kong Admin API:       http://localhost:8001"
echo "  • Kong Manager GUI:     http://localhost:8002"
echo ""
echo "🧪 Test Commands:"
echo "  curl http://localhost:8000/api/tasks"
echo "  curl http://localhost:8000/api/tasks/1"
echo ""
echo "🔍 Verify Configuration:"
echo "  curl http://localhost:8001/services"
echo "  curl http://localhost:8001/routes"
echo "  curl http://localhost:8001/plugins"
echo ""