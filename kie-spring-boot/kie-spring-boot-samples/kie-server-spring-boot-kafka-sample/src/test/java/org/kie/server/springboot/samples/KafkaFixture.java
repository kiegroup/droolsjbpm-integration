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

package org.kie.server.springboot.samples;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.StreamSupport.stream;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jbpm.event.emitters.kafka.KafkaEventEmitter;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.springboot.utils.KieJarBuildHelper;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;


public class KafkaFixture {

    protected static final int LARGE_SIZE = 50000;
    protected static final int TOO_LARGE_SIZE = 500000;
    protected static final String GROUP_ID = "org.kie.server.testing";
    protected static final String VERSION = "1.0.0";
    
    protected static final String PROCESSES_TOPIC = "jbpm-processes-events";
    protected static final String TASKS_TOPIC = "jbpm-tasks-events";
    protected static final String CASES_TOPIC = "jbpm-cases-events";
    protected static final String CUSTOM_PROCESSES_TOPIC = "custom-processes";
    
    protected static final String PROCESS_TYPE = "process";
    protected static final String CASE_TYPE = "case";
    protected static final String TASK_TYPE = "task";

    protected static final String EVALUATION_PROCESS_ID = "evaluation";
    protected static final String EVALUATION_DESC = "Evaluation";
    
    protected static final String JOHN = "john";
    protected static final String YODA = "yoda";
    
    protected static final String CONTACT = "contact";
    protected static final String OWNER = "owner";
    protected static final String ACTUAL_OWNER = "actualOwner";
    protected static final String STATUS = "status";

    protected static final String VAR_KEY = "s";
    protected static final String VAR_VALUE= "first case started";
    protected static final String CHINESE_INITIATOR = "发起者";
    protected static final String LARGE_VAR = "large_var";
    protected static final String NULL_VAR = "null_var";
    protected static final String INITIATOR = "initiator";
    protected static final String CASE_VARIABLES = "caseVariables";
    protected static final String VARIABLES = "variables";
    protected static final String USER_TASK_CASE = "user-task-case";
    
    protected static final String PATH = "src/test/resources/kjars/";
    
    protected static final String BOOTSTRAP_SERVERS = "org.kie.jbpm.event.emitters.kafka.bootstrap.servers";
    protected static final String CLIENT_ID = "org.kie.jbpm.event.emitters.kafka.client.id";
    protected static final String TOPIC_PROCESSES = "org.kie.jbpm.event.emitters.kafka.topic.processes";
    
    protected static String bootstrapServers;
    
    protected KModuleDeploymentUnit unit = null;
    
    public static boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public static void generalSetup(boolean configure) {
        EntityManagerFactoryManager.get().clear();
        
        if (!configure) {
            return;
        }
        
        System.setProperty(BOOTSTRAP_SERVERS, bootstrapServers);
        System.setProperty(CLIENT_ID, "test_jbpm");
        
        createTopics();
    }

