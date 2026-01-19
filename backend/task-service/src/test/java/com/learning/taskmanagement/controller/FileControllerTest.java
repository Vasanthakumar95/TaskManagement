package com.learning.taskmanagement.controller;

import com.learning.taskmanagement.model.TaskAttachment;
import com.learning.taskmanagement.repository.TaskAttachmentRepository;
import com.learning.taskmanagement.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FileControllerTest {

    private FileStorageService fileStorageService;
    private TaskAttachmentRepository attachmentRepository;
    private FileController fileController;

    @BeforeEach
    void setUp() {
        fileStorageService = mock(FileStorageService.class);
        attachmentRepository = mock(TaskAttachmentRepository.class);
        fileController = new FileController(fileStorageService, attachmentRepository);
    }

    @Test
    void uploadFile_success() throws Exception {
        Long taskId = 1L;
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getSize()).thenReturn(10L);
        when(fileStorageService.uploadFile(file, taskId)).thenReturn("1_uuid_test.txt");

        TaskAttachment saved = new TaskAttachment(taskId, "1_uuid_test.txt", "test.txt", "text/plain", 10L);
        when(attachmentRepository.save(ArgumentMatchers.any(TaskAttachment.class))).thenReturn(saved);

        ResponseEntity<TaskAttachment> response = fileController.uploadFile(taskId, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isNotNull();
        assertEquals("1_uuid_test.txt", response.getBody().getFileName());
        verify(fileStorageService).uploadFile(file, taskId);
        verify(attachmentRepository).save(any(TaskAttachment.class));
    }

    @Test
    void uploadFile_failureReturns500() throws Exception {
        Long taskId = 1L;
        MultipartFile file = mock(MultipartFile.class);
        when(fileStorageService.uploadFile(file, taskId)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<TaskAttachment> response = fileController.uploadFile(taskId, file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getAttachments_returnsList() {
        Long taskId = 1L;
        List<TaskAttachment> list = Collections.singletonList(
                new TaskAttachment(taskId, "file", "orig", "text/plain", 10L)
        );
        when(attachmentRepository.findByTaskId(taskId)).thenReturn(list);

        ResponseEntity<List<TaskAttachment>> response = fileController.getAttachments(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        verify(attachmentRepository).findByTaskId(taskId);
    }

    @Test
    void downloadFile_success() throws Exception {
        Long taskId = 1L;
        Long attachmentId = 2L;

        TaskAttachment attachment = new TaskAttachment(taskId, "stored-name", "orig.txt", "text/plain", 10L);
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        InputStream is = new ByteArrayInputStream("data".getBytes());
        when(fileStorageService.downloadFile("stored-name")).thenReturn(is);

        ResponseEntity<InputStreamResource> response = fileController.downloadFile(taskId, attachmentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("orig.txt");
        verify(fileStorageService).downloadFile("stored-name");
    }

    @Test
    void downloadFile_failureReturns500() throws Exception {
        Long taskId = 1L;
        Long attachmentId = 2L;

        when(attachmentRepository.findById(attachmentId)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<InputStreamResource> response = fileController.downloadFile(taskId, attachmentId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getDownloadUrl_success() throws Exception {
        Long taskId = 1L;
        Long attachmentId = 2L;
        TaskAttachment attachment = new TaskAttachment(taskId, "stored", "orig.txt", "text/plain", 10L);
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(fileStorageService.getPresignedUrl("stored")).thenReturn("http://url");

        ResponseEntity<Map<String, String>> response = fileController.getDownloadUrl(taskId, attachmentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).containsEntry("url", "http://url");
        assertThat(response.getBody()).containsEntry("fileName", "orig.txt");
    }

    @Test
    void getDownloadUrl_failureReturns500() throws Exception {
        Long taskId = 1L;
        Long attachmentId = 2L;

        when(attachmentRepository.findById(attachmentId)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Map<String, String>> response = fileController.getDownloadUrl(taskId, attachmentId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteAttachment_success() throws Exception {
        Long taskId = 1L;
        Long attachmentId = 2L;
        TaskAttachment attachment = new TaskAttachment(taskId, "stored", "orig.txt", "text/plain", 10L);
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        ResponseEntity<Void> response = fileController.deleteAttachment(taskId, attachmentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(fileStorageService).deleteFile("stored");
        verify(attachmentRepository).deleteById(attachmentId);
    }

    @Test
    void deleteAttachment_failureReturns500() throws Exception {
        Long taskId = 1L;
        Long attachmentId = 2L;

        when(attachmentRepository.findById(attachmentId)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<Void> response = fileController.deleteAttachment(taskId, attachmentId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}