package com.learning.taskmanagement.repository;

import com.learning.taskmanagement.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Task Repository Tests")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        sampleTask = new Task();
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Description");
        sampleTask.setStatus("TODO");
    }

    @Test
    @DisplayName("Should save and retrieve task")
    void shouldSaveAndRetrieveTask() {
        // When
        Task savedTask = taskRepository.save(sampleTask);

        // Then
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Test Task");
        assertThat(savedTask.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find task by ID")
    void shouldFindTaskById() {
        // Given
        Task savedTask = taskRepository.save(sampleTask);

        // When
        Optional<Task> found = taskRepository.findById(savedTask.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("Should find tasks by status")
    void shouldFindTasksByStatus() {
        // Given
        taskRepository.save(sampleTask);

        Task doneTask = new Task();
        doneTask.setTitle("Done Task");
        doneTask.setStatus("DONE");
        taskRepository.save(doneTask);

        // When
        List<Task> todoTasks = taskRepository.findByStatus("TODO");
        List<Task> doneTasks = taskRepository.findByStatus("DONE");

        // Then
        assertThat(todoTasks).hasSize(1);
        assertThat(todoTasks.get(0).getStatus()).isEqualTo("TODO");
        assertThat(doneTasks).hasSize(1);
        assertThat(doneTasks.get(0).getStatus()).isEqualTo("DONE");
    }

    @Test
    @DisplayName("Should find tasks by title containing (case insensitive)")
    void shouldFindTasksByTitleContaining() {
        // Given
        taskRepository.save(sampleTask);

        Task anotherTask = new Task();
        anotherTask.setTitle("Meeting with Team");
        anotherTask.setStatus("TODO");
        taskRepository.save(anotherTask);

        // When
        List<Task> tasksWithTest = taskRepository.findByTitleContainingIgnoreCase("test");
        List<Task> tasksWithMeeting = taskRepository.findByTitleContainingIgnoreCase("MEETING");

        // Then
        assertThat(tasksWithTest).hasSize(1);
        assertThat(tasksWithTest.get(0).getTitle()).contains("Test");
        assertThat(tasksWithMeeting).hasSize(1);
        assertThat(tasksWithMeeting.get(0).getTitle()).contains("Meeting");
    }

    @Test
    @DisplayName("Should update task")
    void shouldUpdateTask() {
        // Given
        Task savedTask = taskRepository.save(sampleTask);

        // When
        savedTask.setTitle("Updated Title");
        savedTask.setStatus("IN_PROGRESS");
        Task updatedTask = taskRepository.save(savedTask);

        // Then
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updatedTask.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should delete task")
    void shouldDeleteTask() {
        // Given
        Task savedTask = taskRepository.save(sampleTask);
        Long taskId = savedTask.getId();

        // When
        taskRepository.deleteById(taskId);

        // Then
        Optional<Task> deleted = taskRepository.findById(taskId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should find all tasks")
    void shouldFindAllTasks() {
        // Given
        taskRepository.save(sampleTask);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setStatus("TODO");
        taskRepository.save(task2);

        // When
        List<Task> allTasks = taskRepository.findAll();

        // Then
        assertThat(allTasks).hasSize(2);
    }

    @Test
    @DisplayName("Should set timestamps on create")
    void shouldSetTimestampsOnCreate() {
        // When
        Task savedTask = taskRepository.save(sampleTask);

        // Then
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();
        // Check timestamps are within 1 second of each other (account for microsecond differences)
        assertThat(savedTask.getCreatedAt()).isCloseTo(
                savedTask.getUpdatedAt(),
                within(1, java.time.temporal.ChronoUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("Should update timestamp on update")
    void shouldUpdateTimestampOnUpdate() throws InterruptedException {
        // Given
        Task savedTask = taskRepository.save(sampleTask);
        Thread.sleep(1000); // 1 second delay to ensure different timestamp

        // When
        savedTask.setTitle("Updated");
        Task updatedTask = taskRepository.save(savedTask);

        // Then
        assertThat(updatedTask.getUpdatedAt()).isAfter(updatedTask.getCreatedAt());
    }
}