    public static void createTopics() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.createTopics(Arrays.asList(new NewTopic(PROCESSES_TOPIC, 1, (short) 1),
                                                   new NewTopic(TASKS_TOPIC, 1, (short) 1),
                                                   new NewTopic(CASES_TOPIC, 1, (short) 1),
                                                   new NewTopic(CUSTOM_PROCESSES_TOPIC, 1, (short) 1)))
                       .all().get(1, TimeUnit.MINUTES);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Exception when createTopics: " + e.getMessage());
        }
    }
    
    public String setup(DeploymentService ds, String artifactId) {
        KieJarBuildHelper.createKieJar(PATH + artifactId);
        unit = new KModuleDeploymentUnit(GROUP_ID, artifactId, VERSION);
        ds.deploy(unit);
        return unit.getIdentifier();
    }

    protected void cleanup(DeploymentService ds) {
        if (ds!=null) {
            ds.undeploy(unit);
        }
    }

    protected Map<String, Object> initParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(INITIATOR, CHINESE_INITIATOR);
        parameters.put(NULL_VAR, null);
        return parameters;
    }
    
    protected ListAppender<ILoggingEvent> addLogAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaEventEmitter.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        return listAppender;
    }
    
    protected Optional<ILoggingEvent> getLog(ListAppender<ILoggingEvent> listAppender) {
        Optional<ILoggingEvent> logEvent = listAppender.list.stream().filter(log -> log.getLevel() == Level.ERROR)
                .findAny();
        assertTrue("no trace printed when failed", logEvent.isPresent());
        return logEvent;
    }
    
    protected CaseFile caseFile(int valueSize) {
        Map<String, Object> data = new HashMap<>();
        data.put(VAR_KEY, VAR_VALUE);
        data.put(LARGE_VAR, RandomStringUtils.random(valueSize));
        return CaseFile.builder().data(data).build();
    }
    
    protected Map<String, OrganizationalEntity> roleAssignments() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put(OWNER, new UserImpl(YODA));
        roleAssignments.put(CONTACT, new UserImpl(JOHN));
        return roleAssignments;
    }
    
    protected void consumeAndAssertRecords(String topic, String type, int state, int expectedProcesses) {
        ConsumerRecords<String, byte[]>  records = consumeMessages(topic);
        assertEquals(expectedProcesses, records.count());
        if (records.iterator().hasNext()) {
            assertRecord(records.iterator().next(), topic, type, state);
        }
    }
    
    protected void consumeAndAssertTaskRecords(String topic, String taskStatus, String firstOwner, String lastOwner) {
        ConsumerRecords<String, byte[]>  records = consumeMessages(topic);
        assertEquals(2, records.count());
        List<ConsumerRecord<String, byte[]>> recordsList = IterableUtils.toList(records);
        assertEquals(taskStatus, getString(recordsList.get(0),STATUS));
        assertEquals(firstOwner, getString(recordsList.get(0),ACTUAL_OWNER));
        assertEquals(taskStatus, getString(recordsList.get(1),STATUS));
        assertEquals(lastOwner, getString(recordsList.get(1),ACTUAL_OWNER));
    }
    
    protected String getString(final ConsumerRecord<String, byte[]> record, String field) {
        return ((Map<String, String>) getJsonObject(record).get("data")).get(field);
    }

    protected <T> ConsumerRecords<String, T> consumeMessages(String topic) {
        try (KafkaConsumer<String, T> consumer = createConsumer(topic)) {
            ConsumerRecords<String, T> records = consumer.poll(Duration.ofSeconds(10));
            consumer.commitSync();
            return records;
        }
    }

    protected <T> KafkaConsumer<String, T> createConsumer(String topic) {
        KafkaConsumer<String, T> consumer = new KafkaConsumer<>(consumerProperties());
        consumer.subscribe(singletonList(topic));
        return consumer;
    }

    protected Properties consumerProperties() {
        Properties props = new Properties();
        props.setProperty(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(GROUP_ID_CONFIG, "jbpm_group");
        props.setProperty(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.setProperty(AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    protected void assertRecord(ConsumerRecord<String, byte[]> record, String topic, String type, int status) {
        String artifactId = (PROCESS_TYPE.equals(type))? EVALUATION_PROCESS_ID: USER_TASK_CASE;
        Map<String, Object> event = getJsonObject(record);
        
        assertNull(record.key());
        assertEquals(topic, record.topic());
        
        assertEquals(type, event.get("type"));
        assertTrue(event.get("source").toString().contains("/process/"+artifactId));
        assertTrue(event.get("data") instanceof Map);
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        assertTrue(data.get("compositeId").toString().contains("SpringBoot_"));
        assertEquals("org.kie.server.testing:"+artifactId+":1.0.0", data.get("containerId"));
        assertNotNull(data.get("id"));
        assertEquals(-1, data.get("parentId"));
        assertTrue(data.get(VARIABLES) instanceof Map);
        Map<String,Object> variables = (Map<String,Object>) data.get(VARIABLES);
        assertFalse(variables.isEmpty());
        
        if (PROCESS_TYPE.equals(type)) {
            assertEquals(status, data.get("state"));
            assertEquals(EVALUATION_PROCESS_ID, data.get("processId"));
            assertEquals(CHINESE_INITIATOR, data.get(INITIATOR));
            assertEquals("1", data.get("processVersion"));
            assertNotNull(data.get("correlationKey"));
            assertEquals(EVALUATION_DESC, data.get("processInstanceDescription"));
            assertEquals(EVALUATION_DESC, data.get("processName"));
            assertEquals(1, variables.size());
            assertEquals(CHINESE_INITIATOR, variables.get(INITIATOR));
            assertNull(variables.get(NULL_VAR));
        } else {  //case type
            assertEquals(status, data.get("caseStatus"));
            assertEquals(USER_TASK_CASE, data.get("caseDefinitionId"));
            assertEquals("Simple Case with User Tasks", data.get("caseDefinitionName"));
            assertEquals(YODA, data.get("owner"));
            assertTrue(data.get("caseId").toString().contains("HR-0000000"));
            assertEquals("Case "+VAR_VALUE, data.get("caseDescription"));
            assertTrue(data.get(CASE_VARIABLES) instanceof Map);
            Map<String,Object> caseVariables = (Map<String,Object>) data.get(CASE_VARIABLES);
            assertTrue(!caseVariables.isEmpty());
            assertEquals(VAR_VALUE, caseVariables.get(VAR_KEY));
            assertEquals(LARGE_SIZE, caseVariables.get(LARGE_VAR).toString().length());
            assertThat((List<String>) data.get("participants"), containsInAnyOrder(YODA, JOHN));
        }
    }


    protected Map<String, Object> getJsonObject(ConsumerRecord<String, byte[]> record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        ObjectMapper mapper = new ObjectMapper()
                .setDateFormat(dateFormat)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        Map<String, Object> jsonEvent = null;
        
        try {
            jsonEvent = mapper.readValue(record.value(), Map.class);
        } catch(IOException e) {
            fail("Exception when reading value: " + e.getMessage());
        }
        return jsonEvent;
    }
    
    protected Map<String, Long> groupRecordsByField(ConsumerRecords<String, byte[]> records, String field) {
        return stream(records.spliterator(), true)
              .map(r->((Map<String, Object>) getJsonObject(r).get("data")))
              .collect(groupingBy(map -> map.get(field).toString(), counting()));
    }
}