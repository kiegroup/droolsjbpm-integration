/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.services.jbpm.kafka;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.core.Signal;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.model.MessageDescImpl;
import org.jbpm.kie.services.impl.model.SignalDescImpl;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.SignalDesc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.runtime.manager.InternalRegisterableItemsFactory;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.kafka.KafkaServerUtils.Mapping;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.KAFKA_EXTENSION_PREFIX;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.MESSAGE_MAPPING_PROPERTY;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.SIGNAL_MAPPING_PROPERTY;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.TOPIC_PREFIX;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;



public class KafkaServerExtensionConsumerTest {

    private static class MockKafkaServerExtension extends KafkaServerExtension {

        private MockConsumer<String, byte[]> consumer;

        public MockKafkaServerExtension(MockConsumer<String, byte[]> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected Consumer<String, byte[]> getKafkaConsumer() {
            return consumer;
        }

        public void setKafkaConsumer(MockConsumer<String, byte[]> consumer) {
            this.consumer = consumer;
        }
    }

    private final static long TIMEOUT = 1L;

    private ProcessService processService;
    private MockKafkaServerExtension extension;
    private KieServerImpl server;
    private KieServerRegistry registry;
    private DeployedUnit deployedUnit;
    private ProcessDefinition processDefinition;
    private MockConsumer<String, byte[]> mockConsumer;
    private static Logger logger = LoggerFactory.getLogger(KafkaServerExtensionConsumerTest.class);
    private InternalRegisterableItemsFactory itemsFactory;


