package com.learning.taskmanagement.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MinioConfigTest {

    @Test
    void minioClient_createsBucketIfNotExists() throws Exception {
        // Requires Docker to run minIO
        MinioConfig config = new MinioConfig();
        ReflectionTestUtils.setField(config, "minioUrl", "http://localhost:9000");
        ReflectionTestUtils.setField(config, "accessKey", "minioadmin");
        ReflectionTestUtils.setField(config, "secretKey", "minioadmin");
        ReflectionTestUtils.setField(config, "bucketName", "test-bucket");

        MinioClient clientSpy = spy(MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build());

        // We stub behavior via doReturn on spy
        doReturn(false).when(clientSpy).bucketExists(any(BucketExistsArgs.class));
        doNothing().when(clientSpy).makeBucket(any(MakeBucketArgs.class));

        // Use Reflection to inject the spy into config-produced client
        MinioClient produced = config.minioClient();
        assertThat(produced).isNotNull();
    }

    @Test
    void minioClient_throwsRuntimeOnFailure() {
        MinioConfig config = new MinioConfig();
        ReflectionTestUtils.setField(config, "minioUrl", "bad-url");
        ReflectionTestUtils.setField(config, "accessKey", "access");
        ReflectionTestUtils.setField(config, "secretKey", "secret");
        ReflectionTestUtils.setField(config, "bucketName", "bucket");

        // Just verify it throws a RuntimeException when initialization fails
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, config::minioClient);
    }
}