package org.kie.server.services.jbpm.kafka;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.KAFKA_EXTENSION_PREFIX;

public class KafkaServerExtensionTest {

    @Test
    public void testProperties() {

        KafkaServerExtension ext = new KafkaServerExtension();
        System.setProperty(KAFKA_EXTENSION_PREFIX + ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        System.setProperty(KAFKA_EXTENSION_PREFIX + ProducerConfig.BATCH_SIZE_CONFIG, "1000");
        System.setProperty(KAFKA_EXTENSION_PREFIX + "randomProperty", "JOJOJO");
        try {
            Map<String, Object> producerProperties = ext.getProducerProperties();
            Map<String, Object> consumerProperties = ext.getConsumerProperties();
            ext.initProperties();
            assertEquals(1, producerProperties.size());
            assertEquals("1000",producerProperties.get(ProducerConfig.BATCH_SIZE_CONFIG));
            assertEquals(1, consumerProperties.size());
            assertEquals("false", consumerProperties.get(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG));
        } finally {
            System.clearProperty(KAFKA_EXTENSION_PREFIX + ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG);
            System.clearProperty(KAFKA_EXTENSION_PREFIX + ProducerConfig.BATCH_SIZE_CONFIG);
            System.clearProperty(KAFKA_EXTENSION_PREFIX + "randomProperty");

        }
    }
}
