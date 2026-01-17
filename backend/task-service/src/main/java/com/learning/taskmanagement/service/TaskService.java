package com.learning.taskmanagement.service;

import com.learning.taskmanagement.event.TaskEvent;
import com.learning.taskmanagement.model.Task;
import com.learning.taskmanagement.producer.TaskEventProducer;
import com.learning.taskmanagement.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskEventProducer taskEventProducer) {
        this.taskRepository = taskRepository;
        this.taskEventProducer = taskEventProducer;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> searchTasks(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Transactional
    public Task createTask(Task task) {
        Task savedTask = taskRepository.save(task);

        // Publish event to Kafka
        TaskEvent event = new TaskEvent(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getStatus(),
                "CREATED"
        );
        taskEventProducer.sendTaskEvent(event);

        return savedTask;
    }

    @Transactional
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());

        Task updatedTask = taskRepository.save(task);

        // Publish event to Kafka
        TaskEvent event = new TaskEvent(
                updatedTask.getId(),
                updatedTask.getTitle(),
                updatedTask.getDescription(),
                updatedTask.getStatus(),
                "UPDATED"
        );
        taskEventProducer.sendTaskEvent(event);

        return updatedTask;
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        taskRepository.deleteById(id);

        // Publish event to Kafka
        TaskEvent event = new TaskEvent(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                "DELETED"
        );
        taskEventProducer.sendTaskEvent(event);
    }
}