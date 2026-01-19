package com.learning.taskmanagement.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskAttachmentTest {

    @Test
    void defaultConstructor_initializesUploadedAt() {
        TaskAttachment attachment = new TaskAttachment();
        assertThat(attachment.getUploadedAt()).isNotNull();
    }

    @Test
    void allArgsConstructor_setsFieldsAndUploadedAt() {
        TaskAttachment attachment = new TaskAttachment(1L, "stored", "orig", "text/plain", 10L);

        assertThat(attachment.getTaskId()).isEqualTo(1L);
        assertThat(attachment.getFileName()).isEqualTo("stored");
        assertThat(attachment.getOriginalFileName()).isEqualTo("orig");
        assertThat(attachment.getContentType()).isEqualTo("text/plain");
        assertThat(attachment.getFileSize()).isEqualTo(10L);
        assertThat(attachment.getUploadedAt()).isNotNull();
    }
}