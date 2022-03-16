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

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.KAFKA_EXTENSION_PREFIX;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.topicFromSignal;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class KafkaServerProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerProducer.class);
    private static KafkaServerProducer instance;

    public static void init(KafkaEventProcessorFactory factory,
                            Supplier<Producer<String, byte[]>> producerSupplier) {
        instance = new KafkaServerProducer(factory, producerSupplier);
    }

    public static void cleanup(Duration duration) {
        if (instance != null) {
            instance.close(duration);
            instance = null;
        }
    }

    public static void publish(ProcessInstance processInstance,
                               String name,
                               Object value) {
        if (instance != null) {
            instance.sendEvent(processInstance, name, value);
        }
    }

    // Kafka producer
    private Producer<String, byte[]> producer;
    private Supplier<Producer<String, byte[]>> producerSupplier;
    private KafkaEventProcessorFactory factory;
    private KafkaSender kafkaSender;

    private Lock producerLock = new ReentrantLock();

    private KafkaServerProducer(KafkaEventProcessorFactory factory,
                                Supplier<Producer<String, byte[]>> producerSupplier) {
        this.factory = factory;
        this.producerSupplier = producerSupplier;
        this.kafkaSender = Boolean.getBoolean(KAFKA_EXTENSION_PREFIX + "sync") ? this::sendSync : this::sendAsync;
    }

    private void close(Duration duration) {
        producerLock.lock();
        try {
            if (producer != null) {
                producer.close(duration);
                producer = null;
            }
        } finally {
            producerLock.unlock();
        }
    }

    private void sendEvent(ProcessInstance processInstance,
                           String name,
                           Object value) {
        producerLock.lock();
        try {
            if (producer == null) {
                producer = producerSupplier.get();
            }
        } finally {
            producerLock.unlock();
        }
        String topic = topicFromSignal(name);
        logger.debug("Publishing event {}  to topic {}", value, topic);
        kafkaSender.send(topic, value, processInstance);
    }

    private interface KafkaSender {

        void send(String topic, Object value, ProcessInstance processInstance);
    }

    private void sendAsync(String topic, Object value, ProcessInstance processInstance) {
        try {

            producer.send(new ProducerRecord<>(topic, marshall(topic, value, processInstance)),
                          (m, e) -> {
                              if (e != null) {
                                  logError(value, e);
                              }
                          });
        } catch (Exception e) {
            logError(value, e);
        }
    }

    private void sendSync(String topic, Object value, ProcessInstance processInstance) {
        try {
            producer.send(new ProducerRecord<>(topic, marshall(topic, value, processInstance))).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new KafkaException(e.getCause());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private byte[] marshall(String topic, Object value, ProcessInstance processInstance) throws IOException {
        return factory.getEventWriter(topic).writeEvent(processInstance, value);
    }

    private void logError(Object value, Exception e) {
        logger.error("Error publishing event {}", value, e);
    }
}
