package com.learning.taskmanagement.controller;

import com.learning.taskmanagement.model.TaskAttachment;
import com.learning.taskmanagement.repository.TaskAttachmentRepository;
import com.learning.taskmanagement.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
public class FileController {

    private final FileStorageService fileStorageService;
    private final TaskAttachmentRepository attachmentRepository;

    @Autowired
    public FileController(FileStorageService fileStorageService,
                          TaskAttachmentRepository attachmentRepository) {
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
    }

    /**
     * Upload file for a task
     */
    @PostMapping
    public ResponseEntity<TaskAttachment> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file) {
        try {
            // Upload to MinIO
            String fileName = fileStorageService.uploadFile(file, taskId);

            // Save metadata to database
            TaskAttachment attachment = new TaskAttachment(
                    taskId,
                    fileName,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );

            TaskAttachment saved = attachmentRepository.save(attachment);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all attachments for a task
     */
    @GetMapping
    public ResponseEntity<List<TaskAttachment>> getAttachments(@PathVariable Long taskId) {
        List<TaskAttachment> attachments = attachmentRepository.findByTaskId(taskId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Download a file
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        try {
            TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found"));

            InputStream fileStream = fileStorageService.downloadFile(attachment.getFileName());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .body(new InputStreamResource(fileStream));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get presigned URL for direct download
     */
    @GetMapping("/{attachmentId}/url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        try {
            TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found"));

            String url = fileStorageService.getPresignedUrl(attachment.getFileName());

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("fileName", attachment.getOriginalFileName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete an attachment
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long taskId,
            @PathVariable Long attachmentId) {
        try {
            TaskAttachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found"));

            // Delete from MinIO
            fileStorageService.deleteFile(attachment.getFileName());

            // Delete from database
            attachmentRepository.deleteById(attachmentId);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
