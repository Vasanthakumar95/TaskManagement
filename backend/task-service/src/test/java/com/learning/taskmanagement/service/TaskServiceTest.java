package com.learning.taskmanagement.service;

import com.learning.taskmanagement.event.TaskEvent;
import com.learning.taskmanagement.model.Task;
import com.learning.taskmanagement.producer.TaskEventProducer;
import com.learning.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Task Service Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskEventProducer taskEventProducer;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Description");
        sampleTask.setStatus("TODO");
    }

    @Test
    @DisplayName("Should get all tasks")
    void shouldGetAllTasks() {
        // Given
        List<Task> tasks = Arrays.asList(sampleTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get task by ID")
    void shouldGetTaskById() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        // When
        Optional<Task> result = taskService.getTaskById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Task");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when task not found")
    void shouldReturnEmptyWhenTaskNotFound() {
        // Given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskById(99L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should create task and publish event")
    void shouldCreateTaskAndPublishEvent() {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setStatus("TODO");

        Task savedTask = new Task();
        savedTask.setId(2L);
        savedTask.setTitle("New Task");
        savedTask.setStatus("TODO");

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        doNothing().when(taskEventProducer).sendTaskEvent(any(TaskEvent.class));

        // When
        Task result = taskService.createTask(newTask);

        // Then
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Task");

        // Verify event was published
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(taskEventProducer, times(1)).sendTaskEvent(eventCaptor.capture());

        TaskEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getEventType()).isEqualTo("CREATED");
        assertThat(capturedEvent.getTaskId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should update task and publish event")
    void shouldUpdateTaskAndPublishEvent() {
        // Given
        Task updatedDetails = new Task();
        updatedDetails.setTitle("Updated Task");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setStatus("IN_PROGRESS");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // When
        Task result = taskService.updateTask(1L, updatedDetails);

        // Then
        assertThat(result.getTitle()).isEqualTo("Updated Task");
        verify(taskEventProducer, times(1)).sendTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent task")
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        // Given
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.updateTask(99L, sampleTask))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id: 99");
    }

    @Test
    @DisplayName("Should delete task and publish event")
    void shouldDeleteTaskAndPublishEvent() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        doNothing().when(taskRepository).deleteById(1L);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository, times(1)).deleteById(1L);
        verify(taskEventProducer, times(1)).sendTaskEvent(any(TaskEvent.class));
    }

    @Test
    @DisplayName("Should get tasks by status")
    void shouldGetTasksByStatus() {
        // Given
        List<Task> todoTasks = Arrays.asList(sampleTask);
        when(taskRepository.findByStatus("TODO")).thenReturn(todoTasks);

        // When
        List<Task> result = taskService.getTasksByStatus("TODO");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("TODO");
    }

    @Test
    @DisplayName("Should search tasks by title")
    void shouldSearchTasksByTitle() {
        // Given
        List<Task> matchingTasks = Arrays.asList(sampleTask);
        when(taskRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(matchingTasks);

        // When
        List<Task> result = taskService.searchTasks("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Test");
    }
}