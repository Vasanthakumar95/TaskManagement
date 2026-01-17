package com.learning.notificationservice.consumer;

import com.learning.notificationservice.event.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TaskEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TaskEventConsumer.class);

    @KafkaListener(
            topics = "${kafka.topic.task-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTaskEvent(TaskEvent event) {
        logger.info("========================================");
        logger.info("📬 Received Task Event!");
        logger.info("Event Type: {}", event.getEventType());
        logger.info("Task ID: {}", event.getTaskId());
        logger.info("Title: {}", event.getTitle());
        logger.info("Status: {}", event.getStatus());
        logger.info("Timestamp: {}", event.getTimestamp());
        logger.info("========================================");

        // Here you can add your notification logic:
        // - Send email
        // - Send push notification
        // - Update analytics
        // - Trigger other services

        switch (event.getEventType()) {
            case "CREATED":
                handleTaskCreated(event);
                break;
            case "UPDATED":
                handleTaskUpdated(event);
                break;
            case "DELETED":
                handleTaskDeleted(event);
                break;
            default:
                logger.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleTaskCreated(TaskEvent event) {
        logger.info("✅ Processing CREATED event for task: {}", event.getTitle());
        // TODO: Send "New task created" notification
        // Example: emailService.sendTaskCreatedEmail(event);
    }

    private void handleTaskUpdated(TaskEvent event) {
        logger.info("🔄 Processing UPDATED event for task: {}", event.getTitle());
        // TODO: Send "Task updated" notification
        // Example: emailService.sendTaskUpdatedEmail(event);
    }

    private void handleTaskDeleted(TaskEvent event) {
        logger.info("🗑️ Processing DELETED event for task: {}", event.getTitle());
        // TODO: Send "Task deleted" notification
        // Example: emailService.sendTaskDeletedEmail(event);
    }
}