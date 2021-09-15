/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.kie.server.services.jbpm.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.kie.services.impl.DeployedUnitImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.model.MessageDescImpl;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.model.MessageDesc;
import org.jbpm.workflow.core.node.StartNode;
import org.junit.Test;
import org.kie.api.definition.process.Node;

public class KafkaServerRegistrationTest {

    @Test
    public void testVersionedDeployment() {
        KafkaServerRegistration register = new KafkaServerRegistration();
        DeployedUnitImpl du1 = new DeployedUnitImpl(new KModuleDeploymentUnit("individuo", "pepe", "1.0"));
        DeployedUnitImpl du2 = new DeployedUnitImpl(new KModuleDeploymentUnit("individuo", "pepe", "2.0"));

        Message message = new Message("pepe");
        message.setName("pepe");
        Node node = mock(Node.class);
        message.addIncomingNode(node);
        MessageDesc signal = MessageDescImpl.from(message);
        ProcessAssetDesc processDesc = new ProcessAssetDesc();
        processDesc.setMessagesDesc(Collections.singletonList(signal));
        du1.addAssetLocation("pepe", processDesc);
        du2.addAssetLocation("pepe", processDesc);
        DeploymentEvent event1 = new DeploymentEvent(du1.getDeploymentUnit().getIdentifier(), du1);
        DeploymentEvent event2 = new DeploymentEvent(du2.getDeploymentUnit().getIdentifier(), du2);
        register.addRegistration(event1);
        register.addRegistration(event2);
        ConsumerRecord<String, byte[]> record = mock(ConsumerRecord.class);
        when(record.topic()).thenReturn("pepe");
        KafkaServerEventProcessor<MessageDesc> processor = mock(KafkaServerEventProcessor.class);
        register.forEachMessage(record, processor);
        verify(processor).accept(record, event1.getDeploymentId(), signal);
        verify(processor).accept(record, event2.getDeploymentId(), signal);
        reset(processor);
        message.addIncomingNode(mock(StartNode.class));
        register.forEachMessage(record, processor);
        verify(processor, times(0)).accept(record, event1.getDeploymentId(), signal);
        verify(processor).accept(record, event2.getDeploymentId(), signal);
    }

}
