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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.SignalDesc;
import org.jbpm.services.api.model.SignalType;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaServerExtension implements KieServerExtension, DeploymentEventListener, Runnable {

    public static final String EXTENSION_NAME = "Kafka";
    static final String KAFKA_EXTENSION_PREFIX = "org.kie.server.jbpm-kafka.ext.";
    static final String TOPIC_PREFIX = KAFKA_EXTENSION_PREFIX + "topics.";
    private static final Logger logger = LoggerFactory.getLogger(KafkaServerExtension.class);

    private AtomicBoolean initialized = new AtomicBoolean();
    private Consumer<String, byte[]> consumer;
    private ListenerSupport deploymentService;
    private ProcessService processService;
    private ScheduledExecutorService pollService;
    private ExecutorService notifyService;

    private Map<String, SignalInfo> topic2Signal = new ConcurrentHashMap<>();

    @Override
    public boolean isInitialized() {
        return initialized.get();
    }

    @Override
    public boolean isActive() {
        return !Boolean.getBoolean(KieServerConstants.KIE_KAFKA_SERVER_EXT_DISABLED);
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        if (initialized.getAndSet(true)) {
            return;
        }
        pollService = Executors.newSingleThreadScheduledExecutor();
        notifyService = Executors.newCachedThreadPool();
        consumer = getKafkaConsumer();
        KieServerExtension jbpmExt = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExt == null) {
            throw new IllegalStateException("Cannot find extension " + JbpmKieServerExtension.EXTENSION_NAME);
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
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        pollService.shutdownNow();
        pollService = null;
        notifyService.shutdownNow();
        notifyService = null;
        if (deploymentService != null) {
            deploymentService.removeListener(this);
            deploymentService = null;
        }
        if (consumer != null) {
            consumer.close();
            consumer = null;
        }
        initialized.set(false);
    }

    protected Consumer<String, byte[]> getKafkaConsumer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty(
                KAFKA_EXTENSION_PREFIX + "boopstrap.servers", "localhost:9092"));
        // read only commited events
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase());
        // do not automatically create topics by default
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, Boolean.getBoolean(
                KAFKA_EXTENSION_PREFIX + "auto.create.topics"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, System.getProperty(KAFKA_EXTENSION_PREFIX + "group.id",
                "jbpm-consumer"));
        return new KafkaConsumer<>(props, new StringDeserializer(), new ByteArrayDeserializer());
    }

    @Override
    public void onDeploy(DeploymentEvent event) {
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
        for (SignalDesc signal : processDefinition.getSignalsMetadata()) {
            topic2Signal.computeIfAbsent(topicFromSignal(signal), t -> new SignalInfo(deploymentId,
                    signal));
        }
    }

    private void removeTopics(String deploymentId, ProcessDefinition processDefinition) {
        for (SignalDesc signal : processDefinition.getSignalsMetadata()) {
            topic2Signal.remove(topicFromSignal(signal));
        }
    }

    private void updateRegistration(DeploymentEvent event, BiConsumer<String, ProcessDefinition> updater) {
        if (consumer != null) {
            for (DeployedAsset asset : event.getDeployedUnit().getDeployedAssets()) {
                updater.accept(event.getDeploymentId(), (ProcessDefinition) asset);
            }
            synchronized (consumer) {
                consumer.subscribe(topic2Signal.keySet());
            }
        }
    }

    private String topicFromSignal(SignalDesc signal) {
        return System.getProperty(TOPIC_PREFIX + signal.getName(), signal.getName());
    }

    @Override
    public void serverStarted() {
        if (pollService != null) {
            long delay = Long.getLong(KAFKA_EXTENSION_PREFIX + "poll.interval", 1L);
            pollService.scheduleWithFixedDelay(this, delay, delay, TimeUnit.SECONDS);
        }
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // will be done by listener
    }

    @Override
    public boolean isUpdateContainerAllowed(String id,
                                            KieContainerInstance kieContainerInstance,
                                            Map<String, Object> parameters) {
        return false;
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
    public void run() {
        if (consumer != null && !topic2Signal.isEmpty()) {
            ConsumerRecords<String, byte[]> events;
            synchronized (consumer) {
                events = consumer.subscription().isEmpty() ? ConsumerRecords.empty() : consumer.poll(Duration.ZERO);
            }
            for (ConsumerRecord<String, byte[]> event : events) {
                notifyService.submit(() -> processEvent(event));
            }
        }
    }

    private void processEvent(ConsumerRecord<String, byte[]> event) {
        SignalInfo signalInfo = topic2Signal.get(event.topic());
        if (signalInfo != null) {
            try {
                String type = signalInfo.getSignalDesc().getName();
                if (signalInfo.getSignalDesc().getSignalType() == SignalType.MESSAGE) {
                    type = "Message-" + type;
                }
                processService.signalEvent(signalInfo.getDeploymentId(), type, CloudEvent.read(event.value(),
                        getDataClass(signalInfo.getSignalDesc())).getData());
            } catch (IOException | ParseException e) {
                logger.error("Error deserializing event", e);
            }
        }
    }

    private Class<?> getDataClass(SignalDesc signalDesc) {
        Class<?> dataClazz = Object.class;
        if (signalDesc.getStructureRef() != null) {
            try {
                String className = signalDesc.getStructureRef();
                if (!className.contains(".")) {
                    className = "java.lang." + className;
                }
                dataClazz = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                logger.error("Invalid class in structure ref", ex);
            }
        }
        return dataClazz;
    }
}
