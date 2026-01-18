# Frontend - Angular Web Application

Modern task management web application built with Angular 18 and TypeScript.

## 🎯 Overview

A responsive, feature-rich web application that provides:
- Task management with CRUD operations
- Drag-and-drop file attachments
- Real-time updates
- Status filtering and search
- Modern, intuitive UI

## 🛠️ Technology Stack

- **Angular 18** - Frontend framework
- **TypeScript 5.x** - Programming language
- **RxJS** - Reactive programming
- **Standalone Components** - Modern Angular architecture
- **CSS3** - Styling (no framework dependencies)
- **HttpClient** - API communication

## 📁 Project Structure

```
frontend/web/
├── src/
│   ├── app/
│   │   ├── services/          # API services
│   │   │   └── task.service.ts
│   │   ├── app.component.ts   # Main component
│   │   ├── app.component.html # Main template
│   │   ├── app.component.css  # Main styles
│   │   ├── app.config.ts      # App configuration
│   │   └── app.routes.ts      # Routing config
│   ├── assets/                # Static assets
│   ├── index.html             # HTML entry point
│   └── main.ts                # Application bootstrap
├── angular.json               # Angular CLI config
├── package.json               # Dependencies
└── tsconfig.json              # TypeScript config
```

## 🚀 Getting Started

### Prerequisites

- Node.js 20+ 
- npm 10+
- Angular CLI 18+

### Installation

```bash
cd frontend/web

# Install dependencies
npm install

# Install Angular CLI globally (if not installed)
npm install -g @angular/cli
```

### Development Server

```bash
# Start development server
ng serve

# With custom port
ng serve --port 4300

# Open browser automatically
ng serve --open
```

**Access:** http://localhost:4200

### Build

```bash
# Development build
ng build

# Production build
ng build --configuration production

# Output location: dist/frontend/browser/
```

### Testing

```bash
# Run unit tests
ng test

# Run e2e tests
ng e2e

# Test with coverage
ng test --code-coverage
```

## 🎨 Features

### Task Management

**Create Tasks:**
- Form validation
- Status selection (TODO, IN_PROGRESS, DONE)
- Optional description
- Instant file attachment

**View Tasks:**
- Grid layout
- Color-coded status badges
- Timestamp display
- Attachment indicators

**Update Tasks:**
- Inline editing modal
- Status updates
- Description changes
- Preserves created timestamp

**Delete Tasks:**
- Confirmation dialog
- Cascade delete attachments
- Smooth animations

### File Management

**Upload Files:**
- Drag and drop support
- Multiple file selection
- Click to browse
- Real-time upload progress
- File size display

**View Attachments:**
- File name and metadata
- Upload timestamp
- File size formatting
- Download functionality

**Delete Attachments:**
- Individual file deletion
- Confirmation prompt
- Update UI immediately

### Filtering & Search

**Status Filters:**
- All tasks
- TODO only
- IN_PROGRESS only
- DONE only
- Active state indication

**Search:**
- Real-time search
- Title-based filtering
- Case-insensitive
- Clear results

## 🔧 Configuration

### API Endpoint

**Location:** `src/app/services/task.service.ts`

```typescript
// Through Kong Gateway
private apiUrl = 'http://localhost:8000/api/tasks';

// Direct to backend (bypass Kong)
// private apiUrl = 'http://localhost:8080/api/tasks';
```

### Environment Configuration (Future)

Create `src/environments/`:
```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8000/api/tasks'
};

// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com/api/tasks'
};
```

## 📡 API Communication

### Task Service

**Location:** `src/app/services/task.service.ts`

**Methods:**

```typescript
// Get all tasks
getAllTasks(status?: string, search?: string): Observable<Task[]>

// Get single task
getTaskById(id: number): Observable<Task>

// Create task
createTask(task: Task): Observable<Task>

// Update task
updateTask(id: number, task: Task): Observable<Task>

// Delete task
deleteTask(id: number): Observable<void>

// File operations
uploadFile(taskId: number, file: File): Observable<TaskAttachment>
getAttachments(taskId: number): Observable<TaskAttachment[]>
downloadFile(taskId: number, attachmentId: number): Observable<Blob>
deleteAttachment(taskId: number, attachmentId: number): Observable<void>
```

### Error Handling

```typescript
this.taskService.getAllTasks()
  .subscribe({
    next: (tasks) => {
      // Handle success
      this.tasks = tasks;
    },
    error: (error) => {
      // Handle error
      console.error('Error loading tasks:', error);
      // Show user-friendly message
    }
  });
```

