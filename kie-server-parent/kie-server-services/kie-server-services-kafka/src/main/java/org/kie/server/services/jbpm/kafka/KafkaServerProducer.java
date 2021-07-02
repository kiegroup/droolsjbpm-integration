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

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jbpm.services.api.DeploymentEvent;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.MessageEvent;
import org.kie.api.event.process.SignalEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.InternalRegisterableItemsFactory;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.processMessages;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.processSignals;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.topicFromSignal;

class KafkaServerProducer extends DefaultProcessEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerProducer.class);

    // Kafka producer
    private Producer<String, byte[]> producer;
    private Supplier<Producer<String, byte[]>> producerSupplier;
    private KafkaEventProcessorFactory factory;

    private Lock producerLock = new ReentrantLock();

    public KafkaServerProducer(KafkaEventProcessorFactory factory,
                               Supplier<Producer<String, byte[]>> producerSupplier) {
        this.factory = factory;
        this.producerSupplier = producerSupplier;
    }

    void close(Duration duration) {
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

    @Override
    public void onMessage(MessageEvent event) {
        if (processMessages()) {
            sendEvent(event.getProcessInstance(), event.getMessageName(), event.getMessage());
        }
    }

    @Override
    public void onSignal(SignalEvent event) {
        if (processSignals(event)) {
            sendEvent(event.getProcessInstance(), event.getSignalName(), event.getSignal());
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
        }
         finally {
             producerLock.unlock();
        }
        try {
            String topic = topicFromSignal(name);
            producer.send(new ProducerRecord<>(topic, factory.getEventWriter(topic)
                    .writeEvent(processInstance, value)),
                    (m, e) -> {
                        if (e != null) {
                            logError(value, e);
                        }
                    });
        } catch (Exception e) {
            logError(value, e);
        }
    }

    private void logError(Object value, Exception e) {
        logger.error("Error publishing event {}", value, e);
    }

    public void activate(DeploymentEvent event) {
        ((InternalRegisterableItemsFactory) ((InternalRuntimeManager) event.getDeployedUnit().getRuntimeManager())
                .getEnvironment().getRegisterableItemsFactory()).addProcessListener(this);
    }

    public void deactivate(DeploymentEvent event) {
        // when deployment is deactivated, the listener is gone too
    }
}
