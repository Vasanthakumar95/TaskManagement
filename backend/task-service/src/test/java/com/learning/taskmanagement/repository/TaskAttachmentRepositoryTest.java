package com.learning.taskmanagement.repository;

import com.learning.taskmanagement.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskAttachmentRepositoryTest {

    @Autowired
    private TaskAttachmentRepository repository;

    @Test
    void findByTaskId_returnsAttachments() {
        TaskAttachment a1 = new TaskAttachment(1L, "f1", "orig1", "text/plain", 1L);
        TaskAttachment a2 = new TaskAttachment(1L, "f2", "orig2", "text/plain", 2L);
        TaskAttachment a3 = new TaskAttachment(2L, "f3", "orig3", "text/plain", 3L);

        repository.save(a1);
        repository.save(a2);
        repository.save(a3);

        List<TaskAttachment> result = repository.findByTaskId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteByTaskId_removesAttachments() {
        TaskAttachment a1 = new TaskAttachment(1L, "f1", "orig1", "text/plain", 1L);
        TaskAttachment a2 = new TaskAttachment(1L, "f2", "orig2", "text/plain", 2L);

        repository.save(a1);
        repository.save(a2);

        repository.deleteByTaskId(1L);

        assertThat(repository.findByTaskId(1L)).isEmpty();
    }
}