package com.learning.taskmanagement.producer;

import com.learning.taskmanagement.event.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class TaskEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventProducer.class);

    @Value("${kafka.topic.task-events}")
    private String taskEventsTopic;

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    @Autowired
    public TaskEventProducer(KafkaTemplate<String, TaskEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTaskEvent(TaskEvent event) {
        logger.info("Sending task event: {}", event);

        Message<TaskEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, taskEventsTopic)
                .setHeader(KafkaHeaders.KEY, event.getTaskId().toString())
                .build();

        kafkaTemplate.send(message);
        logger.info("Task event sent successfully");
    }
}