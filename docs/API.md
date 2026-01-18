# API Documentation

Complete REST API reference for the Task Management application.

## 📡 Base URL

**Development:**
- Through Kong Gateway: `http://localhost:8000`
- Direct to service: `http://localhost:8080`

**Production:**
- `https://api.yourdomain.com`

## 🔑 Authentication

Currently no authentication (development only).

**Future:** JWT Bearer tokens
```
Authorization: Bearer <token>
```

## 📋 Task Endpoints

### Get All Tasks

Retrieve all tasks with optional filtering.

**Endpoint:** `GET /api/tasks`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Filter by status (TODO, IN_PROGRESS, DONE) |
| `search` | string | No | Search by title (case-insensitive) |

**Example Requests:**

```bash
# Get all tasks
curl http://localhost:8000/api/tasks

# Get tasks by status
curl http://localhost:8000/api/tasks?status=TODO

# Search tasks
curl http://localhost:8000/api/tasks?search=meeting
```

**Success Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
[
  {
    "id": 1,
    "title": "Complete project documentation",
    "description": "Write comprehensive docs",
    "status": "IN_PROGRESS",
    "createdAt": "2026-01-17T10:00:00",
    "updatedAt": "2026-01-17T15:30:00"
  },
  {
    "id": 2,
    "title": "Review pull requests",
    "description": null,
    "status": "TODO",
    "createdAt": "2026-01-17T11:00:00",
    "updatedAt": "2026-01-17T11:00:00"
  }
]
```

---

### Get Task by ID

Retrieve a specific task by its ID.

**Endpoint:** `GET /api/tasks/{id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | Task ID |

**Example Request:**

```bash
curl http://localhost:8000/api/tasks/1
```

**Success Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive docs",
  "status": "IN_PROGRESS",
  "createdAt": "2026-01-17T10:00:00",
  "updatedAt": "2026-01-17T15:30:00"
}
```

**Error Response:**

```http
HTTP/1.1 404 Not Found
Content-Type: application/json
```

```json
{
  "timestamp": "2026-01-17T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 1",
  "path": "/api/tasks/1"
}
```

---

### Create Task

Create a new task.

**Endpoint:** `POST /api/tasks`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `title` | string | Yes | Task title (max 255 chars) |
| `description` | string | No | Task description (max 1000 chars) |
| `status` | string | No | Task status (default: TODO) |

**Valid Status Values:**
- `TODO`
- `IN_PROGRESS`
- `DONE`

**Example Request:**

```bash
curl -X POST http://localhost:8000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Task",
    "description": "Task description",
    "status": "TODO"
  }'
```

**Success Response:**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 3,
  "title": "New Task",
  "description": "Task description",
  "status": "TODO",
  "createdAt": "2026-01-17T16:00:00",
  "updatedAt": "2026-01-17T16:00:00"
}
```

**Validation Error Response:**

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{
  "timestamp": "2026-01-17T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Title is required",
  "path": "/api/tasks"
}
```

---

### Update Task

Update an existing task.

**Endpoint:** `PUT /api/tasks/{id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | Task ID |

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:** Same as Create Task

**Example Request:**

```bash
curl -X PUT http://localhost:8000/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Task",
    "description": "Updated description",
    "status": "DONE"
  }'
```

**Success Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "id": 1,
  "title": "Updated Task",
  "description": "Updated description",
  "status": "DONE",
  "createdAt": "2026-01-17T10:00:00",
  "updatedAt": "2026-01-17T16:05:00"
}
```

---

### Delete Task

Delete a task and all its attachments.

**Endpoint:** `DELETE /api/tasks/{id}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | integer | Yes | Task ID |

**Example Request:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/1
```

**Success Response:**

```http
HTTP/1.1 204 No Content
```

---

## 📎 Attachment Endpoints

### Upload File

Upload a file attachment to a task.

**Endpoint:** `POST /api/tasks/{taskId}/attachments`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskId` | integer | Yes | Task ID |

**Request Headers:**
```
Content-Type: multipart/form-data
```

**Form Data:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | file | Yes | File to upload |

**Example Request:**

```bash
curl -X POST http://localhost:8000/api/tasks/1/attachments \
  -F "file=@/path/to/document.pdf"
```

**Success Response:**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "taskId": 1,
  "fileName": "1_a1b2c3d4_document.pdf",
  "originalFileName": "document.pdf",
  "contentType": "application/pdf",
  "fileSize": 102400,
  "uploadedAt": "2026-01-17T16:10:00"
}
```

---

### Get Attachments

Get all attachments for a task.

**Endpoint:** `GET /api/tasks/{taskId}/attachments`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskId` | integer | Yes | Task ID |

**Example Request:**

```bash
curl http://localhost:8000/api/tasks/1/attachments
```

