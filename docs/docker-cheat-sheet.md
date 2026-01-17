# Docker Compose Cheat Sheet

## Basic Commands

### Starting Services
```bash
# Start all services in detached mode (background)
docker compose up -d

# Start specific service(s)
docker compose up -d postgres kafka

# Start and view logs (foreground)
docker compose up

# Start and rebuild images
docker compose up -d --build
```

### Stopping Services
```bash
# Stop all running services (keeps containers)
docker compose stop

# Stop specific service
docker compose stop postgres

# Stop and remove containers (data persists in volumes)
docker compose down

# Stop and remove containers + volumes (DELETES ALL DATA!)
docker compose down -v

# Stop and remove containers + images
docker compose down --rmi all
```

### Viewing Status & Logs
```bash
# List all services and their status
docker compose ps

# View logs of all services
docker compose logs

# Follow logs in real-time
docker compose logs -f

# View logs for specific service
docker compose logs postgres
docker compose logs -f kafka

# View last 100 lines
docker compose logs --tail=100

# View logs with timestamps
docker compose logs -t
```

### Restarting Services
```bash
# Restart all services
docker compose restart

# Restart specific service
docker compose restart postgres

# Restart and view logs
docker compose restart postgres && docker compose logs -f postgres
```

### Executing Commands in Containers
```bash
# Execute command in running container
docker compose exec postgres psql -U taskuser -d taskdb

# Execute as specific user
docker compose exec -u root postgres bash

# Start a bash shell in container
docker compose exec postgres bash
docker compose exec kafka bash

# Run one-off command (creates new container)
docker compose run postgres psql -U taskuser
```

## Service Management

### Building & Pulling
```bash
# Build or rebuild services
docker compose build

# Build specific service
docker compose build task-service

# Pull latest images
docker compose pull

# Build without cache
docker compose build --no-cache
```

### Scaling Services
```bash
# Scale a service to multiple instances
docker compose up -d --scale kafka=3

# Scale multiple services
docker compose up -d --scale web=3 --scale worker=2
```

### Configuration
```bash
# Validate docker-compose.yml syntax
docker compose config

# View the actual configuration (with variable substitution)
docker compose config --resolve-image-digests

# List services defined in compose file
docker compose config --services
```

## Volume & Network Management

### Volumes
```bash
# List volumes
docker volume ls

# Inspect a volume
docker volume inspect taskmanagement_postgres_data

# Remove unused volumes
docker volume prune

# Remove specific volume (service must be stopped)
docker volume rm taskmanagement_postgres_data
```

### Networks
```bash
# List networks
docker network ls

# Inspect network
docker network inspect taskmanagement_app-network

# Remove unused networks
docker network prune
```

## Troubleshooting

### Health Checks
```bash
# Check container health status
docker compose ps

# Inspect container details
docker inspect task-postgres

# View health check logs
docker inspect task-postgres | grep -i health
```

### Resource Usage
```bash
# View resource usage (CPU, Memory)
docker stats

# View for specific service
docker stats task-postgres
```

### Debugging
```bash
# View container processes
docker compose top

# View port mappings
docker compose port postgres 5432

# Check events
docker compose events

# Pause all services
docker compose pause

# Unpause all services
docker compose unpause
```

## Clean Up Commands

### Remove Everything (Nuclear Option)
```bash
# Stop and remove containers, networks
docker compose down

# Stop and remove containers, networks, volumes
docker compose down -v

# Stop and remove containers, networks, volumes, images
docker compose down -v --rmi all

# Remove all unused Docker resources system-wide
docker system prune -a --volumes
```

### Selective Cleanup
```bash
# Remove stopped containers
docker compose rm

# Remove stopped containers without confirmation
docker compose rm -f

# Remove unused images
docker image prune

# Remove dangling volumes
docker volume prune
```

## Environment & Variables

### Environment Files
```bash
# Use specific env file
docker compose --env-file .env.prod up -d

# Override compose file
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### View Environment Variables
```bash
# Show environment variables of a service
docker compose exec postgres env

# Run with environment variable override
docker compose run -e DEBUG=1 web
```

## Useful Combinations

### Complete Restart
```bash
# Stop, remove, rebuild, and start fresh
docker compose down && docker compose up -d --build
```

### Quick Troubleshoot
```bash
# Check status and logs
docker compose ps && docker compose logs --tail=50
```

### Fresh Start (Keep Data)
```bash
# Restart everything without losing data
docker compose down && docker compose up -d
```

### Fresh Start (Delete Everything)
```bash
# Complete wipe and restart
docker compose down -v && docker compose up -d
```

### Monitor Specific Service
```bash
# Watch logs and stats
docker compose logs -f postgres & docker stats task-postgres
```

## Project-Specific Quick Commands

### For Your Task Management App
```bash
# Start everything
docker compose up -d

# View all services
docker compose ps

# Check PostgreSQL
docker compose exec postgres psql -U taskuser -d taskdb

# View Kafka topics
docker compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Check MinIO
curl http://localhost:9000/minio/health/live

# View all logs
docker compose logs -f

# Restart just Spring Boot (if containerized later)
docker compose restart task-service

# Clean restart (keeps data)
docker compose down && docker compose up -d

# Nuclear option (deletes everything)
docker compose down -v && docker compose up -d
```

## Tips & Best Practices

1. **Always use `-d`** for production/development (runs in background)
2. **Check logs frequently** with `docker compose logs -f`
3. **Use `docker compose ps`** to quickly check service status
4. **Before deleting volumes** (`-v` flag), ensure you have backups
5. **Use `docker compose config`** to validate your compose file
6. **Name your containers** in compose file for easier reference
7. **Health checks** are your friends - use them to ensure services are ready
8. **Use networks** to isolate services
9. **Resource limits** prevent one service from consuming all resources

## Common Issues & Fixes

### Port Already in Use
```bash
# Find what's using the port
lsof -i :5432

# Kill the process or change port in docker-compose.yml
ports:
  - "5433:5432"
```

### Service Won't Start
```bash
# Check logs
docker compose logs service-name

# Remove and recreate
docker compose rm -f service-name
docker compose up -d service-name
```

### Out of Disk Space
```bash
# Clean up everything unused
docker system prune -a --volumes

# Check disk usage
docker system df
```

### Networking Issues
```bash
# Recreate network
docker compose down
docker network prune
docker compose up -d
```

## Keyboard Shortcuts in Logs

- **Ctrl+C** - Stop following logs
- **Ctrl+Z** - Suspend process
- **Ctrl+D** - Exit interactive shell

## File Locations (Mac)

- **Compose files**: Usually in project root
- **Volumes**: `~/Library/Containers/com.docker.docker/Data/`
- **Logs**: View via Docker Desktop or `docker compose logs`