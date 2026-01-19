package com.learning.notificationservice.consumer;

import com.learning.notificationservice.event.TaskEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class TaskEventConsumerTest {

    @Test
    void consumeTaskEvent_handlesCreatedUpdatedDeletedAndUnknown() {
        TaskEventConsumer consumer = new TaskEventConsumer();

        TaskEvent created = new TaskEvent(1L, "t1", "d1", "TODO", "CREATED");
        TaskEvent updated = new TaskEvent(2L, "t2", "d2", "IN_PROGRESS", "UPDATED");
        TaskEvent deleted = new TaskEvent(3L, "t3", "d3", "DONE", "DELETED");
        TaskEvent unknown = new TaskEvent(4L, "t4", "d4", "DONE", "UNKNOWN");

        assertThatCode(() -> consumer.consumeTaskEvent(created)).doesNotThrowAnyException();
        assertThatCode(() -> consumer.consumeTaskEvent(updated)).doesNotThrowAnyException();
        assertThatCode(() -> consumer.consumeTaskEvent(deleted)).doesNotThrowAnyException();
        assertThatCode(() -> consumer.consumeTaskEvent(unknown)).doesNotThrowAnyException();
    }
}