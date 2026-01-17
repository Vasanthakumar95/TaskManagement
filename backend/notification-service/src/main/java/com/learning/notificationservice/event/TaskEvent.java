package com.learning.notificationservice.event;

import java.time.LocalDateTime;

public class TaskEvent {
    private Long taskId;
    private String title;
    private String description;
    private String status;
    private String eventType;
    private LocalDateTime timestamp;

    // Constructors
    public TaskEvent() {}

    public TaskEvent(Long taskId, String title, String description, String status, String eventType) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TaskEvent{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}