    @Before
    public void setup() {
        System.setProperty(KAFKA_EXTENSION_PREFIX + "poll.interval", Long.toString(TIMEOUT));
        System.setProperty(SIGNAL_MAPPING_PROPERTY, Mapping.AUTO.toString());
        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        extension = new MockKafkaServerExtension(mockConsumer);
        server = mock(KieServerImpl.class);
        registry = mock(KieServerRegistry.class);
        itemsFactory = new SimpleRegisterableItemsFactory();
        KieServerExtension serverExtension = mock(KieServerExtension.class);
        when(registry.getServerExtension(Mockito.anyString())).thenReturn(serverExtension);
        ListenerSupport deployService = mock(ListenerSupport.class, withSettings().extraInterfaces(
                DeploymentService.class));
        processService = mock(ProcessService.class);
        when(serverExtension.getServices()).thenReturn(Arrays.asList(deployService, processService));
        deployedUnit = mock(DeployedUnit.class);
        InternalRuntimeManager runtimeManager = mock(InternalRuntimeManager.class);
        when(deployedUnit.getRuntimeManager()).thenReturn(runtimeManager);
        RuntimeEnvironment runtimeEngine = mock(RuntimeEnvironment.class);
        when(runtimeManager.getEnvironment()).thenReturn(runtimeEngine);
        when(runtimeEngine.getRegisterableItemsFactory()).thenReturn(itemsFactory);
        processDefinition = mock(ProcessDefinition.class);
        when(deployedUnit.getDeployedAssets()).thenReturn(Collections.singletonList(processDefinition));
        extension.init(server, registry);
        KModuleDeploymentUnit deploymentUnit = mock(KModuleDeploymentUnit.class);
        when(deployedUnit.getDeploymentUnit()).thenReturn(deploymentUnit);
        KieContainer kieContainer = mock(KieContainer.class);
        when(deploymentUnit.getKieContainer()).thenReturn(kieContainer);
        when(kieContainer.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
        extension.serverStarted();
    }

    @After
    public void close() {
        extension.destroy(server, registry);
        System.clearProperty(SIGNAL_MAPPING_PROPERTY);
        System.clearProperty(MESSAGE_MAPPING_PROPERTY);
    }

    private SignalDesc createSignal(String id, String type) {
        Signal signal = new Signal(id, id, type);
        signal.addIncomingNode(mock(Node.class));
        return SignalDescImpl.from(signal);
    }

    @Test
    public void testKafkaServerExecutorSignal() {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal",
                "String")));
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        publishEvent("MySignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy1", "MySignal", "javierito");
    }



    private void testStructRefEvent(String clazzName) {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal",
                clazzName)));
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        publishEvent("MySignal",
                "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":{\"name\":\"javierito\"}}");
        verify(processService, getTimeout()).signalEvent("MyDeploy1", "MySignal", new Person("javierito"));
    }

    @Test
    public void testKafkaServerExecutorSignalWithClassType() {
        testStructRefEvent(Person.class.getTypeName());
    }

    @Test
    public void testKafkaServerExecutorSignalWithClassName() {
        testStructRefEvent(Person.class.getName());
    }

    @Test
    public void testKafkaSubscriptionChange() {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal",
                "String")));
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        publishEvent("MySignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy1", "MySignal", "javierito");
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("ChangedSignal",
                "String")));
        extension.onActivate(new DeploymentEvent("MyDeploy1", deployedUnit));
        publishEvent("ChangedSignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy1", "ChangedSignal", "javierito");
    }
    
    @Test
    public void testMultiDeployment () throws InterruptedException, ExecutionException {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal",
                "String")));
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<?> future1 = executorService.submit(() -> extension.onDeploy(new DeploymentEvent("MyDeploy1",
                deployedUnit)));
        Future<?> future2 = executorService.submit(() -> extension.onDeploy(new DeploymentEvent("MyDeploy2",
                deployedUnit)));
        future1.get();
        future2.get();
        publishEvent("MySignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        ArgumentCaptor<String> deploymentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> signalCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(processService, getTimeout(2, 2)).signalEvent(deploymentCaptor.capture(), signalCaptor.capture(),
                dataCaptor.capture());
        assertEquals("MySignal", signalCaptor.getValue());
        assertEquals("javierito", dataCaptor.getValue());
        assertThat(deploymentCaptor.getValue(), anyOf(is("MyDeploy1"),is("MyDeploy2")));
    }

    @Test
    public void testKafkaSubscriptionEmpty() {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal",
                "String")));
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        publishEvent("MySignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy1", "MySignal", "javierito");
        extension.onUnDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.emptyList());
        extension.onActivate(new DeploymentEvent("MyDeploy1", deployedUnit));
        assertTrue(mockConsumer.assignment().isEmpty());
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("NewSignal",
                "String")));
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        publishEvent("NewSignal", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy1", "NewSignal", "javierito");
    }

    @Test
    public void testKafkaServerExecutorMessage() {
        Message msg = new Message("MyMessage");
        msg.setName("Hello");
        msg.setType("String");
        msg.addIncomingNode(mock(Node.class));
        when(processDefinition.getMessagesDesc()).thenReturn(Collections.singletonList(MessageDescImpl.from(msg)));
        extension.onDeploy(new DeploymentEvent("MyDeploy2", deployedUnit));
        publishEvent("Hello", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"pepe\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy2", "Message-Hello", "pepe");
    }

    @Test
    public void testKafkaServerExecutorMessageTopic() {
        final String topicProperty = TOPIC_PREFIX + "Hello";
        System.setProperty(topicProperty, "MyTopic");
        try {
            Message msg = new Message("MyMessage");
            msg.setName("Hello");
            msg.setType("String");
            msg.addIncomingNode(mock(Node.class));
            when(processDefinition.getMessagesDesc()).thenReturn(Collections.singletonList(MessageDescImpl.from(
                    msg)));
            extension.onDeploy(new DeploymentEvent("MyDeploy3", deployedUnit));
            publishEvent("MyTopic", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"pepe\"}");
            verify(processService, getTimeout()).signalEvent("MyDeploy3", "Message-Hello", "pepe");
        } finally {
            System.clearProperty(topicProperty);
        }
    }

    @Test
    public void testNoSignals() {
        extension = spy(extension);
        when(processDefinition.getMessagesDesc()).thenReturn(Collections.emptyList());
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.emptyList());
        extension.onDeploy(new DeploymentEvent("EmptyDeploy", deployedUnit));
        verify(extension, never()).getKafkaConsumer();
    }

    @Test
    public void testWithDestroy() {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal2",
                "String")));
        Message msg = new Message("MyMessage");
        msg.setName("Hello2");
        msg.setType("String");
        msg.addIncomingNode(mock(Node.class));
        extension.onDeploy(new DeploymentEvent("MyDeploy4", deployedUnit));
        publishEvent("MySignal2", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy4", "MySignal2", "javierito");
        extension.destroy(server, registry);
        mockConsumer = new MockConsumer<String, byte[]>(OffsetResetStrategy.EARLIEST);
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.emptyList());
        when(processDefinition.getMessagesDesc()).thenReturn(Collections.singletonList(MessageDescImpl.from(
                msg)));
        extension.setKafkaConsumer(mockConsumer);
        extension.init(server, registry);
        extension.onDeploy(new DeploymentEvent("MyDeploy5", deployedUnit));
        publishEvent("Hello2", "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"pepe\"}");
        verify(processService, getTimeout()).signalEvent("MyDeploy5", "Message-Hello2", "pepe");
    }

    @Test
    public void testSignalDisable() {
        when(processDefinition.getSignalsDesc()).thenReturn(Collections.singletonList(createSignal("MySignal2",
                "String")));
        System.clearProperty(SIGNAL_MAPPING_PROPERTY);
        extension.onDeploy(new DeploymentEvent("MyDeploy4", deployedUnit));
        verify(processDefinition, never()).getSignalsDesc();
    }

    @Test
    public void testMessageDisable() {
        when(processDefinition.getMessagesDesc()).thenReturn(Collections.singletonList(MessageDescImpl.from(new Message(
                "MyMessage"))));
        System.setProperty(MESSAGE_MAPPING_PROPERTY, Mapping.NONE.toString());
        extension.onDeploy(new DeploymentEvent("MyDeploy4", deployedUnit));
        verify(processDefinition, never()).getMessagesDesc();
    }


    private VerificationMode getTimeout() {
        return getTimeout(1);
    }

    private VerificationMode getTimeout(int times) {
        return getTimeout(times, TIMEOUT);
    }

    private VerificationMode getTimeout(int times, long timeout) {
        return timeout(timeout * 1000).times(times);
    }

    private void publishEvent(String topic, String cloudEventText) {
        Set<String> topics = mockConsumer.subscription();
        assertTrue("Topic " + topic + " not found", topics.contains(topic));
        List<TopicPartition> partitions = Collections.singletonList(new TopicPartition(topic, 0));
        Map<TopicPartition, Long> partitionsBeginningMap = new HashMap<>();
        Map<TopicPartition, Long> partitionsEndMap = new HashMap<>();
        for (TopicPartition partition : partitions) {
            partitionsBeginningMap.put(partition, 0L);
            partitionsEndMap.put(partition, 10L);
        }
        mockConsumer.rebalance(partitions);
        mockConsumer.updateBeginningOffsets(partitionsBeginningMap);
        mockConsumer.updateEndOffsets(partitionsEndMap);

        logger.debug("Publishing event {} to topic {}", cloudEventText, topic);
        mockConsumer.addRecord(new ConsumerRecord<>(topic, 0, 0L, "",
                cloudEventText.getBytes()));
    }
}
