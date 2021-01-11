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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.getTopicProperty;

class DefaultEventProcessorFactory implements KafkaEventProcessorFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventProcessorFactory.class);

    private Map<String, Map<ClassLoader, KafkaEventReader>> topicReaderMap = new ConcurrentHashMap<>();
    private Map<ClassLoader, KafkaEventReader> readerInstanceMap = new ConcurrentHashMap<>();

    private Map<String, KafkaEventWriter> topicWriterMap = new ConcurrentHashMap<>();
    private Map<String, KafkaEventWriter> writerInstanceMap = new ConcurrentHashMap<>();

    private AtomicBoolean defaultWriterInitialized = new AtomicBoolean(false);
    private KafkaEventWriter defaultWriterInstance;

    public KafkaEventReader getEventReader(String topic, ClassLoader cl) {
        return topicReaderMap.computeIfAbsent(topic, t -> new ConcurrentHashMap<>()).computeIfAbsent(cl,
                c -> buildReader(topic, cl));
    }

    public KafkaEventWriter getEventWriter(String topic) {
        return topicWriterMap.computeIfAbsent(topic, this::buildWriter);
    }

    private KafkaEventReader buildReader(String topic, ClassLoader cl) {
        String className = getTopicProperty(topic, "eventReaderClass");
        KafkaEventReader instance = null;
        if (className != null) {
            instance = readerInstanceMap.computeIfAbsent(cl, c -> newReaderInstance(className, cl));
        }
        return instance == null ? buildDefaultReader(cl) : instance;
    }

    private KafkaEventWriter buildWriter(String topic) {
        String className = getTopicProperty(topic, "eventWriterClass");
        KafkaEventWriter instance = null;
        if (className != null) {
            instance = writerInstanceMap.computeIfAbsent(className, this::newWriterInstance);
        }
        if (defaultWriterInitialized.compareAndSet(false, true)) {
            defaultWriterInstance = buildDefaultWriter();
        }
        return instance == null ? defaultWriterInstance : instance;
    }

    private KafkaEventReader newReaderInstance(String className, ClassLoader cl) {
        try {
            return Class.forName(className).asSubclass(KafkaEventReader.class).getConstructor(ClassLoader.class)
                    .newInstance(cl);
        } catch (ReflectiveOperationException | ClassCastException ex) {
            logger.error("Error instantiating class {}", className, ex);
            return null;
        }
    }

    private KafkaEventWriter newWriterInstance(String className) {
        try {
            return Class.forName(className).asSubclass(KafkaEventWriter.class).getConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException ex) {
            logger.error("Error instantiating class {}", className, ex);
            return null;
        }
    }

    protected KafkaEventReader buildDefaultReader(ClassLoader cl) {
        return new CloudEventReader(cl);
    }

    protected KafkaEventWriter buildDefaultWriter() {
        return new CloudEventWriter();
    }
}
