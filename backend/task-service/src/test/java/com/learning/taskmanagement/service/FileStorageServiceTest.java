package com.learning.taskmanagement.service;

import io.minio.*;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private MinioClient minioClient;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        minioClient = mock(MinioClient.class);
        fileStorageService = new FileStorageService(minioClient);
        ReflectionTestUtils.setField(fileStorageService, "bucketName", "test-bucket");
    }

    @Test
    void uploadFile_callsMinioAndReturnsName() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getSize()).thenReturn(5L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String name = fileStorageService.uploadFile(file, 1L);

        assertThat(name).contains("1_");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void downloadFile_returnsInputStream() throws Exception {
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        InputStream result = fileStorageService.downloadFile("file.txt");

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(response); // optional but clearer
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void deleteFile_callsMinio() throws Exception {
        fileStorageService.deleteFile("file.txt");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void getPresignedUrl_returnsUrl() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://presigned");

        String url = fileStorageService.getPresignedUrl("file.txt");

        assertThat(url).isEqualTo("http://presigned");
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void fileExists_trueWhenStatSucceeds() throws Exception {
        StatObjectResponse response = mock(StatObjectResponse.class);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(response);

        boolean exists = fileStorageService.fileExists("file.txt");

        assertThat(exists).isTrue();
    }

    @Test
    void fileExists_falseWhenStatThrows() throws Exception {
        doThrow(new RuntimeException("boom")).when(minioClient).statObject(any(StatObjectArgs.class));

        boolean exists = fileStorageService.fileExists("file.txt");

        assertThat(exists).isFalse();
    }
}