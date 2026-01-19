package com.learning.taskmanagement.producer;

import com.learning.taskmanagement.event.TaskEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskEventProducerTest {

    private KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private TaskEventProducer producer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        producer = new TaskEventProducer(kafkaTemplate);
        ReflectionTestUtils.setField(producer, "taskEventsTopic", "task-events");
    }

    @Test
    void sendTaskEvent_buildsMessageAndSends() {
        TaskEvent event = new TaskEvent(1L, "title", "desc", "TODO", "CREATED");

        producer.sendTaskEvent(event);

        ArgumentCaptor<Message<TaskEvent>> captor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(captor.capture());
        Message<TaskEvent> msg = captor.getValue();
        assertThat(msg.getPayload()).isEqualTo(event);
        assertThat(msg.getHeaders().get("kafka_topic")).isEqualTo("task-events");
        assertThat(msg.getHeaders().get("kafka_messageKey")).isEqualTo("1");
    }
}