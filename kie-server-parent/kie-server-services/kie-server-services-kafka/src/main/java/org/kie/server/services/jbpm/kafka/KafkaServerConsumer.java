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

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.model.MessageDesc;
import org.jbpm.services.api.model.SignalDesc;
import org.jbpm.services.api.model.SignalDescBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.KAFKA_EXTENSION_PREFIX;

class KafkaServerConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerConsumer.class);

    // Kafka consumer
    private Consumer<String, byte[]> consumer;
    private AtomicBoolean consumerReady = new AtomicBoolean();
    // Executor thread for dispatching signals to jbpm core
    private AtomicReference<ExecutorService> notifyService = new AtomicReference<>();
    private ProcessService processService;
    // classes information
    private Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<>();
    // topic registration class
    private KafkaServerRegistration registration = new KafkaServerRegistration();
    // synchronization 
    private Lock consumerLock = new ReentrantLock(true);
    private Condition isSubscribedCond = consumerLock.newCondition();

    private Supplier<Consumer<String, byte[]>> consumerSupplier;
    private KafkaEventProcessorFactory factory;

    public KafkaServerConsumer(KafkaEventProcessorFactory factory, Supplier<Consumer<String, byte[]>> consumerSupplier,
                               ProcessService processService) {
        this.factory = factory;
        this.consumerSupplier = consumerSupplier;
        this.processService = processService;
    }

    void addRegistration(DeploymentEvent event) {
        classLoaders.put(event.getDeploymentId(), ((KModuleDeploymentUnit) event.getDeployedUnit().getDeploymentUnit())
                .getKieContainer().getClassLoader());
        registrationUpdated(registration.addRegistration(event));
    }

    void removeRegistration(DeploymentEvent event) {
        ClassLoader cl = classLoaders.remove(event.getDeploymentId());
        registrationUpdated(registration.removeRegistration(event, t -> factory.readerUndeployed(t, cl)));
    }

    void close(Duration duration) {
        registration.close();
        if (consumerReady.compareAndSet(true, false)) {
            consumer.wakeup();
            consumerLock.lock();
            try {
                isSubscribedCond.signal();
                consumer.unsubscribe();
                consumer.close(duration);
                consumer = null;
                notifyService.getAndSet(null).shutdownNow();
            } finally {
                consumerLock.unlock();
            }
        }
        classLoaders.clear();
        processService = null;
    }


    private void registrationUpdated(Set<String> topics2Register) {

        if (consumerReady.compareAndSet(false, true)) {
            try {
                consumer = consumerSupplier.get();
            }
            catch (Exception ex) {
                consumerReady.set(false);
                throw ex;
            }
            consumer.subscribe(topics2Register);
            logger.debug("Created kafka consumer with these topics registered {}", topics2Register);
            notifyService.set(
                    new ThreadPoolExecutor(1, Integer.getInteger(KAFKA_EXTENSION_PREFIX + "maxNotifyThreads", 10),
                            60L,
                            TimeUnit.SECONDS, new LinkedBlockingQueue<>()));
            new Thread(this).start();
        } else {
            consumer.wakeup();
            consumerLock.lock();
            try {
                if (topics2Register.isEmpty()) {
                    consumer.unsubscribe();
                } else {
                    consumer.subscribe(topics2Register);
                    isSubscribedCond.signal();
                }
            } finally {
                consumerLock.unlock();
            }
            logger.debug("Updated kafka subscription list to these topics {}", topics2Register);
        }
    }

    @Override
    public void run() {
        Duration duration =
                Duration.ofSeconds(Long.getLong(KAFKA_EXTENSION_PREFIX + "poll.interval", 10L));
        logger.trace("Start polling kafka consumer every {} seconds", duration.getSeconds());
        ConsumerRecords<String, byte[]> events;
        while (consumerReady.get()) {
            events = pollEvents(duration);
            dispatchEvents(events);
        }
        logger.trace("Kafka polling stopped");
    }

    private ConsumerRecords<String, byte[]> pollEvents(Duration duration) {
        ConsumerRecords<String, byte[]> events = ConsumerRecords.empty();
        consumerLock.lock();
        try {
            while (consumerReady.get() && registration.isEmpty()) {
                isSubscribedCond.await();
            }
            if (consumerReady.get()) {
                events = consumer.poll(duration);
            }
        } catch (WakeupException ex) {
            logger.trace("Kafka wait interrupted");
        } catch (Exception ex) {
            logger.error("Error polling Kafka consumer", ex);
        } finally {
            consumerLock.unlock();
        }
        return events;
    }

    private void dispatchEvents(ConsumerRecords<String, byte[]> events) {
        if (consumerReady.get() && !events.isEmpty()) {
            if (logger.isDebugEnabled()) {
                printEventsLog(events);
            }
            for (ConsumerRecord<String, byte[]> event : events) {
                notifyService.get().submit(() -> processEvent(event));
            }
        }
    }

    private void printEventsLog(ConsumerRecords<String, byte[]> events) {
        Map<String, Integer> eventsPerTopic = new HashMap<>();
        for (ConsumerRecord<String, byte[]> event : events) {
            eventsPerTopic.compute(event.topic(), (k, v) -> v == null ? 1 : v++);
        }
        logger.debug("Number of events received per topic {}", eventsPerTopic);
    }

    @FunctionalInterface
    interface Signaller {

        void signalEvent(String deploymentId, String signalName, Object data);
    }

    private Signaller messageSignaller =
            (deployment, signalName, data) -> signalEvent(deployment, "Message-" + signalName, data);

    private void signalEvent(String deployment, String signalName, Object data) {
        processService.signalEvent(deployment, signalName, data);
    }

    private void processEvent(ConsumerRecord<String, byte[]> event) {
        registration.forEachSignal(event, this::processSignal);
        registration.forEachMessage(event, this::processMessage);
    }

    private void processSignal(ConsumerRecord<String, byte[]> event,
                               String deploymentId,
                               SignalDesc signal) {
        processEvent(event, deploymentId, signal, this::signalEvent);
    }

    private void processMessage(ConsumerRecord<String, byte[]> event,
                                String deploymentId,
                                MessageDesc message) {
        processEvent(event, deploymentId, message, messageSignaller);
    }

    private void processEvent(ConsumerRecord<String, byte[]> event,
                              String deploymentId,
                              SignalDescBase signal,
                              Signaller signaller) {
        try {
            String signalName = signal.getName();
            ClassLoader cl = classLoaders.get(deploymentId);
            Class<?> valueType = Object.class;
            String className = signal.getStructureRef();
            if (className != null) {
                valueType = cl.loadClass(className.contains(".") ? className : "java.lang." + className);
            }
            Object value = factory.getEventReader(event.topic(), cl).readEvent(event
                    .value(), valueType);
            logger.debug("Sending event with name {} to deployment {} with data {}", signalName,
                    deploymentId, value);
            signaller.signalEvent(deploymentId, signalName, value);
        } catch (ClassNotFoundException ex) {
            logger.error("Class not found in deployment id {}", deploymentId, ex);
        } catch (RuntimeException | IOException ex) {
            logger.error("Exception deserializing event", ex);
        }
    }
}
