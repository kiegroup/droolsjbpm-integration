/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import java.util.function.BiConsumer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.MessageDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.SignalDesc;
import org.jbpm.services.api.model.SignalDescBase;
import org.kie.api.event.process.MessageEvent;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.process.SignalEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.manager.InternalRegisterableItemsFactory;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaServerExtension implements KieServerExtension, DeploymentEventListener, ProcessEventListener,
                                  Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerExtension.class);
    
    public static final String EXTENSION_NAME = "Kafka";
    static final String KAFKA_EXTENSION_PREFIX = "org.kie.server.jbpm-kafka.ext.";
    static final String TOPIC_PREFIX = KAFKA_EXTENSION_PREFIX + "topics.";
    static final String SIGNAL_MAPPING_PROPERTY = KAFKA_EXTENSION_PREFIX + "signals.mapping";
    static final String MESSAGE_MAPPING_PROPERTY = KAFKA_EXTENSION_PREFIX + "message.mapping";
    private static final Mapping SIGNAL_MAPPING_DEFAULT = Mapping.NONE;
    private static final Mapping MESSAGE_MAPPING_DEFAULT = Mapping.AUTO;
    
    enum Mapping {
        AUTO, NONE
    }
    

    private AtomicBoolean initialized = new AtomicBoolean();
    // Kafka consumer
    private Consumer<String, byte[]> consumer;
    // Kafka producer
    private Producer<String, byte[]> producer;
    // JBPM services
    private ListenerSupport deploymentService;
    private ProcessService processService;
    // Executor thread for dispatching signals to jbpm core
    private AtomicReference<ExecutorService> notifyService = new AtomicReference<>();
    // registration and classes information
    private Map<String, Map<SignalDesc, Collection<String>>> topic2Signal = new HashMap<>();
    private Map<String, Map<MessageDesc, Collection<String>>> topic2Message = new HashMap<>();
    private Map<String, Collection<Class<?>>> classes = new ConcurrentHashMap<>();
    // synchronization variables
    private AtomicBoolean consumerReady = new AtomicBoolean();
    private AtomicBoolean producerReady = new AtomicBoolean();
    private Lock changeRegistrationLock = new ReentrantLock();
    private Lock consumerLock = new ReentrantLock();
    private Condition isSubscribedCond = changeRegistrationLock.newCondition();

    @Override
    public boolean isInitialized() {
        return initialized.get();
    }

    @Override
    public boolean isActive() {
        return !Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_KAFKA_SERVER_EXT_DISABLED, "true"));
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        if (initialized.get()) {
            logger.warn("Kafka extension already initialized");
            return;
        }

        KieServerExtension jbpmExt = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExt == null) {
            logger.warn("Extension " + JbpmKieServerExtension.EXTENSION_NAME + " is required");
            return;
        }

        for (Object service : jbpmExt.getServices()) {
            if (deploymentService == null && DeploymentService.class.isAssignableFrom(service.getClass())) {
                deploymentService = (ListenerSupport) service;
            } else if (processService == null && ProcessService.class.isAssignableFrom(service.getClass())) {
                processService = (ProcessService) service;
            }
            if (deploymentService != null && processService != null) {
                break;
            }
        }
        if (deploymentService == null) {
            throw new IllegalStateException("Cannot find deployment service");
        }
        if (processService == null) {
            throw new IllegalStateException("Cannot find process service");
        }
        deploymentService.addListener(this);
        initialized.set(true);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        if (deploymentService != null) {
            deploymentService.removeListener(this);
        }
        Duration duration = Duration.ofSeconds(Long.getLong(KAFKA_EXTENSION_PREFIX + "close.timeout", 30L));
        if (producerReady.compareAndSet(true, false)) {
            producer.close(duration);
        }

        if (consumerReady.compareAndSet(true, false)) {
            notifyService.getAndSet(null).shutdownNow();
            consumer.wakeup();
            consumerLock.lock();
            try {
                consumer.unsubscribe();
                consumer.close(duration);
            } finally {
                consumerLock.unlock();
            }
            consumer = null;
        }
        changeRegistrationLock.lock();
        try {
            topic2Signal.clear();
            topic2Message.clear();
            isSubscribedCond.signal();
        } finally {
            changeRegistrationLock.unlock();
        }
        classes.clear();
        processService = null;
        deploymentService = null;
        initialized.set(false);
    }

    protected Consumer<String, byte[]> getKafkaConsumer() {
        Map<String, Object> props = initCommonConfig();
        // read only committed events
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase());
        // automatically create topics by default
        String autoCreateTopics = System.getProperty(KAFKA_EXTENSION_PREFIX +
                                                     ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG);
        if (autoCreateTopics != null) {
            props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, Boolean.parseBoolean(autoCreateTopics));
        }
        props.put(ConsumerConfig.GROUP_ID_CONFIG, System.getProperty(KAFKA_EXTENSION_PREFIX +
                                                                     ConsumerConfig.GROUP_ID_CONFIG, "jbpm-consumer"));
        return new KafkaConsumer<>(props, new StringDeserializer(), new ByteArrayDeserializer());
    }

    protected Producer<String, byte[]> getKafkaProducer() {
        Map<String, Object> props = initCommonConfig();
        String acks = System.getProperty(KAFKA_EXTENSION_PREFIX + ProducerConfig.ACKS_CONFIG);
        if (acks != null) {
            props.put(ProducerConfig.ACKS_CONFIG, acks);
        }
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.getLong(KAFKA_EXTENSION_PREFIX +
                                                                   ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000L));
        return new KafkaProducer<>(props, new StringSerializer(), new ByteArraySerializer());
    }

    private Map<String, Object> initCommonConfig() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(
                KAFKA_EXTENSION_PREFIX + CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
        String clientId = System.getProperty(KAFKA_EXTENSION_PREFIX + CommonClientConfigs.CLIENT_ID_CONFIG);
        if (clientId != null) {
            configs.put(CommonClientConfigs.CLIENT_ID_CONFIG, clientId);
        }
        return configs;
    }

    @Override
    public void onDeploy(DeploymentEvent event) {
        ((InternalRegisterableItemsFactory) ((InternalRuntimeManager) event.getDeployedUnit().getRuntimeManager())
                .getEnvironment().getRegisterableItemsFactory()).addProcessListener(this);
        updateRegistration(event, this::updateTopics);
    }

    @Override
    public void onUnDeploy(DeploymentEvent event) {
        updateRegistration(event, this::removeTopics);
    }

    @Override
    public void onActivate(DeploymentEvent event) {
        updateRegistration(event, this::updateTopics);
    }

    @Override
    public void onDeactivate(DeploymentEvent event) {
        updateRegistration(event, this::removeTopics);
    }

    private void updateTopics(String deploymentId, ProcessDefinition processDefinition) {
        if (processSignals()) {
            updateTopics(topic2Signal, deploymentId, processDefinition.getSignalsDesc());
        }
        if (processMessages()) {
            updateTopics(topic2Message, deploymentId, processDefinition.getMessagesDesc());
        }
    }

    private void removeTopics(String deploymentId, ProcessDefinition processDefinition) {
        if (processSignals()) {
            removeTopics(topic2Signal, deploymentId, processDefinition.getSignalsDesc());
        }
        if (processMessages()) {
            removeTopics(topic2Message, deploymentId, processDefinition.getMessagesDesc());
        }
    }

    private <T extends SignalDescBase> void updateTopics(Map<String, Map<T, Collection<String>>> topic2SignalBase,
                                                         String deploymentId,
                                                         Collection<T> signals) {
        for (T signal : signals) {
            if (!signal.getIncomingNodes().isEmpty()) {
                topic2SignalBase.computeIfAbsent(topicFromSignal(signal), k -> new HashMap<>()).computeIfAbsent(
                        signal, k -> new ArrayList<>()).add(deploymentId);
            }
        }
    }

    private <T extends SignalDescBase> void removeTopics(Map<String, Map<T, Collection<String>>> topic2SignalBase,
                                                         String deploymentId,
                                                         Collection<T> signalsDesc) {
        for (T signal : signalsDesc) {
            String topic = topicFromSignal(signal);
            Map<T, Collection<String>> signals = topic2SignalBase.get(topic);
            if (signals != null) {
                Collection<String> deploymentIds = signals.get(signal);
                if (deploymentIds != null) {
                    deploymentIds.remove(deploymentId);
                    if (deploymentIds.isEmpty()) {
                        signals.remove(signal);
                        if (signals.isEmpty()) {
                            topic2SignalBase.remove(topic);
                        }
                    }
                }
            }
        }
    }

    private void updateRegistration(DeploymentEvent event, BiConsumer<String, ProcessDefinition> updater) {
        classes.put(event.getDeploymentId(), event.getDeployedUnit().getDeployedClasses());
        Set<String> topic2Register = new HashSet<>();
        changeRegistrationLock.lock();
        try {
            for (DeployedAsset asset : event.getDeployedUnit().getDeployedAssets()) {
                updater.accept(event.getDeploymentId(), (ProcessDefinition) asset);
            }
            topic2Register.addAll(topic2Signal.keySet());
            topic2Register.addAll(topic2Message.keySet());
        } finally {
            changeRegistrationLock.unlock();
        }

        if (topic2Register.isEmpty()) {
            if (consumerReady.get()) {
                consumer.wakeup();
                unsubscribe();
            }
        }
        else if (consumerReady.compareAndSet(false, true)) {
            logger.trace("Creating kafka consumer");
            consumer = getKafkaConsumer();
            subscribe(topic2Register);
            notifyService.set(
                    new ThreadPoolExecutor(1, Integer.getInteger(KAFKA_EXTENSION_PREFIX + "maxNotifyThreads", 10), 60L,
                            TimeUnit.SECONDS, new LinkedBlockingQueue<>()));
            new Thread(this).start();
        } else {
            consumer.wakeup();
            subscribe(topic2Register);
            changeRegistrationLock.lock();
            try {
                isSubscribedCond.signal();
            } finally {
                changeRegistrationLock.unlock();
            }
        }
    }

    private void subscribe(Set<String> topic2Register) {
        consumerLock.lock();
        try {
            consumer.subscribe(topic2Register);
        } finally {
            consumerLock.unlock();
        }
        logger.debug("Updated kafka subscription list to these topics {}", topic2Register);
    }

    private void unsubscribe() {
        consumerLock.lock();
        try {
            consumer.unsubscribe();
        } finally {
            consumerLock.unlock();
        }
        logger.debug("All topics unsubscribed");
    }

    private static <T extends SignalDescBase> String topicFromSignal(T signal) {
        return topicFromSignal(signal.getName());
    }
    
    private static String topicFromSignal(String name) {
        return System.getProperty(TOPIC_PREFIX + name, name);
    }


    @Override
    public boolean isUpdateContainerAllowed(String id,
                                            KieContainerInstance kieContainerInstance,
                                            Map<String, Object> parameters) {
        return true;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BPM_KAFKA;
    }

    @Override
    public List<Object> getServices() {
        return Collections.emptyList();
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 20;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }


    @Override
    public void run() {
        Duration duration =
                Duration.ofSeconds(Long.getLong(KAFKA_EXTENSION_PREFIX + "poll.interval", 10L));
        logger.trace("Start polling kafka consumer every {} seconds", duration.getSeconds());
        try {
            while (consumerReady.get()) {
                checkSubscribed();
                dispatchEvents(pollEvents(duration));
            }
        } catch (InterruptedException e) {
            logger.warn("Polling thread interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Polling thread unexpectedly finished", e);
        }
        logger.trace("Kafka polling stopped");
    }

    private void checkSubscribed() throws InterruptedException {
        changeRegistrationLock.lock();
        try {
            while (consumerReady.get() && topic2Signal.isEmpty() && topic2Message.isEmpty()) {
                isSubscribedCond.await();
            }
        } finally {
            changeRegistrationLock.unlock();
        }
    }

    private ConsumerRecords<String, byte[]> pollEvents(Duration duration) {
        ConsumerRecords<String, byte[]> events = ConsumerRecords.empty();
        if (consumerReady.get()) {
            consumerLock.lock();
            try {
                events = consumer.poll(duration);
            } catch (WakeupException ex) {
                logger.trace("Kafka wait interrupted");
            } catch (Exception ex) {
                logger.error("Error polling Kafka consumer", ex);
            } finally {
                consumerLock.unlock();
            }
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
    private interface Signaller {

        void signalEvent(String deploymentId, String signalName, Object data);
    }

    private Signaller messageSignaller = (deployment, signalName, data) -> signalEvent(deployment, "Message-" +
                                                                                                   signalName, data);

    private void processEvent(ConsumerRecord<String, byte[]> event) {
        changeRegistrationLock.lock();
        try {
            processEvent(topic2Signal, event, this::signalEvent);
            processEvent(topic2Message, event, messageSignaller);
        } finally {
            changeRegistrationLock.unlock();
        }
    }


    private void signalEvent(String deployment, String signalName, Object data) {
        processService.signalEvent(deployment, signalName, data);
    }

    private <T extends SignalDescBase> void processEvent(Map<String, Map<T, Collection<String>>> topic2SignalBase,
                                                         ConsumerRecord<String, byte[]> event,
                                                         Signaller signaller) {
        Map<T, Collection<String>> signalInfo = topic2SignalBase.get(event.topic());
        if (signalInfo != null) {
            for (Map.Entry<T, Collection<String>> entry : signalInfo.entrySet())
                try {
                    String signalName = entry.getKey().getName();
                    for (String deploymentId : entry.getValue()) {
                        CloudEvent<?> cloudEvent = CloudEvent.read(event.value(), getDataClass(deploymentId, entry
                                .getKey()));
                        logger.debug("Sending event with name {} to deployment {} with data {}", signalName,
                                deploymentId, cloudEvent.getData());
                        signaller.signalEvent(deploymentId, signalName, cloudEvent.getData());
                    }
                } catch (IOException | ParseException | ClassNotFoundException e) {
                    logger.error("Error deserializing event", e);
                }
        }
    }

    private <T extends SignalDescBase> Class<?> getDataClass(String deploymentId,
                                                             T signalDesc) throws ClassNotFoundException {
        Optional<Class<?>> dataClazz = Optional.empty();
        String className = signalDesc.getStructureRef();
        if (className != null) {
            Collection<Class<?>> deployedClasses = classes.get(deploymentId);
            if (deployedClasses != null) {
                dataClazz = deployedClasses.stream().filter(c -> c.getCanonicalName().equals(className) || c
                        .getSimpleName().equals(className) || c.getTypeName().equals(className)).findAny();
            }
            if (!dataClazz.isPresent()) {
                logger.debug("Class {} has not been found in deployment {}, trying from classloader", className,
                        deploymentId);
                dataClazz = Optional.of(Class.forName(className.contains(".") ? className : "java.lang." + className));
            }
        }
        return dataClazz.orElse(Object.class);
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

        if (producerReady.compareAndSet(false, true)) {
            producer = getKafkaProducer();
        }
        try {
            producer.send(new ProducerRecord<>(topicFromSignal(name), CloudEvent.write(processInstance.getProcessId(),
                    processInstance.getId(), value)), (m, e) -> {
                        if (e != null) {
                            logError(value, e);
                        }
                    });
        } catch (Exception e) {
            logError(value, e);
        }
        
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

    private static boolean processMessages() {
        return getMapping(MESSAGE_MAPPING_PROPERTY, MESSAGE_MAPPING_DEFAULT) == Mapping.AUTO;
    }

    private static boolean processSignals() {
        return getMapping(SIGNAL_MAPPING_PROPERTY, SIGNAL_MAPPING_DEFAULT) == Mapping.AUTO;
    }

    private static boolean processSignals(SignalEvent event) {
        Mapping mapping = getMapping(SIGNAL_MAPPING_PROPERTY, SIGNAL_MAPPING_DEFAULT);
        return mapping == Mapping.AUTO || "##kafka".equalsIgnoreCase((String) event.getNodeInstance().getNode()
                .getMetaData().get("implementation"));
    }

    private void logError(Object value, Exception e) {
        logger.error("Error publishing event {}", value, e);
    }

    @Override
    public void serverStarted() {
        // will use lazy initialization for consumer
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // will be done by listener
    }

    @Override
    public void prepareContainerUpdate(String id,
                                       KieContainerInstance kieContainerInstance,
                                       Map<String, Object> parameters) {
        // will be done by listener
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // will be done by listener
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // will be done by listener
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        return Collections.emptyList();
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }


    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        // not interested
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        // not interested

    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        // not interested

    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        // not interested

    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        // not interested
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        // not interested

    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        // not interested
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        // not interested

    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        // not interested

    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        // not interested

    }

}
