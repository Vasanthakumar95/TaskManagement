package com.learning.notificationservice.config;

import com.learning.notificationservice.event.TaskEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerConfigTest {

    @Test
    void consumerFactory_configuresProperties() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "test-group");

        ConsumerFactory<String, TaskEvent> factory = config.consumerFactory();
        assertThat(factory).isNotNull();

        // Use reflection to check props on internal factory map
        Map<String, Object> props = (Map<String, Object>)
                ReflectionTestUtils.getField(factory, "configs");
        assertThat(props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(props.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("test-group");
    }

    @Test
    void kafkaListenerContainerFactory_usesConsumerFactory() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "test-group");

        ConcurrentKafkaListenerContainerFactory<String, TaskEvent> factory =
                config.kafkaListenerContainerFactory();

        assertThat(factory.getConsumerFactory()).isNotNull();
    }
}