import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Task {
  id?: number;
  title: string;
  description?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  createdAt?: string;
  updatedAt?: string;
}

export interface TaskAttachment {
  id?: number;
  taskId: number;
  fileName: string;
  originalFileName: string;
  contentType: string;
  fileSize: number;
  uploadedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  // Use Kong Gateway as the API endpoint
  private apiUrl = 'http://localhost:8000/api/tasks';

  constructor(private http: HttpClient) {}

  getAllTasks(status?: string, search?: string): Observable<Task[]> {
    let params = new HttpParams();
    if (status && status.trim()) {
      params = params.set('status', status);
    }
    if (search && search.trim()) {
      params = params.set('search', search);
    }

    return this.http.get<Task[]>(this.apiUrl, { params });
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  createTask(task: Task): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, task);
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // File operations
  uploadFile(taskId: number, file: File): Observable<TaskAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<TaskAttachment>(`${this.apiUrl}/${taskId}/attachments`, formData);
  }

  getAttachments(taskId: number): Observable<TaskAttachment[]> {
    return this.http.get<TaskAttachment[]>(`${this.apiUrl}/${taskId}/attachments`);
  }

  downloadFile(taskId: number, attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${taskId}/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    });
  }

  getDownloadUrl(taskId: number, attachmentId: number): Observable<{url: string, fileName: string}> {
    return this.http.get<{url: string, fileName: string}>(`${this.apiUrl}/${taskId}/attachments/${attachmentId}/url`);
  }

  deleteAttachment(taskId: number, attachmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${taskId}/attachments/${attachmentId}`);
  }
}
