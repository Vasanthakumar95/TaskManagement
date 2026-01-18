# Backend Services

This directory contains all backend microservices for the Task Management application.

## 📁 Structure

```
backend/
├── task-service/           # Main task management API
└── notification-service/   # Event consumer & notification handler
```

## 🎯 Services Overview

### Task Service (Port 8080)

The main REST API service that handles:
- Task CRUD operations
- File attachment management
- Event publishing to Kafka
- Database interactions

**Technology Stack:**
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL 16
- Apache Kafka
- MinIO Client
- Logstash Logback Encoder

### Notification Service (Port 8081)

Event-driven microservice that:
- Consumes Kafka events
- Processes task notifications
- Logs all task-related events
- (Future: Email/SMS notifications)

**Technology Stack:**
- Spring Boot 3.2
- Spring Kafka
- Logstash Logback Encoder

## 🚀 Running the Services

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker (for infrastructure services)

### Start Infrastructure First

```bash
cd devops/docker
docker compose up -d
```

### Run Task Service

```bash
cd backend/task-service

# Clean build
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Or with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Access:** http://localhost:8080

### Run Notification Service

```bash
cd backend/notification-service

# Clean build
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

**Access:** http://localhost:8081

## 🔧 Configuration

### Task Service Configuration

**Location:** `task-service/src/main/resources/application.yml`

Key configurations:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskdb
    username: taskuser
    password: taskpass
  
  kafka:
    bootstrap-servers: localhost:9092
    
minio:
  url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: task-attachments
```

### Notification Service Configuration

**Location:** `notification-service/src/main/resources/application.yml`

Key configurations:
```yaml
server:
  port: 8081

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service-group
```

## 📊 Database Schema

### Tasks Table

```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Task Attachments Table

```sql
CREATE TABLE task_attachments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
```

## 📡 Kafka Topics

### task-events

**Purpose:** Publishes all task-related events

**Event Types:**
- `CREATED` - When a task is created
- `UPDATED` - When a task is updated
- `DELETED` - When a task is deleted

**Event Schema:**
```json
{
  "taskId": 1,
  "title": "Task title",
  "description": "Task description",
  "status": "TODO",
  "eventType": "CREATED",
  "timestamp": "2026-01-17T12:00:00"
}
```

## 🧪 Testing

### Run Unit Tests

```bash
# Task Service
cd backend/task-service
./mvnw test

# Notification Service
cd backend/notification-service
./mvnw test
```

### Manual API Testing

```bash
# Health check
curl http://localhost:8080/actuator/health

# Get all tasks
curl http://localhost:8080/api/tasks

# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "Testing API",
    "status": "TODO"
  }'

# Get task by ID
curl http://localhost:8080/api/tasks/1

# Update task
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Task",
    "description": "Updated description",
    "status": "IN_PROGRESS"
  }'

# Delete task
curl -X DELETE http://localhost:8080/api/tasks/1

# Upload file
curl -X POST http://localhost:8080/api/tasks/1/attachments \
  -F "file=@/path/to/file.pdf"

# Get attachments
curl http://localhost:8080/api/tasks/1/attachments
```

## 📝 Logging

Both services send logs to ELK Stack via Logstash.

**Log Levels:**
- `DEBUG` - Application-specific logs
- `INFO` - General information
- `WARN` - Warning messages
- `ERROR` - Error messages with stack traces

**View logs in Kibana:**
- URL: http://localhost:5601
- Index: `application-logs-*`
- Filter by service: `service: "task-service"`

**Local log files:**
- Task Service: `backend/task-service/logs/task-service.log`
- Notification Service: `backend/notification-service/logs/notification-service.log`

## 🔐 Security (Future)

Planned security features:
- [ ] JWT authentication
- [ ] Role-based access control (RBAC)
- [ ] API rate limiting (via Kong)
- [ ] Input validation
- [ ] SQL injection prevention
- [ ] XSS protection

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check database connection
docker exec -it task-postgres psql -U taskuser -d taskdb

# Restart PostgreSQL
cd devops/docker
docker compose restart postgres
```

### Kafka Connection Issues

```bash
# Check Kafka status
docker ps | grep kafka

# Check Kafka logs
docker logs task-kafka

# Restart Kafka
cd devops/docker
docker compose restart kafka
```

### Build Issues

```bash
# Clean Maven cache
rm -rf ~/.m2/repository/com/learning/

# Clean project
./mvnw clean

# Rebuild
./mvnw clean install -U
```

## 📦 Building for Production

```bash
# Build JAR file
./mvnw clean package -DskipTests

# JAR location
ls -lh target/*.jar

# Run JAR
java -jar target/task-service-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker Support

### Build Docker Image

```bash
# Task Service
cd backend/task-service
docker build -t task-service:latest -f ../../devops/docker/dockerfiles/task-service.Dockerfile .

# Notification Service
cd backend/notification-service
docker build -t notification-service:latest -f ../../devops/docker/dockerfiles/notification-service.Dockerfile .
```

### Run in Docker

```bash
docker run -d \
  --name task-service \
  --network taskmanagement_app-network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskdb \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  task-service:latest
```

## 🎯 Performance Optimization

### Database

- Indexes on frequently queried columns
- Connection pooling (HikariCP)
- Query optimization

### Caching (Future)

- Redis for frequently accessed data
- HTTP caching headers
- CDN for static assets

### Monitoring

- Spring Boot Actuator endpoints
- Prometheus metrics (planned)
- Grafana dashboards (planned)

## 📚 API Endpoints

See [API Documentation](../docs/API.md) for complete endpoint reference.

## 🤝 Contributing

1. Create a feature branch
2. Write tests
3. Ensure all tests pass
4. Submit a pull request

## 📄 License

MIT License - Free to use for learning purposes