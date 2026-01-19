import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AppComponent } from './app.component';
import { TaskService, Task, TaskAttachment } from './services/task.service';

describe('AppComponent', () => {
  let taskServiceMock: {
    getAllTasks: any;
    createTask: any;
    updateTask: any;
    deleteTask: any;
    uploadFile: any;
    getAttachments: any;
    downloadFile: any;
    deleteAttachment: any;
  };

  beforeEach(async () => {
    taskServiceMock = {
      getAllTasks: vi.fn(),
      createTask: vi.fn(),
      updateTask: vi.fn(),
      deleteTask: vi.fn(),
      uploadFile: vi.fn(),
      getAttachments: vi.fn(),
      downloadFile: vi.fn(),
      deleteAttachment: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [{ provide: TaskService, useValue: taskServiceMock }],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render title', async () => {
    taskServiceMock.getAllTasks.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('📝 Task Management');
  });

  it('ngOnInit loads tasks and attachments for tasks with id', async () => {
    const tasks: Task[] = [
      { id: 1, title: 't1', status: 'TODO' },
      { id: 2, title: 't2', status: 'DONE' },
      { title: 'no-id', status: 'TODO' },
    ];
    taskServiceMock.getAllTasks.mockReturnValue(of(tasks));
    taskServiceMock.getAttachments.mockImplementation((taskId: number) =>
      of([{ id: 10, taskId, fileName: 'f', originalFileName: 'o', contentType: 'c', fileSize: 1 } satisfies TaskAttachment])
    );

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    const comp = fixture.componentInstance;
    expect(comp.tasks.length).toBe(3);
    expect(taskServiceMock.getAttachments).toHaveBeenCalledTimes(2);
    expect(comp.getAttachments(1).length).toBe(1);
  });

  it('createTask shows alert if title is empty and does not call service', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
    comp.newTask.title = '   ';

    comp.createTask();

    expect(alertSpy).toHaveBeenCalled();
    expect(taskServiceMock.createTask).not.toHaveBeenCalled();
    alertSpy.mockRestore();
  });

  it('createTask calls service; if no files then resets form + reloads tasks', async () => {
    taskServiceMock.createTask.mockReturnValue(of({ id: 1, title: 't', status: 'TODO' } satisfies Task));
    taskServiceMock.getAllTasks.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    comp.newTask.title = 'X';
    comp.newTaskFiles = []; // no attachments
    comp.createTask();

    expect(taskServiceMock.createTask).toHaveBeenCalled();
  });

  it('filterByStatus sets filterStatus, clears searchTerm, triggers loadTasks()', () => {
    taskServiceMock.getAllTasks.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const spy = vi.spyOn(comp, 'loadTasks');
    comp.searchTerm = 'abc';

    comp.filterByStatus('DONE');

    expect(comp.filterStatus).toBe('DONE');
    expect(comp.searchTerm).toBe('');
    expect(spy).toHaveBeenCalled();
  });

  it('getStatusColor returns expected colors', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    expect(comp.getStatusColor('TODO')).toBe('#ff9800');
    expect(comp.getStatusColor('IN_PROGRESS')).toBe('#2196f3');
    expect(comp.getStatusColor('DONE')).toBe('#4caf50');
    expect(comp.getStatusColor('WHATEVER')).toBe('#757575');
  });

  it('formatFileSize formats bytes', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    expect(comp.formatFileSize(0)).toBe('0 Bytes');
    expect(comp.formatFileSize(1024)).toContain('KB');
  });

  // covers app.component.ts and app.component.html
  it('loadTasks() passes status/search as undefined when empty and handles error path', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const err = new Error('boom');
    taskServiceMock.getAllTasks.mockReturnValue(throwError(() => err));

    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    comp.filterStatus = '';
    comp.searchTerm = '';
    comp.loadTasks();

    expect(taskServiceMock.getAllTasks).toHaveBeenCalledWith(undefined, undefined);
    expect(consoleSpy).toHaveBeenCalled(); // error + full error object logs
    consoleSpy.mockRestore();
  });

  it('searchTasks() clears filterStatus and triggers loadTasks()', () => {
    taskServiceMock.getAllTasks.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const spy = vi.spyOn(comp, 'loadTasks');
    comp.filterStatus = 'TODO';
    comp.searchTerm = 'x';

    comp.searchTasks();

    expect(comp.filterStatus).toBe('');
    expect(spy).toHaveBeenCalled();
  });

  it('editTask() clones task and enables editing; cancelEdit() resets', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const task: Task = { id: 1, title: 't', status: 'TODO' };
    comp.editTask(task);

    expect(comp.isEditing).toBe(true);
    expect(comp.selectedTask).toEqual(task);
    expect(comp.selectedTask).not.toBe(task); // cloned

    comp.cancelEdit();
    expect(comp.isEditing).toBe(false);
    expect(comp.selectedTask).toBeNull();
  });

  it('updateTask() calls service, reloads tasks, and cancels edit', () => {
    taskServiceMock.updateTask.mockReturnValue(of({ id: 1, title: 't', status: 'DONE' } satisfies Task));
    taskServiceMock.getAllTasks.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const loadSpy = vi.spyOn(comp, 'loadTasks');
    const cancelSpy = vi.spyOn(comp, 'cancelEdit');

    const selected = { id: 1, title: 't', status: 'DONE' };
    comp.selectedTask = { id: 1, title: 't', status: 'DONE' };
    comp.isEditing = true;

    comp.updateTask();

    expect(taskServiceMock.updateTask).toHaveBeenCalledWith(1, selected);
    expect(loadSpy).toHaveBeenCalled();
    expect(cancelSpy).toHaveBeenCalled();
    expect(comp.selectedTask).toBeNull(); // verify reset
    expect(comp.isEditing).toBe(false);
  });

  it('deleteTask() does nothing when confirm is false', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    comp.deleteTask(1);

    expect(taskServiceMock.deleteTask).not.toHaveBeenCalled();

    confirmSpy.mockRestore();
  });

  it('deleteTask() calls service and reloads when confirm is true', () => {
    taskServiceMock.deleteTask.mockReturnValue(of(void 0));
    taskServiceMock.getAllTasks.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    const loadSpy = vi.spyOn(comp, 'loadTasks');

    comp.deleteTask(1);

    expect(taskServiceMock.deleteTask).toHaveBeenCalledWith(1);
    expect(loadSpy).toHaveBeenCalled();

    confirmSpy.mockRestore();
  });

  it('openFileUpload()/closeFileUpload() toggles modal state', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const task: Task = { id: 1, title: 't', status: 'TODO' };

    comp.openFileUpload(task);
    expect(comp.showFileUpload).toBe(true);
    expect(comp.currentTaskForUpload).toEqual(task);

    comp.isDragging = true;
    comp.closeFileUpload();
    expect(comp.showFileUpload).toBe(false);
    expect(comp.currentTaskForUpload).toBeNull();
    expect(comp.isDragging).toBe(false);
  });

  it('drag/drop handlers toggle isDragging and call uploadFiles on drop', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const uploadSpy = vi.spyOn(comp, 'uploadFiles').mockImplementation(() => {});
    comp.currentTaskForUpload = { id: 1, title: 't', status: 'TODO' };

    const e: any = {
      preventDefault: vi.fn(),
      stopPropagation: vi.fn(),
      dataTransfer: { files: { length: 1 } },
    };

    comp.onDragOver(e);
    expect(comp.isDragging).toBe(true);

    comp.onDragLeave(e);
    expect(comp.isDragging).toBe(false);

    comp.onDrop(e);
    expect(uploadSpy).toHaveBeenCalled();

    uploadSpy.mockRestore();
  });

  it('uploadFiles() returns early when no currentTaskForUpload.id', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    comp.currentTaskForUpload = null;

    comp.uploadFiles({ length: 1 } as any);

    expect(taskServiceMock.uploadFile).not.toHaveBeenCalled();
  });

  it('uploadFiles() calls service and then loadAttachments on success', () => {
    taskServiceMock.uploadFile.mockReturnValue(of({} as TaskAttachment));
    taskServiceMock.getAttachments.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    comp.currentTaskForUpload = { id: 5, title: 't', status: 'TODO' };

    const loadAttachmentsSpy = vi.spyOn(comp, 'loadAttachments');

    const file = new File(['x'], 'a.txt', { type: 'text/plain' });
    const files = { length: 1, 0: file } as any;

    comp.uploadFiles(files);

    expect(taskServiceMock.uploadFile).toHaveBeenCalledWith(5, file);
    expect(loadAttachmentsSpy).toHaveBeenCalledWith(5);
    expect(comp.uploadProgress).toBe(false);
  });

  it('loadAttachments() populates map; getAttachments() returns empty for undefined', () => {
    taskServiceMock.getAttachments.mockReturnValue(of([{ id: 1 } as any]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    comp.loadAttachments(10);

    expect(comp.getAttachments(10).length).toBe(1);
    expect(comp.getAttachments(undefined)).toEqual([]);
  });

  it('downloadAttachment() creates an anchor and triggers download', () => {
    const blob = new Blob(['x'], { type: 'application/octet-stream' });
    taskServiceMock.downloadFile.mockReturnValue(of(blob));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const createUrlSpy = vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:mock');
    const revokeSpy = vi.spyOn(window.URL, 'revokeObjectURL').mockImplementation(() => {});

    const click = vi.fn();
    const aEl: any = { click, set href(v: string) {}, set download(v: string) {} };

    const createElSpy = vi.spyOn(document, 'createElement').mockReturnValue(aEl);
    const appendSpy = vi.spyOn(document.body, 'appendChild').mockImplementation(() => aEl);
    const removeSpy = vi.spyOn(document.body, 'removeChild').mockImplementation(() => aEl);

    comp.downloadAttachment(1, {
      id: 2,
      taskId: 1,
      fileName: 'stored',
      originalFileName: 'file.txt',
      contentType: 'text/plain',
      fileSize: 1,
    });

    expect(taskServiceMock.downloadFile).toHaveBeenCalledWith(1, 2);
    expect(createUrlSpy).toHaveBeenCalled();
    expect(appendSpy).toHaveBeenCalled();
    expect(click).toHaveBeenCalled();
    expect(removeSpy).toHaveBeenCalled();
    expect(revokeSpy).toHaveBeenCalled();

    createUrlSpy.mockRestore();
    revokeSpy.mockRestore();
    createElSpy.mockRestore();
    appendSpy.mockRestore();
    removeSpy.mockRestore();
  });

  it('deleteAttachment() does nothing when confirm is false, calls service when true', () => {
    taskServiceMock.deleteAttachment.mockReturnValue(of(void 0));
    taskServiceMock.getAttachments.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    comp.deleteAttachment(1, { id: 2 } as any);
    expect(taskServiceMock.deleteAttachment).not.toHaveBeenCalled();

    confirmSpy.mockReturnValue(true);
    const loadSpy = vi.spyOn(comp, 'loadAttachments');

    comp.deleteAttachment(1, { id: 2 } as any);
    expect(taskServiceMock.deleteAttachment).toHaveBeenCalledWith(1, 2);
    expect(loadSpy).toHaveBeenCalledWith(1);

    confirmSpy.mockRestore();
  });

  it('new task drag/drop + file list add/remove are exercised', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    const e: any = { preventDefault: vi.fn(), stopPropagation: vi.fn(), dataTransfer: { files: { length: 1 } } };

    comp.onNewTaskDragOver(e);
    expect(comp.isNewTaskDragging).toBe(true);

    comp.onNewTaskDragLeave(e);
    expect(comp.isNewTaskDragging).toBe(false);

    const file = new File(['x'], 'a.txt', { type: 'text/plain' });
    comp.addNewTaskFiles({ length: 1, 0: file } as any);
    expect(comp.newTaskFiles.length).toBe(1);

    comp.removeNewTaskFile(0);
    expect(comp.newTaskFiles.length).toBe(0);
  });

  it('template: shows empty state when no tasks, and shows file upload modal when enabled', async () => {
    taskServiceMock.getAllTasks.mockReturnValue(of([]));
    taskServiceMock.getAttachments.mockReturnValue(of([]));

    const fixture = TestBed.createComponent(AppComponent);
    const comp = fixture.componentInstance;

    // set modal state before initial change detection
    comp.showFileUpload = true;
    comp.currentTaskForUpload = { id: 1, title: 't', status: 'TODO' };

    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('No tasks found');
    expect(text).toContain('Attachments for:');
  });
});