**Success Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
[
  {
    "id": 1,
    "taskId": 1,
    "fileName": "1_a1b2c3d4_document.pdf",
    "originalFileName": "document.pdf",
    "contentType": "application/pdf",
    "fileSize": 102400,
    "uploadedAt": "2026-01-17T16:10:00"
  }
]
```

---

### Download File

Download an attachment.

**Endpoint:** `GET /api/tasks/{taskId}/attachments/{attachmentId}/download`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskId` | integer | Yes | Task ID |
| `attachmentId` | integer | Yes | Attachment ID |

**Example Request:**

```bash
curl -O -J http://localhost:8000/api/tasks/1/attachments/1/download
```

**Success Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="document.pdf"
Content-Length: 102400

[binary file content]
```

---

### Get Download URL

Get a presigned URL for direct download (valid for 1 hour).

**Endpoint:** `GET /api/tasks/{taskId}/attachments/{attachmentId}/url`

**Example Request:**

```bash
curl http://localhost:8000/api/tasks/1/attachments/1/url
```

**Success Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "url": "http://localhost:9000/task-attachments/1_a1b2c3d4_document.pdf?X-Amz-Algorithm=...",
  "fileName": "document.pdf"
}
```

---

### Delete Attachment

Delete an attachment from a task.

**Endpoint:** `DELETE /api/tasks/{taskId}/attachments/{attachmentId}`

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `taskId` | integer | Yes | Task ID |
| `attachmentId` | integer | Yes | Attachment ID |

**Example Request:**

```bash
curl -X DELETE http://localhost:8000/api/tasks/1/attachments/1
```

**Success Response:**

```http
HTTP/1.1 204 No Content
```

---

## 📊 Data Models

### Task

```typescript
{
  id: number;              // Auto-generated
  title: string;           // Required, max 255 chars
  description?: string;    // Optional, max 1000 chars
  status: string;          // TODO | IN_PROGRESS | DONE
  createdAt: string;       // ISO 8601 timestamp
  updatedAt: string;       // ISO 8601 timestamp
}
```

### TaskAttachment

```typescript
{
  id: number;              // Auto-generated
  taskId: number;          // Foreign key to Task
  fileName: string;        // Stored filename in MinIO
  originalFileName: string;// Original uploaded filename
  contentType: string;     // MIME type
  fileSize: number;        // Size in bytes
  uploadedAt: string;      // ISO 8601 timestamp
}
```

---

## ⚠️ Error Responses

### Standard Error Format

```json
{
  "timestamp": "2026-01-17T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tasks"
}
```

### HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request succeeded |
| 201 | Created | Resource created |
| 204 | No Content | Success, no response body |
| 400 | Bad Request | Invalid request data |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

---

## 🔄 Rate Limiting

**Via Kong Gateway:**
- Limit: 100 requests per minute per IP
- Header: `X-RateLimit-Remaining`

**Example Headers:**
```
X-RateLimit-Limit-Minute: 100
X-RateLimit-Remaining-Minute: 95
```

---

## 📝 Examples

### Complete Workflow

```bash
# 1. Create a task
TASK_ID=$(curl -s -X POST http://localhost:8000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"API Test","status":"TODO"}' \
  | jq -r '.id')

echo "Created task ID: $TASK_ID"

# 2. Upload a file
curl -X POST http://localhost:8000/api/tasks/$TASK_ID/attachments \
  -F "file=@test.pdf"

# 3. Get task with attachments
curl http://localhost:8000/api/tasks/$TASK_ID
curl http://localhost:8000/api/tasks/$TASK_ID/attachments

# 4. Update task status
curl -X PUT http://localhost:8000/api/tasks/$TASK_ID \
  -H "Content-Type: application/json" \
  -d '{"title":"API Test","status":"DONE"}'

# 5. Delete task
curl -X DELETE http://localhost:8000/api/tasks/$TASK_ID
```

---

## 🧪 Testing with Postman

Import this collection:

```json
{
  "info": {
    "name": "Task Management API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get All Tasks",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/tasks"
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8000"
    }
  ]
}
```

---

## 🔮 Future Endpoints

**Planned:**
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration
- `GET /api/users/me` - Get current user
- `POST /api/tasks/{id}/share` - Share task with users
- `GET /api/tasks/{id}/comments` - Task comments
- `POST /api/tasks/{id}/comments` - Add comment

---

## 📚 Additional Resources

- [Swagger/OpenAPI Specification](./swagger.json) (future)
- [Postman Collection](./postman-collection.json) (future)
- [cURL Examples](./curl-examples.sh)

---

## 🤝 API Versioning (Future)

**Planned versioning strategy:**
- URL: `/api/v1/tasks`, `/api/v2/tasks`
- Header: `Accept: application/vnd.taskmanagement.v1+json`

---

Last Updated: January 17, 2026