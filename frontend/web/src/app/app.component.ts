import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService, Task, TaskAttachment } from './services/task.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  tasks: Task[] = [];
  filteredTasks: Task[] = [];
  selectedTask: Task | null = null;
  isEditing = false;

  newTask: Task = {
    title: '',
    description: '',
    status: 'TODO'
  };

  filterStatus: string = '';
  searchTerm: string = '';

  // File upload properties
  showFileUpload = false;
  currentTaskForUpload: Task | null = null;
  taskAttachments: Map<number, TaskAttachment[]> = new Map();
  isDragging = false;
  uploadProgress = false;

  // Files for new task
  newTaskFiles: File[] = [];
  isNewTaskDragging = false;

  constructor(
    private taskService: TaskService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('Component initialized, loading tasks...');
    this.loadTasks();
  }

  loadTasks(): void {
    const status = this.filterStatus || undefined;
    const search = this.searchTerm || undefined;

    console.log('Loading tasks with status:', status, 'search:', search);

    this.taskService.getAllTasks(status, search)
      .subscribe({
        next: (tasks) => {
          console.log('Tasks loaded:', tasks);
          this.tasks = tasks;
          this.filteredTasks = tasks;
          // Load attachments for each task
          tasks.forEach(task => {
            if (task.id) {
              this.loadAttachments(task.id);
            }
          });
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading tasks:', error);
          console.error('Full error object:', JSON.stringify(error));
        }
      });
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.searchTerm = '';
    this.loadTasks();
  }

  searchTasks(): void {
    this.filterStatus = '';
    this.loadTasks();
  }

  createTask(): void {
    if (!this.newTask.title.trim()) {
      alert('Title is required');
      return;
    }

    this.taskService.createTask(this.newTask)
      .subscribe({
        next: (createdTask) => {
          // If there are files, upload them
          if (this.newTaskFiles.length > 0 && createdTask.id) {
            this.uploadNewTaskFiles(createdTask.id);
          } else {
            this.loadTasks();
            this.resetForm();
          }
        },
        error: (error) => console.error('Error creating task:', error)
      });
  }

  editTask(task: Task): void {
    this.selectedTask = { ...task };
    this.isEditing = true;
  }

  updateTask(): void {
    if (this.selectedTask && this.selectedTask.id) {
      this.taskService.updateTask(this.selectedTask.id, this.selectedTask)
        .subscribe({
          next: () => {
            this.loadTasks();
            this.cancelEdit();
          },
          error: (error) => console.error('Error updating task:', error)
        });
    }
  }

  deleteTask(id: number | undefined): void {
    if (!id || !confirm('Are you sure you want to delete this task?')) {
      return;
    }

    this.taskService.deleteTask(id)
      .subscribe({
        next: () => this.loadTasks(),
        error: (error) => console.error('Error deleting task:', error)
      });
  }

  cancelEdit(): void {
    this.selectedTask = null;
    this.isEditing = false;
  }

  resetForm(): void {
    this.newTask = {
      title: '',
      description: '',
      status: 'TODO'
    };
    this.newTaskFiles = [];
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'TODO': return '#ff9800';
      case 'IN_PROGRESS': return '#2196f3';
      case 'DONE': return '#4caf50';
      default: return '#757575';
    }
  }

  // File upload methods
  openFileUpload(task: Task): void {
    this.currentTaskForUpload = task;
    this.showFileUpload = true;
  }

  closeFileUpload(): void {
    this.showFileUpload = false;
    this.currentTaskForUpload = null;
    this.isDragging = false;
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadFiles(files);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadFiles(input.files);
    }
  }

  uploadFiles(files: FileList): void {
    if (!this.currentTaskForUpload?.id) return;

    this.uploadProgress = true;
    const taskId = this.currentTaskForUpload.id;

    Array.from(files).forEach(file => {
      this.taskService.uploadFile(taskId, file)
        .subscribe({
          next: (attachment) => {
            console.log('File uploaded:', attachment);
            this.loadAttachments(taskId);
            this.uploadProgress = false;
          },
          error: (error) => {
            console.error('Error uploading file:', error);
            this.uploadProgress = false;
            alert('Error uploading file: ' + file.name);
          }
        });
    });
  }

  loadAttachments(taskId: number): void {
    this.taskService.getAttachments(taskId)
      .subscribe({
        next: (attachments) => {
          this.taskAttachments.set(taskId, attachments);
          this.cdr.detectChanges();
        },
        error: (error) => console.error('Error loading attachments:', error)
      });
  }

  getAttachments(taskId: number | undefined): TaskAttachment[] {
    if (!taskId) return [];
    return this.taskAttachments.get(taskId) || [];
  }

  downloadAttachment(taskId: number, attachment: TaskAttachment): void {
    if (!attachment.id) return;

    this.taskService.downloadFile(taskId, attachment.id)
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = attachment.originalFileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: (error) => console.error('Error downloading file:', error)
      });
  }

  deleteAttachment(taskId: number, attachment: TaskAttachment): void {
    if (!attachment.id || !confirm('Delete this file?')) return;

    this.taskService.deleteAttachment(taskId, attachment.id)
      .subscribe({
        next: () => {
          this.loadAttachments(taskId);
        },
        error: (error) => console.error('Error deleting attachment:', error)
      });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }

  // New Task File Upload Methods
  onNewTaskDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isNewTaskDragging = true;
  }

  onNewTaskDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isNewTaskDragging = false;
  }

  onNewTaskDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isNewTaskDragging = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.addNewTaskFiles(files);
    }
  }

  onNewTaskFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.addNewTaskFiles(input.files);
    }
  }

  addNewTaskFiles(fileList: FileList): void {
    Array.from(fileList).forEach(file => {
      this.newTaskFiles.push(file);
    });
  }

  removeNewTaskFile(index: number): void {
    this.newTaskFiles.splice(index, 1);
  }

  uploadNewTaskFiles(taskId: number): void {
    let uploadCount = 0;
    const totalFiles = this.newTaskFiles.length;

    this.newTaskFiles.forEach(file => {
      this.taskService.uploadFile(taskId, file)
        .subscribe({
          next: () => {
            uploadCount++;
            if (uploadCount === totalFiles) {
              this.loadTasks();
              this.resetForm();
            }
          },
          error: (error) => {
            console.error('Error uploading file:', error);
            uploadCount++;
            if (uploadCount === totalFiles) {
              this.loadTasks();
              this.resetForm();
            }
          }
        });
    });
  }
}
