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
import java.util.Collections;

import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertEquals;
import static org.kie.server.services.jbpm.kafka.KafkaJsonUtils.mapper;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudEventTest {

    @Test
    public void testStringCloudEventDeserialization() throws IOException {
        String cloudEventText =
                "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\",\"specversion\":\"1.0\",\"time\":\"2020-03-21T17:43:34.000GMT\"}";
        CloudEventReader reader = new CloudEventReader(Thread.currentThread().getContextClassLoader());
        String event = reader.readEvent(cloudEventText.getBytes(), String.class);
        assertEquals("javierito", event);
    }

    @Test
    public void testPersonCloudEventDeserialization() throws IOException {
        String cloudEventText =
                "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":{\"name\":\"javierito\"},\"specversion\":\"1.0\",\"time\":\"2020-03-21T17:43:34.000GMT\"}";
        CloudEventReader reader = new CloudEventReader(Thread.currentThread().getContextClassLoader());
        Person event = reader.readEvent(cloudEventText.getBytes(), Person.class);
        assertEquals("javierito", event.getName());
    }

    @Test
    public void testPersonCloudEventSerialization() throws IOException {
        CloudEventWriter writer = new CloudEventWriter();
        ProcessInstance processInstance = mock(ProcessInstance.class);
        when(processInstance.getId()).thenReturn(1L);
        when(processInstance.getProcessId()).thenReturn("pepe");
        CloudEvent<?> event = mapper.readValue(writer.writeEvent(processInstance, new Person("Javierito")),
                CloudEvent.class);
        assertEquals("org.kie.server.services.jbpm.kafka.Person", event.getType());
        assertEquals("1.0", event.getSpecversion());
        assertEquals("/process/pepe/1", event.getSource());
        assertEquals(Collections.singletonMap("name", "Javierito"), event.getData());
    }
}
