# 📝 Task Management - Full Stack Application

A complete full-stack task management system built with microservices architecture for learning modern development practices.

## 🎯 Project Overview

This project demonstrates:
- Microservices architecture
- Event-driven design with Kafka
- API Gateway pattern with Kong
- Centralized logging with ELK Stack
- Object storage with MinIO
- Modern frontend with Angular
- Containerization with Docker

## 📁 Project Structure

```
TaskManagement/
├── backend/                    # Backend microservices
│   ├── task-service/          # Main API service
│   └── notification-service/  # Event consumer
├── frontend/                   # Frontend applications
│   └── web/                   # Angular web app
├── devops/                     # DevOps configurations
│   ├── docker/                # Docker Compose
│   ├── kubernetes/            # K8s manifests
│   ├── kong/                  # API Gateway setup
│   └── logstash/              # Log processing
└── scripts/                    # Utility scripts
```

## 🚀 Quick Start

### Prerequisites
- Docker Desktop (with Kubernetes enabled)
- Java 17+
- Node.js 20+
- Maven 3.9+

### IDE
- IntelliJ Idea
- VS Code

### Start Everything

```bash
# 1. Start infrastructure services
./scripts/start-all.sh

# 2. In Terminal 1 - Start Task Service
cd backend/task-service
./mvnw spring-boot:run

# 3. In Terminal 2 - Start Notification Service
cd backend/notification-service
./mvnw spring-boot:run

# 4. In Terminal 3 - Start Web App
cd frontend/web
ng serve
```

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Web App** | http://localhost:4200 | - |
| **API Gateway** | http://localhost:8000 | - |
| **Kong Manager** | http://localhost:8002 | - |
| **Kibana** | http://localhost:5601 | - |
| **Kafka UI** | http://localhost:8090 | - |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin |
| **pgAdmin** | http://localhost:5050 | admin@admin.com / admin |

## 🛠️ Tech Stack

### Frontend
- **Angular 18** - Web framework
- **TypeScript** - Type-safe JavaScript
- **RxJS** - Reactive programming
- **Tailwind CSS** - Utility-first CSS

### Backend
- **Spring Boot 3.2** - Java framework
- **PostgreSQL 16** - Relational database
- **Apache Kafka 3.5** - Event streaming
- **MinIO** - S3-compatible object storage

### Infrastructure
- **Kong Gateway 3.5** - API management
- **ELK Stack 8.11** - Logging & monitoring
- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Orchestration (ready)

## 📚 Features

### ✅ Implemented
- [x] Task CRUD operations
- [x] File attachments with drag & drop
- [x] Real-time event streaming
- [x] Microservices communication
- [x] API Gateway with rate limiting
- [x] Centralized logging
- [x] CORS handling
- [x] MinIO object storage

### 🚧 Planned
- [ ] User authentication (JWT)
- [ ] Task sharing & permissions
- [ ] Email/Push notifications
- [ ] Mobile app (Android)
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline
- [ ] Monitoring dashboards

## 🧪 Development

### Backend Development

```bash
# Build task service
cd backend/task-service
./mvnw clean install

# Run tests
./mvnw test

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend Development

```bash
cd frontend/web

# Install dependencies
npm install

# Development server
ng serve

# Build for production
ng build --configuration production

# Run tests
ng test
```

## 🐳 Docker Commands

```bash
# Start all infrastructure
cd devops/docker
docker compose up -d

# Stop all services
docker compose down

# View logs
docker compose logs -f [service-name]

# Restart a service
docker compose restart [service-name]

# Clean up everything (including data)
docker compose down -v
```

## 📊 Architecture

```
┌─────────────────────────────────────────┐
│      Angular Web App (Port 4200)       │
└──────────────────┬──────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────┐
│   Kong API Gateway (Port 8000)          │
│   • Rate Limiting  • CORS  • Logging    │
└─────────┬─────────────────┬──────────────┘
          │                 │
          ↓                 ↓
┌─────────────────┐  ┌─────────────────┐
│  Task Service   │  │ Notification    │
│  (Port 8080)    │  │ Service (8081)  │
└────────┬────────┘  └────────┬────────┘
         │                    │
         └──────────┬─────────┘
                    ↓
          ┌─────────────────┐
          │  Apache Kafka   │
          │  Event Stream   │
          └─────────────────┘
         │        │        │
         ↓        ↓        ↓
    ┌────────┐ ┌──────┐ ┌─────┐
    │Postgres│ │MinIO │ │ ELK │
    └────────┘ └──────┘ └─────┘
```

## 📝 Useful Scripts

```bash
# Start all infrastructure
./scripts/start-all.sh

# Stop all services
./scripts/stop-all.sh

# Reset everything (clean slate)
./scripts/reset-all.sh

# Setup development environment
./scripts/setup.sh
```

## 🔍 Monitoring & Debugging

### View Application Logs (Kibana)
1. Open http://localhost:5601
2. Go to **Discover**
3. Filter: `service: "task-service"`

### Monitor Kafka Events
1. Open http://localhost:8090
2. Navigate to **Topics** → **task-events**
3. View real-time messages

### Check Kong Configuration
```bash
# List all services
curl http://localhost:8001/services

# List all routes
curl http://localhost:8001/routes

# View plugins
curl http://localhost:8001/plugins
```

## 🐛 Troubleshooting

### Kong not routing
```bash
cd devops/kong
./kong-setup.sh
```

### Kafka connection issues
```bash
cd devops/docker
docker compose restart kafka
docker compose logs kafka
```

### Database connection errors
```bash
docker compose restart postgres
docker compose logs postgres
```

## 📖 Documentation

- [Backend Services](./backend/README.md)
- [Frontend](./frontend/web/README.md)
- [DevOps Setup](./devops/README.md)
- [API Documentation](./docs/API.md)
- [Docker Cheat Sheet](/docs/docker-cheat-sheet.md)

## 🤝 Contributing

This is a learning project. Contributions, issues, and feature requests are welcome!

## 📄 License

MIT License - Free to use for learning purposes

---

**Built with ❤️ for learning Full Stack Development**

Made with: Java • Spring Boot • Angular • Kafka • Kong • ELK • Docker • Kubernetes