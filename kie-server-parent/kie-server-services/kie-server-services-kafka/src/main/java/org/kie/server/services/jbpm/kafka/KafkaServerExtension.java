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

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ListenerSupport;
import org.jbpm.services.api.ProcessService;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorManager;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.KAFKA_EXTENSION_PREFIX;
import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.buildEventProcessorFactory;

public class KafkaServerExtension implements KieServerExtension, DeploymentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServerExtension.class);

    public static final String EXTENSION_NAME = "Kafka";

    private KafkaServerConsumer kafkaServerConsumer;
    private KafkaServerProducer kafkaServerProducer;
    private KafkaEventProcessorFactory factory;


    private AtomicBoolean initialized = new AtomicBoolean();
    // JBPM services
    private ListenerSupport deploymentService;


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
        
        DeploymentDescriptorManager.addDescriptorLocation(
                "classpath:/META-INF/kafka-deployment-descriptor-defaults.xml");
        
        KieServerExtension jbpmExt = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExt == null) {
            logger.warn("Extension " + JbpmKieServerExtension.EXTENSION_NAME + " is required");
            return;
        }
        ProcessService processService = null;
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
        factory = buildEventProcessorFactory();
        kafkaServerConsumer = new KafkaServerConsumer(factory, this::getKafkaConsumer, processService);
        kafkaServerProducer = new KafkaServerProducer(factory, this::getKafkaProducer);
        deploymentService.addListener(this);
        initialized.set(true);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        if (deploymentService != null) {
            deploymentService.removeListener(this);
        }
        Duration duration = Duration.ofSeconds(Long.getLong(KAFKA_EXTENSION_PREFIX + "close.timeout", 30L));
        kafkaServerConsumer.close(duration);
        kafkaServerConsumer = null;
        kafkaServerProducer.close(duration);
        kafkaServerProducer = null;
        factory.close();
        factory = null;
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
        kafkaServerProducer.activate(event);
        kafkaServerConsumer.addRegistration(event);
    }

    @Override
    public void onUnDeploy(DeploymentEvent event) {
        kafkaServerProducer.deactivate(event);
        kafkaServerConsumer.removeRegistration(event);
    }

    @Override
    public void onActivate(DeploymentEvent event) {
        kafkaServerConsumer.addRegistration(event);
    }

    @Override
    public void onDeactivate(DeploymentEvent event) {
        kafkaServerConsumer.removeRegistration(event);
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
}
