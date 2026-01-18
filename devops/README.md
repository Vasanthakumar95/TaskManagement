# DevOps Configuration

Infrastructure as Code for the Task Management application.

## 📁 Structure

```
devops/
├── docker/
│   ├── docker-compose.yml      # All infrastructure services
│   └── dockerfiles/            # Application Dockerfiles
├── kubernetes/
│   └── manifests/              # K8s deployment files
├── kong/
│   └── kong-setup.sh           # Kong Gateway configuration
└── logstash/
    ├── config/
    │   └── logstash.yml        # Logstash configuration
    └── pipeline/
        └── logstash.conf       # Log processing pipeline
```

## 🐳 Docker Compose

### Services Overview

| Service | Port(s) | Purpose |
|---------|---------|---------|
| **PostgreSQL** | 5432 | Main database |
| **pgAdmin** | 5050 | Database management |
| **Zookeeper** | 2181 | Kafka coordination |
| **Kafka** | 9092, 29092 | Event streaming |
| **Kafka UI** | 8090 | Kafka monitoring |
| **MinIO** | 9000, 9001 | Object storage |
| **Elasticsearch** | 9200, 9300 | Log storage |
| **Logstash** | 5000, 9600 | Log processing |
| **Kibana** | 5601 | Log visualization |
| **Kong Database** | - | Kong's PostgreSQL |
| **Kong Gateway** | 8000, 8001, 8002 | API Gateway |

### Quick Commands

```bash
cd devops/docker

# Start all services
docker compose up -d

# Stop all services
docker compose down

# Stop and remove data
docker compose down -v

# View logs
docker compose logs -f [service-name]

# Restart specific service
docker compose restart [service-name]

# Check status
docker compose ps

# View resource usage
docker stats
```

### Service Details

#### PostgreSQL

**Image:** `postgres:16-alpine`  
**Database:** `taskdb`  
**User:** `taskuser`  
**Password:** `taskpass`

```bash
# Connect via psql
docker exec -it task-postgres psql -U taskuser -d taskdb

# Backup database
docker exec task-postgres pg_dump -U taskuser taskdb > backup.sql

# Restore database
docker exec -i task-postgres psql -U taskuser taskdb < backup.sql
```

#### Kafka

**Image:** `confluentinc/cp-kafka:7.5.0`  
**Bootstrap Servers:** `localhost:9092`  
**Internal:** `kafka:29092`

```bash
# List topics
docker exec task-kafka kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
docker exec task-kafka kafka-topics --describe --topic task-events --bootstrap-server localhost:9092

# Consume messages
docker exec task-kafka kafka-console-consumer --topic task-events --from-beginning --bootstrap-server localhost:9092

# Produce test message
docker exec -it task-kafka kafka-console-producer --topic task-events --bootstrap-server localhost:9092
```

#### MinIO

**Image:** `minio/minio:latest`  
**Console:** http://localhost:9001  
**API:** http://localhost:9000  
**Credentials:** `minioadmin` / `minioadmin`

```bash
# List buckets
docker exec task-minio mc ls local/

# List objects in bucket
docker exec task-minio mc ls local/task-attachments/

# Download object
docker exec task-minio mc cp local/task-attachments/file.pdf /tmp/
```

#### Elasticsearch

**Image:** `docker.elastic.co/elasticsearch/elasticsearch:8.11.0`  
**URL:** http://localhost:9200

```bash
# Check cluster health
curl http://localhost:9200/_cluster/health?pretty

# List indices
curl http://localhost:9200/_cat/indices?v

# Search logs
curl -X GET "http://localhost:9200/application-logs-*/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "match": {
      "service": "task-service"
    }
  }
}'
```

#### Kong Gateway

**Image:** `kong:3.5`  
**Proxy:** http://localhost:8000  
**Admin API:** http://localhost:8001  
**Manager:** http://localhost:8002

```bash
# Check Kong status
curl http://localhost:8001/status

# List services
curl http://localhost:8001/services

# List routes
curl http://localhost:8001/routes

# List plugins
curl http://localhost:8001/plugins
```

## 🔧 Kong Gateway Configuration

### Setup Script

**Location:** `devops/kong/kong-setup.sh`

**What it does:**
1. Cleans existing configuration
2. Creates services for microservices
3. Sets up routes with correct path handling
4. Enables CORS plugin
5. Configures rate limiting
6. Verifies configuration

```bash
cd devops/kong

# Run setup
./kong-setup.sh

# Manual verification
curl http://localhost:8001/services
curl http://localhost:8001/routes
```

### Kong Services

**Task Service:**
- Name: `task-service`
- URL: `http://host.docker.internal:8080`
- Route: `/api/tasks`

**Notification Service:**
- Name: `notification-service`
- URL: `http://host.docker.internal:8081`
- Route: `/api/notifications`

### Kong Plugins

**CORS:**
- Origins: `http://localhost:4200`
- Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Credentials: true

**Rate Limiting:**
- Limit: 100 requests per minute
- Policy: local

### Troubleshooting Kong

```bash
# Reset Kong configuration
cd devops/kong
./kong-setup.sh

# Check Kong logs
docker logs task-kong

# Restart Kong
docker compose restart kong

# Test routing
curl -v http://localhost:8000/api/tasks
```

## 📊 ELK Stack Configuration

### Elasticsearch

**Configuration:**
- Single-node cluster
- No security (development only)
- Heap: 512MB

