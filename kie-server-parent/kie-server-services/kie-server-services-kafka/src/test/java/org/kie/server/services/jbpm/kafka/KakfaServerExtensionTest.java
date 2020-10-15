package org.kie.server.services.jbpm.kafka;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.core.Signal;
import org.jbpm.kie.services.impl.model.DefaultSignalDesc;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerImpl;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class KakfaServerExtensionTest {

    private static class MockKafkaServerExtension extends KafkaServerExtension {

        private MockConsumer<String, byte[]> consumer = new MockConsumer<String, byte[]>(OffsetResetStrategy.EARLIEST);

        protected Consumer<String, byte[]> getKafkaConsumer() {
            return consumer;
        }

        public MockConsumer<String, byte[]> getMockConsumer() {
            return consumer;
        }
    }

    private ProcessService processService;
    private MockKafkaServerExtension extension;
    private KieServerImpl server;
    private KieServerRegistry registry;
    private DeployedUnit deployedUnit;
    private ProcessDefinition processDefinition;

    @Before
    public void setup() {
        extension = new MockKafkaServerExtension();
        server = mock(KieServerImpl.class);
        registry = mock(KieServerRegistry.class);
        KieServerExtension serverExtension = mock(KieServerExtension.class);

        when(registry.getServerExtension(Mockito.anyString())).thenReturn(serverExtension);
        ListenerSupport deployService = mock(ListenerSupport.class, withSettings().extraInterfaces(
                DeploymentService.class));
        processService = mock(ProcessService.class);
        when(serverExtension.getServices()).thenReturn(Arrays.asList(deployService, processService));
        extension.init(server, registry);
        extension.serverStarted();
        deployedUnit = mock(DeployedUnit.class);
        processDefinition = mock(ProcessDefinition.class);
        when(deployedUnit.getDeployedAssets()).thenReturn(Collections.singletonList(processDefinition));
    }

    @After
    public void close() {
        extension.destroy(server, registry);
    }

    @Test
    public void testKafkaServerExecutorSignal() throws InterruptedException {
        when(processDefinition.getSignalsMetadata()).thenReturn(Collections.singletonList(DefaultSignalDesc.from(
                new Signal("MySignal", "MySignal", "String"))));
        when(deployedUnit.getDeployedAssets()).thenReturn(Collections.singletonList(processDefinition));
        extension.onDeploy(new DeploymentEvent("MyDeploy", deployedUnit));
        publishEvent("MySignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy", "MySignal", "javierito");
    }

    @Test
    public void testKafkaServerExecutorMessage() throws InterruptedException {

        Message msg = new Message("MyMessage");
        msg.setName("Hello");
        msg.setType("String");
        when(processDefinition.getSignalsMetadata()).thenReturn(Collections.singletonList(DefaultSignalDesc.from(msg)));
        extension.onDeploy(new DeploymentEvent("MyDeploy", deployedUnit));
        publishEvent("Hello", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"pepe\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy", "Message-Hello", "pepe");
    }

    @Test
    public void testKafkaServerExecutorMessageTopic() throws InterruptedException {

        final String topicProperty = KafkaServerExtension.TOPIC_PREFIX + "Hello";
        System.setProperty(topicProperty, "MyTopic");
        try {
            Message msg = new Message("MyMessage");
            msg.setName("Hello");
            msg.setType("String");
            when(processDefinition.getSignalsMetadata()).thenReturn(Collections.singletonList(DefaultSignalDesc.from(
                    msg)));
            extension.onDeploy(new DeploymentEvent("MyDeploy", deployedUnit));
            publishEvent("MyTopic", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"pepe\"}");
            verify(processService, getTimeout()).signalEvent("MyDeploy", "Message-Hello", "pepe");
        } finally {
            System.clearProperty(topicProperty);
        }
    }

    private VerificationMode getTimeout() {
        return timeout(Long.getLong(KafkaServerExtension.KAFKA_EXTENSION_PREFIX + "poll.interval", 1L) * 2000).times(1);
    }

    private void publishEvent(String topic, String cloudEventText) {
        Set<String> topics = extension.getMockConsumer().subscription();
        assertTrue("Topic " + topic + " not found", topics.contains(topic));
        List<TopicPartition> partitions = Collections.singletonList(new TopicPartition(topic, 0));
        extension.getMockConsumer().rebalance(partitions);
        Map<TopicPartition, Long> partitionsBeginningMap = new HashMap<TopicPartition, Long>();
        Map<TopicPartition, Long> partitionsEndMap = new HashMap<TopicPartition, Long>();
        long records = 10L;
        for (TopicPartition partition : partitions) {
            partitionsBeginningMap.put(partition, 0l);
            partitionsEndMap.put(partition, records);
        }
        extension.getMockConsumer().updateBeginningOffsets(partitionsBeginningMap);
        extension.getMockConsumer().updateEndOffsets(partitionsEndMap);
        extension.getMockConsumer().addRecord(new ConsumerRecord<String, byte[]>(topic, 0, 0L, "",
                cloudEventText.getBytes()));
    }

}