## 🎨 Styling

### Component-Scoped Styles

Each component has its own CSS file with scoped styles:
- `app.component.css` - Main application styles
- No global CSS framework dependencies
- Modern CSS features (Grid, Flexbox, Animations)

### Responsive Design

**Breakpoints:**
```css
/* Mobile */
@media (max-width: 768px) { }

/* Tablet */
@media (min-width: 769px) and (max-width: 1024px) { }

/* Desktop */
@media (min-width: 1025px) { }
```

### Color Scheme

```css
/* Status Colors */
--todo: #ff9800;       /* Orange */
--in-progress: #2196f3; /* Blue */
--done: #4caf50;        /* Green */

/* UI Colors */
--primary: #2196f3;
--danger: #f44336;
--warning: #ff9800;
--success: #4caf50;
```

## 🧪 Testing Best Practices

### Unit Testing

```typescript
// task.service.spec.ts
describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService]
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch all tasks', () => {
    const mockTasks: Task[] = [
      { id: 1, title: 'Test', status: 'TODO' }
    ];

    service.getAllTasks().subscribe(tasks => {
      expect(tasks).toEqual(mockTasks);
    });

    const req = httpMock.expectOne('http://localhost:8000/api/tasks');
    expect(req.request.method).toBe('GET');
    req.flush(mockTasks);
  });
});
```

## 🔐 Security

### CORS

Handled by Kong Gateway:
- Allowed origins: `http://localhost:4200`
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Credentials: true

### Input Validation

```typescript
// Client-side validation
if (!this.newTask.title.trim()) {
  alert('Title is required');
  return;
}

// Server-side validation enforced by backend
```

### XSS Prevention

Angular automatically sanitizes:
- Template bindings: `{{ }}`
- Property bindings: `[property]`
- Attribute bindings

## 📱 Progressive Web App (Future)

Planned PWA features:
- [ ] Service Worker
- [ ] Offline support
- [ ] App manifest
- [ ] Push notifications
- [ ] Install prompt

## ⚡ Performance Optimization

### Current Optimizations

- Lazy loading (ready for routes)
- Change detection optimization
- OnPush strategy (ready)
- RxJS operators (debounceTime, distinctUntilChanged)

### Future Improvements

- [ ] Virtual scrolling for large lists
- [ ] Image lazy loading
- [ ] Code splitting
- [ ] Bundle optimization
- [ ] Service Worker caching

## 🐛 Debugging

### Browser DevTools

```typescript
// Enable debug mode
localStorage.setItem('debug', 'true');

// Console logging
console.log('Task loaded:', task);
console.table(this.tasks);
```

### Angular DevTools

Install Angular DevTools Chrome extension:
- Component inspector
- Profiler
- Dependency injection tree

### Network Debugging

```bash
# View API calls in Chrome DevTools
# Network tab → Filter: XHR
```

## 📦 Build for Production

```bash
# Production build
ng build --configuration production

# Build stats
ng build --configuration production --stats-json

# Analyze bundle size
npx webpack-bundle-analyzer dist/frontend/browser/stats.json
```

### Deployment

```bash
# Deploy to Nginx
cp -r dist/frontend/browser/* /usr/share/nginx/html/

# Deploy to Apache
cp -r dist/frontend/browser/* /var/www/html/

# Deploy to Firebase
firebase deploy

# Deploy to Netlify
netlify deploy --prod --dir=dist/frontend/browser
```

## 🚢 Docker Deployment

```bash
# Build Docker image
docker build -t frontend:latest -f ../../devops/docker/dockerfiles/web.Dockerfile .

# Run container
docker run -d -p 80:80 frontend:latest
```

## 🔄 State Management (Future)

Planned state management with NgRx:
- [ ] Centralized state
- [ ] Redux DevTools
- [ ] Time-travel debugging
- [ ] Effects for side effects

## 🌐 Internationalization (Future)

Planned i18n support:
- [ ] Multiple languages
- [ ] Date/time formatting
- [ ] Number formatting
- [ ] RTL support

## 📚 Learning Resources

- [Angular Documentation](https://angular.io/docs)
- [RxJS Guide](https://rxjs.dev/guide/overview)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Angular Style Guide](https://angular.io/guide/styleguide)

## 🤝 Contributing

1. Follow Angular style guide
2. Write unit tests
3. Ensure accessibility (a11y)
4. Document complex logic
5. Keep components small and focused

## 📄 License

MIT License - Free to use for learning purposes