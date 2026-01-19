package com.learning.taskmanagement.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void allArgsConstructor_setsFields() {
        Task task = new Task(1L, "t", "d", "IN_PROGRESS");

        assertThat(task.getId()).isEqualTo(1L);
        assertThat(task.getTitle()).isEqualTo("t");
        assertThat(task.getDescription()).isEqualTo("d");
        assertThat(task.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void prePersist_setsCreatedAndUpdated() {
        Task task = new Task();
        task.onCreate();

        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getUpdatedAt()).isNotNull();
    }

    @Test
    void preUpdate_updatesUpdatedAtOnly() {
        Task task = new Task();
        task.onCreate();
        LocalDateTime firstUpdated = task.getUpdatedAt();

        task.onUpdate();

        assertThat(task.getUpdatedAt()).isAfterOrEqualTo(firstUpdated);
    }
}