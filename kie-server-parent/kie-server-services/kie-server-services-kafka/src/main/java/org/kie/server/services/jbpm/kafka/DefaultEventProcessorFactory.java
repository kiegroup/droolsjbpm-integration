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

    private Map<ClassLoader, Map<String, KafkaEventReader>> topicReaderMap = new HashMap<>();
    private Map<KafkaEventReader, Collection<String>> readerInstanceMap = new HashMap<>();

    private Map<String, KafkaEventWriter> topicWriterMap = new ConcurrentHashMap<>();
    private Map<String, KafkaEventWriter> writerInstanceMap = new ConcurrentHashMap<>();

    public KafkaEventReader getEventReader(String topic, ClassLoader cl) {
        String className = getTopicProperty(topic, "eventReaderClass", CloudEventReader.class.getName());
        KafkaEventReader instance;
        synchronized (topicReaderMap) {
            instance = topicReaderMap.computeIfAbsent(cl, t -> new ConcurrentHashMap<>()).computeIfAbsent(
                    className, c -> newReaderInstance(className, cl));
            readerInstanceMap.computeIfAbsent(instance, i -> new HashSet<>()).add(topic);
        }
        return instance;
    }

    public KafkaEventWriter getEventWriter(String topic) {
        return topicWriterMap.computeIfAbsent(topic, this::buildWriter);
    }

    private KafkaEventWriter buildWriter(String topic) {
        return writerInstanceMap.computeIfAbsent(getTopicProperty(topic, "eventWriterClass", CloudEventWriter.class
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
        synchronized (topicReaderMap) {
            Map<String, KafkaEventReader> map = topicReaderMap.get(cl);
            if (map != null) {
                KafkaEventReader instanceRemoved = map.get(className);
                if (instanceRemoved != null) {
                    Collection<String> topics = readerInstanceMap.get(instanceRemoved);
                    if (topics.remove(topic) && topics.isEmpty()) {
                        readerInstanceMap.remove(instanceRemoved);
                        map.remove(className);
                        if (map.isEmpty()) {
                            topicReaderMap.remove(cl);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        synchronized (topicReaderMap) {
            topicReaderMap.clear();
            readerInstanceMap.clear();
        }
        topicWriterMap.clear();
        writerInstanceMap.clear();
    }
}