**Data persistence:**
- Volume: `elasticsearch_data`

### Logstash

**Pipeline:** `devops/logstash/pipeline/logstash.conf`

**Input:**
- TCP port 5000 (JSON lines)
- UDP port 5000 (JSON)

**Filter:**
- Adds service name
- Parses log levels
- Enriches with metadata

**Output:**
- Elasticsearch (index: `application-logs-YYYY.MM.dd`)
- Stdout (debugging)

### Kibana

**URL:** http://localhost:5601

**Setup:**
1. Create data view:
   - Pattern: `application-logs-*`
   - Timestamp: `@timestamp`
2. Go to Discover
3. Filter logs: `service: "task-service"`

**Useful Queries:**
```
# Task service errors
service: "task-service" AND level: "error"

# Kafka events
logger_name: *TaskEventProducer* OR logger_name: *TaskEventConsumer*

# File uploads
message: *upload*

# Last hour only
@timestamp >= now-1h
```

## 🎯 Docker Networking

### Network: `app-network`

**Type:** Bridge  
**Driver:** bridge

**Connected Services:**
- All infrastructure services
- (Future) Application containers

```bash
# Inspect network
docker network inspect taskmanagement_app-network

# View connected containers
docker network inspect taskmanagement_app-network | grep Name
```

## 💾 Docker Volumes

### Persistent Data

| Volume | Size | Purpose |
|--------|------|---------|
| `taskmanagement_postgres_data` | ~100MB | Database |
| `taskmanagement_minio_data` | Variable | File storage |
| `taskmanagement_elasticsearch_data` | ~500MB | Logs |
| `taskmanagement_kong_data` | ~50MB | Kong config |

```bash
# List volumes
docker volume ls | grep taskmanagement

# Inspect volume
docker volume inspect taskmanagement_postgres_data

# Backup volume
docker run --rm -v taskmanagement_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data

# Restore volume
docker run --rm -v taskmanagement_postgres_data:/data -v $(pwd):/backup alpine sh -c "cd /data && tar xzf /backup/postgres-backup.tar.gz --strip 1"

# Remove all volumes (DANGEROUS!)
docker volume prune
```

## 🔒 Security Considerations

### Development Environment

**Current setup (NOT for production):**
- ❌ No authentication on services
- ❌ Default credentials
- ❌ HTTP only (no HTTPS)
- ❌ No network isolation
- ❌ Security features disabled

### Production Recommendations

**Must-have for production:**
- ✅ Enable security in Elasticsearch
- ✅ Use strong passwords
- ✅ Enable HTTPS/TLS
- ✅ Network segmentation
- ✅ Secrets management (Vault, AWS Secrets Manager)
- ✅ Regular security updates
- ✅ Access control lists
- ✅ Firewall rules

## 🚀 Performance Tuning

### Resource Allocation

```yaml
# docker-compose.yml
services:
  elasticsearch:
    deploy:
      resources:
        limits:
          memory: 1GB
        reservations:
          memory: 512MB
```

### Java Heap Sizes

```yaml
environment:
  - "ES_JAVA_OPTS=-Xms512m -Xmx512m"  # Elasticsearch
  - "LS_JAVA_OPTS=-Xms256m -Xmx256m"  # Logstash
```

### Connection Pools

Adjust based on load:
- PostgreSQL max connections: 100
- Kafka partitions: 3
- Kong workers: auto

## 📈 Monitoring

### Health Checks

```bash
# All services health
docker compose ps

# PostgreSQL
docker exec task-postgres pg_isready

# Elasticsearch
curl http://localhost:9200/_cluster/health

# Kafka
docker exec task-kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Kong
curl http://localhost:8001/status

# MinIO
curl http://localhost:9000/minio/health/live
```

### Resource Monitoring

```bash
# Real-time stats
docker stats

# Disk usage
docker system df

# Clean up unused resources
docker system prune
```

## 🔄 Backup & Recovery

### Database Backup

```bash
# Backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec task-postgres pg_dump -U taskuser taskdb > backup_${DATE}.sql
```

### Full Backup

```bash
# Backup all volumes
docker-compose down
tar czf backup.tar.gz /var/lib/docker/volumes/taskmanagement_*
docker-compose up -d
```

## 🐛 Troubleshooting

### Common Issues

**Port conflicts:**
```bash
# Find process using port
lsof -i :5432

# Change port in docker-compose.yml
ports:
  - "5433:5432"
```

**Out of disk space:**
```bash
# Check disk usage
df -h
docker system df

# Clean up
docker system prune -a --volumes
```

**Services not starting:**
```bash
# Check logs
docker compose logs [service-name]

# Restart service
docker compose restart [service-name]

# Rebuild service
docker compose up -d --force-recreate [service-name]
```

**Network issues:**
```bash
# Recreate network
docker compose down
docker network prune
docker compose up -d
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Kong Gateway Documentation](https://docs.konghq.com/)
- [Elastic Stack Documentation](https://www.elastic.co/guide/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)

## 🔮 Future Enhancements

- [ ] Kubernetes deployment
- [ ] Prometheus & Grafana monitoring
- [ ] Distributed tracing (Jaeger)
- [ ] Service mesh (Istio)
- [ ] GitOps with ArgoCD
- [ ] CI/CD pipelines
- [ ] Infrastructure testing
- [ ] Disaster recovery procedures