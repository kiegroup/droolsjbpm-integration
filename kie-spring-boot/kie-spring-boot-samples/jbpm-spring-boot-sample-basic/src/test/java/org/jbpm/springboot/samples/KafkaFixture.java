/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.springboot.samples;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.springboot.samples.entities.Box;
import org.jbpm.springboot.samples.serialization.BoxDeserializer;
import org.jbpm.springboot.samples.utils.KieJarBuildHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;

import static java.util.Collections.singletonList;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.kie.internal.runtime.conf.RuntimeStrategy.SINGLETON;

public class KafkaFixture {

    private static final Logger logger = LoggerFactory.getLogger(KafkaFixture.class);
    
    protected static final String ARTIFACT_ID = "kafka-process";
    protected static final String GROUP_ID = "com.myspace";
    protected static final String VERSION = "1.0.0-SNAPSHOT";
    
    protected static final String KAFKA_PROCESS_ID = "kafka-process.kafka-process";
    protected static final String KAFKA_FLOW_ID = "kafka-process.kafka-flow";
    protected static final String KAFKA_RESULT = "kafka-result";

    protected static final String PATH = "src/test/resources/kjars/kafka-process";
    protected static final String TEMPLATE_FILE = "src/test/resources/templates/kie-deployment-descriptor.template";
    protected static final String DEPLOYMENT_DESCRIPTOR_FILE = PATH+"/src/main/resources/META-INF/kie-deployment-descriptor.xml";
    protected static final String STRATEGY_TEMPLATE = "STRATEGY_TEMPLATE";
    
    protected static final String TOPIC = "mytopic";
    protected static final String KEY = "mykey";
    protected static final String VALUE = "myvalue";

    protected KModuleDeploymentUnit unit = null;
    
    
    public void generalSetup() {
        EntityManagerFactoryManager.get().clear();
    }

    public String setup(DeploymentService ds, String strategy) {
        Map<String, String> map = new HashMap<>();
        if (SINGLETON.name().equals(strategy)) {
            map.put("env['"+BOOTSTRAP_SERVERS_CONFIG+"']", "\""+System.getProperty(BOOTSTRAP_SERVERS_CONFIG)+"\"");
            map.put("env['"+CLIENT_ID_CONFIG+"']", "\""+System.getProperty(CLIENT_ID_CONFIG)+"\"");
            map.put("env['"+KEY_SERIALIZER_CLASS_CONFIG+"']", "\""+System.getProperty(KEY_SERIALIZER_CLASS_CONFIG)+"\"");
            map.put("env['"+VALUE_SERIALIZER_CLASS_CONFIG+"']", "\""+System.getProperty(VALUE_SERIALIZER_CLASS_CONFIG)+"\"");
        }
        map.put(STRATEGY_TEMPLATE, strategy);
        
        KieJarBuildHelper.replaceInFile(TEMPLATE_FILE, DEPLOYMENT_DESCRIPTOR_FILE, map);
        KieJarBuildHelper.createKieJar(PATH);
        unit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        ds.deploy(unit);
        return unit.getIdentifier();
    }

    public void createTopic(KafkaContainer kafka) throws IOException, InterruptedException {
        //create the topic in the broker, though TestContainers have autocreation feature enabled
        kafka.execInContainer("/bin/sh", "-c", "/usr/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic "+TOPIC);
    }
    
    protected void cleanup(DeploymentService ds) {
        if (ds!=null) {
            ds.undeploy(unit);
        }
        try {
            Files.deleteIfExists(Paths.get(DEPLOYMENT_DESCRIPTOR_FILE));
        } catch (IOException e) {
            logger.warn("File {} could not be deleted ", DEPLOYMENT_DESCRIPTOR_FILE, e);
        }
    }

    protected Map<String, Object> getProcessVariables() {
        Map<String, Object> map = new HashMap<>();
        map.put("kafka-topic", TOPIC);
        map.put("kafka-key", KEY);
        map.put("kafka-value", VALUE);
        return map;
    }
    
    protected Map<String, Object> getFlowVariables() {
        Map<String, Object> map = new HashMap<>();
        map.put("kafka-topic", TOPIC);
        map.put("triggername", KEY);
        return map;
    }

    protected void assertConsumerMessages(String bootstrapServers) {
        ConsumerRecords<String, String>  records = consumeMessages(bootstrapServers, StringDeserializer.class.getName());
        assertEquals(1, records.count());
        assertEquals(KEY, records.iterator().next().key());
        assertEquals(VALUE, records.iterator().next().value());
    }
    
    protected void assertConsumerMessagesBox(String bootstrapServers) {
        ConsumerRecords<String, Box>  records = consumeMessages(bootstrapServers, BoxDeserializer.class.getName());
        assertEquals(2, records.count());
        records.forEach(rec -> {
            if (KEY.equals(rec.key())) {
                assertEquals(new Box(new BigInteger("10000000"), Arrays.asList(10,20,30), "caja507", true), 
                        rec.value());
            } else if ("second-key".equals(rec.key())) {
                assertEquals(new Box(new BigInteger("999"), Arrays.asList(40,50), "dial999", false), 
                        rec.value());
            } else {
                fail("Should have received one of those keys");
            }
        });
    }

    protected <T> ConsumerRecords<String, T> consumeMessages(String bootstrapServers, String deserializer) {
        try (KafkaConsumer<String, T> consumer = createConsumer(bootstrapServers, deserializer)) {
            ConsumerRecords<String, T> records = consumer.poll(Duration.ofSeconds(10));
            consumer.commitSync();
            return records;
        }
    }
    
    protected <T> KafkaConsumer<String, T> createConsumer(String bootstrapServers, String deserializer) {
        KafkaConsumer<String, T> consumer = new KafkaConsumer<>(consumerProperties(bootstrapServers, deserializer));
        consumer.subscribe(singletonList(TOPIC));
        return consumer;
    }

    protected Properties consumerProperties(String bootstrapServers, String deserializer) {
        Properties props = new Properties();
        props.setProperty(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(GROUP_ID_CONFIG, "jbpm_group");
        props.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        props.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
    
}
