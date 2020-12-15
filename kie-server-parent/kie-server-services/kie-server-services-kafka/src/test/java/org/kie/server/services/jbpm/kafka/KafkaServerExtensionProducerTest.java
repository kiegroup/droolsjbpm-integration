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

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.drools.core.event.MessageEventImpl;
import org.drools.core.event.SignalEventImpl;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.workflow.core.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.InternalRegisterableItemsFactory;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeEnvironment;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.kafka.KafkaServerExtension.Mapping;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class KafkaServerExtensionProducerTest {

    private static class MockKafkaServerExtension extends KafkaServerExtension {

        private MockProducer<String, byte[]> producer;

        public MockKafkaServerExtension(MockProducer<String, byte[]> producer) {
            this.producer = producer;
        }

        @Override
        protected Producer<String, byte[]> getKafkaProducer() {
            return producer;
        }
    }

    private ProcessService processService;
    private MockKafkaServerExtension extension;
    private KieServerImpl server;
    private KieServerRegistry registry;
    private DeployedUnit deployedUnit;
    private ProcessDefinition processDefinition;
    private MockProducer<String, byte[]> mockProducer;
    private ProcessInstance pInstance;
    private KieRuntime runtime;
    private NodeInstance nInstance;
    private Node node;
    private InternalRegisterableItemsFactory itemsFactory;
    private RuntimeEngine runtimeEngine = mock(RuntimeEngine.class);

    @Before
    public void setup() {
        System.setProperty(KafkaServerExtension.SIGNAL_MAPPING_PROPERTY, Mapping.AUTO.toString());
        itemsFactory = new SimpleRegisterableItemsFactory();
        mockProducer = new MockProducer<>();
        extension = new MockKafkaServerExtension(mockProducer);
        server = mock(KieServerImpl.class);
        registry = mock(KieServerRegistry.class);
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
        extension.serverStarted();
        pInstance = mock(ProcessInstance.class);
        runtime = mock(KieRuntime.class);
        nInstance = mock(NodeInstance.class);
        node = mock(Node.class);
        when(nInstance.getNode()).thenReturn(node);
        when(node.getMetaData()).thenReturn(Collections.emptyMap());
    }

    @After
    public void close() {
        extension.destroy(server, registry);
        System.clearProperty(KafkaServerExtension.SIGNAL_MAPPING_PROPERTY);
        System.clearProperty(KafkaServerExtension.MESSAGE_MAPPING_PROPERTY);
    }

    

    @Test 
    public void testMessageSent() throws IOException, ParseException {
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        itemsFactory.getProcessEventListeners(runtimeEngine).forEach(l -> l.onMessage(new MessageEventImpl(
                pInstance, runtime, nInstance,
                "MyMessage", "Javierito")));
        List<ProducerRecord<String, byte[]>> events = mockProducer.history();
        assertFalse(events.isEmpty());
        ProducerRecord<String, byte[]> event = events.get(0);
        assertEquals("MyMessage", event.topic());
        assertEquals("Javierito", CloudEvent.read(event.value(), String.class).getData());
    }

    @Test
    public void testSignalSent() throws IOException, ParseException {
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        itemsFactory.getProcessEventListeners(runtimeEngine).forEach(l -> l.onSignal(new SignalEventImpl(
                pInstance, runtime, nInstance, "MySignal", "Javierito")));
        List<ProducerRecord<String, byte[]>> events = mockProducer.history();
        assertFalse(events.isEmpty());
        ProducerRecord<String, byte[]> event = events.get(0);
        assertEquals("MySignal", event.topic());
        assertEquals("Javierito", CloudEvent.read(event.value(), String.class).getData());
    }

    @Test
    public void testSignalSentImplementation() throws IOException, ParseException {
        when(node.getMetaData()).thenReturn(Collections.singletonMap("implementation", "##kafka"));
        System.clearProperty(KafkaServerExtension.SIGNAL_MAPPING_PROPERTY);
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        itemsFactory.getProcessEventListeners(runtimeEngine).forEach(l -> l.onSignal(new SignalEventImpl(
                pInstance, runtime, nInstance, "MySignal", "Javierito")));
        List<ProducerRecord<String, byte[]>> events = mockProducer.history();
        assertFalse(events.isEmpty());
        ProducerRecord<String, byte[]> event = events.get(0);
        assertEquals("MySignal", event.topic());
        assertEquals("Javierito", CloudEvent.read(event.value(), String.class).getData());
    }

    @Test
    public void testSignalDisable() throws IOException, ParseException {
        System.clearProperty(KafkaServerExtension.SIGNAL_MAPPING_PROPERTY);
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        itemsFactory.getProcessEventListeners(runtimeEngine).forEach(l -> l.onSignal(new SignalEventImpl(
                pInstance, runtime, nInstance, "MySignal", "Javierito")));
        assertTrue(mockProducer.history().isEmpty());
    }

    @Test
    public void testMessageDisable() throws IOException, ParseException {
        System.setProperty(KafkaServerExtension.MESSAGE_MAPPING_PROPERTY, Mapping.NONE.toString());
        extension.onDeploy(new DeploymentEvent("MyDeploy1", deployedUnit));
        itemsFactory.getProcessEventListeners(runtimeEngine).forEach(l -> l.onMessage(new MessageEventImpl(
                pInstance, runtime, nInstance, "MyMessage", "Javierito")));
        assertTrue(mockProducer.history().isEmpty());
    }

    @Test
    public void testUndeploy() {
        DeploymentEvent event = new DeploymentEvent("MyDeploy1", deployedUnit);
        extension.onDeploy(event);
        extension.onUnDeploy(event);
        assertTrue(mockProducer.history().isEmpty());
    }

}
