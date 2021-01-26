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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.getTopicProperty;

class DefaultEventProcessorFactory implements KafkaEventProcessorFactory {

    private Map<ReaderKey, ReaderValue> topicReaderMap = new HashMap<>();
    private Map<String, KafkaEventWriter> topicWriterMap = new ConcurrentHashMap<>();


    public KafkaEventReader getEventReader(String topic, ClassLoader cl) {
        String className = getTopicProperty(topic, "eventReaderClass", CloudEventReader.class.getName());
        ReaderValue value;
        synchronized (topicReaderMap) {
            value = topicReaderMap.computeIfAbsent(new ReaderKey(cl, className), k -> new ReaderValue(
                    newReaderInstance(className, cl)));
            value.addReference(topic);
        }
        return value.getInstance();
    }

    public KafkaEventWriter getEventWriter(String topic) {
        return topicWriterMap.computeIfAbsent(getTopicProperty(topic, "eventWriterClass", CloudEventWriter.class
                .getName()), this::newWriterInstance);
    }

    private KafkaEventReader newReaderInstance(String className, ClassLoader cl) {
        try {
            return Class.forName(className).asSubclass(KafkaEventReader.class).getConstructor(ClassLoader.class)
                    .newInstance(cl);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Cannot instantiate KafkaEventReader class " + className +
                                               ". Please review system property configuration and make sure class has a public constructor that accepts a ClassLoaderInstance",
                    ex);
        }
    }

    private KafkaEventWriter newWriterInstance(String className) {
        try {
            return Class.forName(className).asSubclass(KafkaEventWriter.class).getConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Cannot instantiate KafkaEventWriter class " + className +
                                               ". Please review system property configuration and make sure class has a default public constructor ",
                    ex);
        }
    }

    @Override
    public void readerUndeployed(String topic, ClassLoader cl) {
        String className = getTopicProperty(topic, "eventReaderClass", CloudEventReader.class.getName());
        ReaderKey key = new ReaderKey(cl, className);
        synchronized (topicReaderMap) {
            ReaderValue value = topicReaderMap.get(key);
            if (value != null && value.removeReference(topic)) {
                topicReaderMap.remove(key);
            }
        }
    }

    @Override
    public void close() {
        topicReaderMap.clear();
        topicWriterMap.clear();
    }

    private static class ReaderKey {

        private ClassLoader cl;
        private String className;

        public ReaderKey(ClassLoader cl, String className) {
            this.cl = cl;
            this.className = className;
        }
        @Override
        public int hashCode() {
            return className.hashCode() + cl.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = this == obj;
            if (!result && obj instanceof ReaderKey) {
                ReaderKey other = (ReaderKey) obj;
                result = cl.equals(other.cl) && className.equals(other.className);
            }
            return result;
        }
    }

    private static class ReaderValue {
        private KafkaEventReader instance;
        private Collection<String> topics;

        public ReaderValue(KafkaEventReader instance) {
            this.instance = instance;
            this.topics = new HashSet<>();
        }

        public KafkaEventReader getInstance() {
            return instance;
        }

        public void addReference(String topic) {
            topics.add(topic);
        }

        public boolean removeReference(String topic) {
            topics.remove(topic);
            return topics.isEmpty();
        }
    }
}
