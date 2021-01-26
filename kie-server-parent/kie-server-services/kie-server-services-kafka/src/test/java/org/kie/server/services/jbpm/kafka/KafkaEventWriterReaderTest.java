/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.FACTORY_PROCESSOR_CLASS_NAME;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.buildEventProcessorFactory;
import static org.mockito.Mockito.mock;

public class KafkaEventWriterReaderTest {

    @Test
    public void testEventWriter() {
        final String topic1 = "test1";
        final String topic2 = "test2";
        final String property1 = KafkaServerUtils.TOPIC_PREFIX + topic1 + "." + "eventWriterClass";
        final String property2 = KafkaServerUtils.TOPIC_PREFIX + topic2 + "." + "eventWriterClass";
        try {
            final KafkaEventProcessorFactory factory = buildEventProcessorFactory();
            System.setProperty(property1,
                    "org.kie.server.services.jbpm.kafka.RawJsonEventWriter");
            System.setProperty(KafkaServerUtils.TOPIC_PREFIX + topic2 + "." + "eventWriterClass",
                    "org.kie.server.services.jbpm.kafka.RawJsonEventWriter");
            KafkaEventWriter instance1 = factory.getEventWriter(topic1);
            KafkaEventWriter instance2 = factory.getEventWriter(topic2);
            assertEquals(RawJsonEventWriter.class, instance1.getClass());
            assertSame(instance1, instance2);
            assertEquals(CloudEventWriter.class, factory.getEventWriter("doesNotExist").getClass());
        } finally {
            System.clearProperty(property1);
            System.clearProperty(property2);
        }
    }

    @Test
    public void testEventReader() {
        final String topic1 = "test1";
        final String topic2 = "test2";
        final String topic3 = "test3";
        final String property1 = KafkaServerUtils.TOPIC_PREFIX + topic1 + "." + "eventReaderClass";
        final String property2 = KafkaServerUtils.TOPIC_PREFIX + topic2 + "." + "eventReaderClass";
        final String property3 = KafkaServerUtils.TOPIC_PREFIX + topic3 + "." + "eventReaderClass";
        final KafkaEventProcessorFactory factory = buildEventProcessorFactory();
        System.setProperty(property1,
                "org.kie.server.services.jbpm.kafka.RawJsonEventReader");
        System.setProperty(property2,
                "org.kie.server.services.jbpm.kafka.RawJsonEventReader");
        System.setProperty(property3,
                "org.kie.server.services.jbpm.kafka.CloudEventReader");
        ClassLoader newCl = new URLClassLoader(new URL[0]);
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            KafkaEventReader instance1 = factory.getEventReader(topic1, cl);
            assertSame(instance1, factory.getEventReader(topic1, cl));
            KafkaEventReader instance2 = factory.getEventReader(topic2, cl);
            assertSame(instance2, factory.getEventReader(topic2, cl));
            KafkaEventReader instance3 = factory.getEventReader(topic1, newCl);
            assertSame(instance3, factory.getEventReader(topic1, newCl));
            KafkaEventReader instance4 = factory.getEventReader(topic3, cl);
            assertSame(instance4, factory.getEventReader(topic3, cl));
            assertNotSame(instance1, instance4);
            assertSame(CloudEventReader.class, factory.getEventReader("notCustomized", cl).getClass());
            assertEquals(RawJsonEventReader.class, instance1.getClass());
            assertSame(instance1, instance2);
            assertNotEquals(instance1, instance3);
            factory.readerUndeployed(topic1, cl);
            instance1 = factory.getEventReader(topic1, cl);
            assertSame(instance1, instance2);
            factory.readerUndeployed(topic1, cl);
            factory.readerUndeployed(topic2, cl);
            instance1 = factory.getEventReader(topic1, cl);
            assertNotSame(instance1, instance2);
            instance2 = factory.getEventReader(topic2, cl);
            assertSame(instance1, instance2);
        } finally {
            System.clearProperty(property1);
            System.clearProperty(property2);
            System.clearProperty(property3);
        }
    }

    @Test
    public void testEventProcessorFactory() throws ClassNotFoundException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final String topic = "test";
        final String readProperty = KafkaServerUtils.TOPIC_PREFIX + topic + "." + "eventReaderClass";
        final String writeProperty = KafkaServerUtils.TOPIC_PREFIX + topic + "." + "eventWriterClass";
        System.setProperty(readProperty,
                "org.kie.server.services.jbpm.kafka.RawJsonEventReader");
        System.setProperty(writeProperty,
                "org.kie.server.services.jbpm.kafka.RawJsonEventWriter");
        Person person = new Person("Javierito");
        try {
            ProcessInstance processInstance = mock(ProcessInstance.class);
            final KafkaEventProcessorFactory factory = buildEventProcessorFactory();
            KafkaEventWriter writer = factory.getEventWriter(topic);
            KafkaEventReader reader = factory.getEventReader(topic, cl);
            assertEquals(person, reader.readEvent(writer.writeEvent(processInstance, person), Person.class));
        } finally {
            System.clearProperty(readProperty);
            System.clearProperty(writeProperty);
        }
    }

    @Test
    public void testDifferentEventProcessorFactory() throws ClassNotFoundException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final String topic = "doNotCare";
        Person person = new Person("Javierito");
        ProcessInstance processInstance = mock(ProcessInstance.class);
        System.setProperty(FACTORY_PROCESSOR_CLASS_NAME, JavaSerializationEventProcessorFactory.class.getName());
        try {
            KafkaEventProcessorFactory factory = buildEventProcessorFactory();
            KafkaEventWriter writer = factory.getEventWriter(topic);
            KafkaEventReader reader = factory.getEventReader(topic, cl);
            assertEquals(person, reader.readEvent(writer.writeEvent(processInstance, person), Person.class));
        } finally {
            System.clearProperty(FACTORY_PROCESSOR_CLASS_NAME);
        }
    }

}
