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

/**
 * To be implemented by those interested on providing  
 * a custom serialization mechanism for Kafka and do not want
 * to rely on caching and reflection mechanism provided by the 
 * default implementation. 
 */
public interface KafkaEventProcessorFactory {

    /**
     * Returns a KafkaEventReader to be used for the provided topic and classloader 
     * @param topic name of the topic
     * @param cl must be used to load classes
     * @return KafkaEventReader instance, which might be a newly created instance or
     * an already existing one 
     */
    KafkaEventReader getEventReader(String topic, ClassLoader cl);

    /**
     * Returns a KafkaEventWriter to be used for the provided topic  
     * @param topic name of the topic
     * @return KafkaEventReader instance, typically the same instance per topic 
     */
    KafkaEventWriter getEventWriter(String topic);

    /**
     * Cleanup resources held by factory, if any (for example, a EventReader instance cache)
     */
    default void close() {}

    /** To be implemented only by those factories using a reader cache */
    default void readerUndeployed(String topic, ClassLoader cl) {}
}
