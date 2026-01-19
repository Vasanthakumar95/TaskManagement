package com.learning.taskmanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConfigTest {

    @Test
    void taskEventsTopic_buildsTopic() {
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "taskEventsTopic", "task-events");

        NewTopic topic = config.taskEventsTopic();

        assertThat(topic.name()).isEqualTo("task-events");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
    }
}