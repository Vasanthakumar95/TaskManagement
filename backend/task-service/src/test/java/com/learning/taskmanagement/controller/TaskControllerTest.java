package com.learning.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.taskmanagement.model.Task;
import com.learning.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("Task Controller Integration Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    @DisplayName("GET /api/tasks - Should return all tasks")
    void shouldGetAllTasks() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(sampleTask);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Task")))
                .andExpect(jsonPath("$[0].status", is("TODO")));

        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    @DisplayName("GET /api/tasks?status=TODO - Should return filtered tasks")
    void shouldGetTasksByStatus() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(sampleTask);
        when(taskService.getTasksByStatus("TODO")).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("TODO")));

        verify(taskService, times(1)).getTasksByStatus("TODO");
    }

    @Test
    @DisplayName("GET /api/tasks?search=Test - Should return searched tasks")
    void shouldSearchTasks() throws Exception {
        // Given
        List<Task> tasks = Arrays.asList(sampleTask);
        when(taskService.searchTasks("Test")).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks").param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", containsString("Test")));

        verify(taskService, times(1)).searchTasks("Test");
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return task by ID")
    void shouldGetTaskById() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(Optional.of(sampleTask));

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")));

        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - Should return 404 when task not found")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        // Given
        when(taskService.getTaskById(99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());

        verify(taskService, times(1)).getTaskById(99L);
    }

    @Test
    @DisplayName("POST /api/tasks - Should create new task")
    void shouldCreateTask() throws Exception {
        // Given
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setStatus("TODO");

        Task savedTask = new Task();
        savedTask.setId(2L);
        savedTask.setTitle("New Task");
        savedTask.setStatus("TODO");

        when(taskService.createTask(any(Task.class))).thenReturn(savedTask);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.title", is("New Task")));

        verify(taskService, times(1)).createTask(any(Task.class));
    }

    @Test
    @DisplayName("POST /api/tasks - Should return 400 when title is missing")
    void shouldReturn400WhenTitleMissing() throws Exception {
        // Given
        Task invalidTask = new Task();
        invalidTask.setStatus("TODO");
        // Title is missing

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Should update task")
    void shouldUpdateTask() throws Exception {
        // Given
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setStatus("DONE");

        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(sampleTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(taskService, times(1)).updateTask(eq(1L), any(Task.class));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Should return 404 when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        // Given
        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setStatus("DONE");

        when(taskService.updateTask(eq(99L), any(Task.class)))
                .thenThrow(new RuntimeException("Task not found"));

        // When & Then
        mockMvc.perform(put("/api/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Should delete task")
    void shouldDeleteTask() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTask(1L);
    }
}