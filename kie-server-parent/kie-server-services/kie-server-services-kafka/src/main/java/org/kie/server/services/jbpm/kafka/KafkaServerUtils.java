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

import org.jbpm.services.api.model.SignalDescBase;
import org.kie.api.event.process.SignalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KafkaServerUtils {

    private KafkaServerUtils() {}

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerUtils.class);

    static final String KAFKA_EXTENSION_PREFIX = "org.kie.server.jbpm-kafka.ext.";

    enum Mapping {
        AUTO, NONE
    }

    static final String SIGNAL_MAPPING_PROPERTY = KAFKA_EXTENSION_PREFIX + "signals.mapping";
    static final String MESSAGE_MAPPING_PROPERTY = KAFKA_EXTENSION_PREFIX + "message.mapping";
    static final String FACTORY_PROCESSOR_CLASS_NAME = KAFKA_EXTENSION_PREFIX + "eventProcessorFactoryClass";
    static final String TOPIC_PREFIX = KAFKA_EXTENSION_PREFIX + "topics.";
    private static final Mapping SIGNAL_MAPPING_DEFAULT = Mapping.NONE;
    private static final Mapping MESSAGE_MAPPING_DEFAULT = Mapping.AUTO;

    static boolean processMessages() {
        return getMapping(MESSAGE_MAPPING_PROPERTY, MESSAGE_MAPPING_DEFAULT) == Mapping.AUTO;
    }

    static boolean processSignals() {
        return getMapping(SIGNAL_MAPPING_PROPERTY, SIGNAL_MAPPING_DEFAULT) == Mapping.AUTO;
    }

    static boolean processSignals(SignalEvent event) {
        Mapping mapping = getMapping(SIGNAL_MAPPING_PROPERTY, SIGNAL_MAPPING_DEFAULT);
        return mapping == Mapping.AUTO || "##kafka".equalsIgnoreCase((String) event.getNodeInstance().getNode()
                .getMetaData().get("implementation"));
    }

    static <T extends SignalDescBase> String topicFromSignal(T signal) {
        return topicFromSignal(signal.getName());
    }

    static String topicFromSignal(String name) {
        return System.getProperty(TOPIC_PREFIX + name, name);
    }

    static KafkaEventProcessorFactory buildEventProcessorFactory() {
        final String className = System.getProperty(FACTORY_PROCESSOR_CLASS_NAME);
        KafkaEventProcessorFactory instance = null;
        if (className != null) {
            try {
                instance = Class.forName(className).asSubclass(KafkaEventProcessorFactory.class).getConstructor()
                        .newInstance();
            } catch (ReflectiveOperationException ex) {
                logger.warn("Error loading KafkaEventProcessorFactory for class name {}. Check value of property {}",
                        className, FACTORY_PROCESSOR_CLASS_NAME, ex);
            }
        }
        if (instance == null) {
            instance = new DefaultEventProcessorFactory();
        }
        return instance;
    }

    static String getTopicProperty(String topic, String propName, String defaultValue) {
        return System.getProperty(TOPIC_PREFIX + topic + "." + propName, System.getProperty(KAFKA_EXTENSION_PREFIX +
                                                                                            propName, defaultValue));
    }

    private static Mapping getMapping(String propName, Mapping defaultValue) {
        Mapping result = null;
        String propValue = System.getProperty(propName);
        if (propValue != null) {
            try {
                result = Mapping.valueOf(propValue.toUpperCase());
            } catch (IllegalArgumentException ex) {
                logger.warn("Wrong value {} for property {}, using default {}", propValue, propName, defaultValue);
            }
        }
        return result == null ? defaultValue : result;
    }
}
