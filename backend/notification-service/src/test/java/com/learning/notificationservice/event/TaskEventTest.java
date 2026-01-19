package com.learning.notificationservice.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEventTest {

    @Test
    void defaultConstructor_leavesTimestampNull() {
        TaskEvent event = new TaskEvent();
        assertThat(event.getTimestamp()).isNull();
    }

    @Test
    void allArgsConstructor_setsFieldsAndTimestamp() {
        TaskEvent event = new TaskEvent(1L, "t", "d", "TODO", "CREATED");

        assertThat(event.getTaskId()).isEqualTo(1L);
        assertThat(event.getTitle()).isEqualTo("t");
        assertThat(event.getDescription()).isEqualTo("d");
        assertThat(event.getStatus()).isEqualTo("TODO");
        assertThat(event.getEventType()).isEqualTo("CREATED");
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    void settersAndGetters_work() {
        TaskEvent event = new TaskEvent();
        LocalDateTime now = LocalDateTime.now();

        event.setTaskId(2L);
        event.setTitle("title");
        event.setDescription("desc");
        event.setStatus("DONE");
        event.setEventType("UPDATED");
        event.setTimestamp(now);

        assertThat(event.getTaskId()).isEqualTo(2L);
        assertThat(event.getTitle()).isEqualTo("title");
        assertThat(event.getDescription()).isEqualTo("desc");
        assertThat(event.getStatus()).isEqualTo("DONE");
        assertThat(event.getEventType()).isEqualTo("UPDATED");
        assertThat(event.getTimestamp()).isEqualTo(now);
    }
}