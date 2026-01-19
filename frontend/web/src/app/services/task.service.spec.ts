import { TestBed } from '@angular/core/testing';
import {
  provideHttpClient,
  withInterceptorsFromDi,
  HttpParams,
} from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController,
} from '@angular/common/http/testing';

import { TaskService, Task, TaskAttachment } from './task.service';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const apiUrl = 'http://localhost:8000/api/tasks';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        TaskService,
      ],
    });

    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getAllTasks() calls GET /api/tasks with no params when inputs empty', () => {
    service.getAllTasks('', '   ').subscribe();

    const req = httpMock.expectOne((r) => r.method === 'GET' && r.url === apiUrl);
    expect(req.request.params.keys().length).toBe(0);
    req.flush([]);
  });

  it('getAllTasks() adds status + search params when provided', () => {
    service.getAllTasks('TODO', 'report').subscribe();

    const req = httpMock.expectOne((r) => r.method === 'GET' && r.url === apiUrl);
    expect(req.request.params.get('status')).toBe('TODO');
    expect(req.request.params.get('search')).toBe('report');
    req.flush([]);
  });

  it('getTaskById() calls GET /api/tasks/:id', () => {
    service.getTaskById(10).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/10`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 10, title: 't', status: 'TODO' } satisfies Task);
  });

  it('createTask() calls POST /api/tasks with body', () => {
    const payload: Task = { title: 'New', description: 'D', status: 'TODO' };

    service.createTask(payload).subscribe((created) => {
      expect(created.title).toBe('New');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush({ id: 1, ...payload });
  });

  it('updateTask() calls PUT /api/tasks/:id with body', () => {
    const payload: Task = { id: 1, title: 'Updated', status: 'DONE' };

    service.updateTask(1, payload).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush(payload);
  });

  it('deleteTask() calls DELETE /api/tasks/:id', () => {
    service.deleteTask(7).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('uploadFile() posts FormData to /api/tasks/:taskId/attachments', () => {
    const file = new File(['hello'], 'hello.txt', { type: 'text/plain' });

    service.uploadFile(5, file).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/5/attachments`);
    expect(req.request.method).toBe('POST');

    const body = req.request.body as FormData;
    expect(body instanceof FormData).toBe(true);
    expect(body.get('file')).toBe(file);

    req.flush({
      id: 1,
      taskId: 5,
      fileName: 'stored',
      originalFileName: 'hello.txt',
      contentType: 'text/plain',
      fileSize: 5,
    } satisfies TaskAttachment);
  });

  it('getAttachments() calls GET /api/tasks/:taskId/attachments', () => {
    service.getAttachments(5).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/5/attachments`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('downloadFile() requests blob responseType', () => {
    service.downloadFile(5, 99).subscribe((blob) => {
      expect(blob).toBeInstanceOf(Blob);
    });

    const req = httpMock.expectOne(`${apiUrl}/5/attachments/99/download`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');

    req.flush(new Blob(['x'], { type: 'application/octet-stream' }));
  });

  it('getDownloadUrl() calls GET /url and returns {url,fileName}', () => {
    service.getDownloadUrl(5, 99).subscribe((res) => {
      expect(res.url).toBe('http://signed');
      expect(res.fileName).toBe('a.txt');
    });

    const req = httpMock.expectOne(`${apiUrl}/5/attachments/99/url`);
    expect(req.request.method).toBe('GET');
    req.flush({ url: 'http://signed', fileName: 'a.txt' });
  });

  it('deleteAttachment() calls DELETE /api/tasks/:taskId/attachments/:attachmentId', () => {
    service.deleteAttachment(5, 99).subscribe();

    const req = httpMock.expectOne(`${apiUrl}/5/attachments/99`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});