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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;
import static org.kie.api.task.model.Status.Completed;
import static org.kie.api.task.model.Status.InProgress;
import static org.kie.api.task.model.Status.Ready;
import static org.kie.api.task.model.Status.Reserved;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.jbpm.casemgmt.api.CaseService;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.server.springboot.samples.kafka.KieServerApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.KafkaContainer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaEmitterHappyPathTest extends KafkaFixture {
    
    protected static KafkaContainer kafka = new KafkaContainer();
    
    @Autowired
    protected DeploymentService deploymentService;

    @Autowired
    protected ProcessService processService;
    
    @Autowired
    protected CaseService caseService;
    
    @Autowired
    protected UserTaskService userTaskService;
    
    @Autowired
    protected RuntimeDataService runtimeDataService;
    
    protected String deploymentId;

    @BeforeClass
    public static void beforeClass() {
        checkRightOSForTestContainers();
        kafka.start();
        bootstrapServers = kafka.getBootstrapServers();
        generalSetup(true);
    }


    @After
    public void cleanup() {
        cleanup(deploymentService);
    }
    
    @AfterClass
    public static void teardown() {
        kafka.stop();
        System.clearProperty("org.kie.jbpm.event.emitters.kafka.boopstrap.servers");
        System.clearProperty("org.kie.jbpm.event.emitters.kafka.client.id");
    }

    @Test(timeout = 30000)
    public void testKafkaEmitterProcessStartAndAbort() {
        deploymentId = setup(deploymentService, EVALUATION_PROCESS_ID);
        
        Long processInstanceId = processService.startProcess(deploymentId, EVALUATION_PROCESS_ID, initParameters());

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        
        consumeAndAssertRecords(PROCESSES_TOPIC, PROCESS_TYPE, STATE_ACTIVE, 1);

        processService.abortProcessInstance(processInstanceId);
        
        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
                
        consumeAndAssertRecords(PROCESSES_TOPIC, PROCESS_TYPE, STATE_ABORTED, 1);
    }
    
    @Test(timeout = 30000)
    public void testKafkaEmitterProcessStartAndWorkOnUserTasks() {
        deploymentId = setup(deploymentService, EVALUATION_PROCESS_ID);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("employee", JOHN);
        parameters.put("reason", "autoevaluation");
        Long processInstanceId = processService.startProcess(deploymentId, EVALUATION_PROCESS_ID, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        
        List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(JOHN, new QueryFilter());
        assertEquals(1, tasks.size());
        
        userTaskService.completeAutoProgress(tasks.get(0).getId(), JOHN, new HashMap<>());
        
        tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(JOHN, new QueryFilter());
        assertEquals(2, tasks.size());
        
        tasks.forEach(t -> userTaskService.completeAutoProgress(t.getId(), JOHN, new HashMap<>()));
        
        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
        
        ConsumerRecords<String, byte[]>  records = consumeMessages(TASKS_TOPIC);
        assertEquals(11, records.count());
        Map<String, Long> taskRecordsByStatus = groupRecordsByField(records, "status");
        
        assertEquals(2, taskRecordsByStatus.get(Ready.name()).intValue());
        assertEquals(3, taskRecordsByStatus.get(Reserved.name()).intValue());
        assertEquals(3, taskRecordsByStatus.get(InProgress.name()).intValue());
        assertEquals(3, taskRecordsByStatus.get(Completed.name()).intValue());
        
        records = consumeMessages(PROCESSES_TOPIC);
        assertEquals(4, records.count());
        Map<String, Long> processRecordsByStatus =  groupRecordsByField(records, "state");
        
        assertEquals(3, processRecordsByStatus.get("1").intValue()); //Active
        assertEquals(1, processRecordsByStatus.get("2").intValue()); //Completed
        
    }
    
    @Test(timeout = 30000)
    public void testKafkaEmitterCaseStartAndAbort() {
        deploymentId = setup(deploymentService, USER_TASK_CASE);
        
        CaseFileInstance caseFileInstance = caseService.newCaseFileInstance(deploymentId, 
                                                                            USER_TASK_CASE, 
                                                                            caseFile(LARGE_SIZE).getData(),
                                                                            roleAssignments());
        
        String caseId = caseService.startCase(deploymentId, USER_TASK_CASE, caseFileInstance);
        assertNotNull(caseId);
        
        consumeAndAssertRecords(CASES_TOPIC, CASE_TYPE, STATE_ACTIVE, 1);
        caseService.cancelCase(caseId);
        
        consumeAndAssertRecords(CASES_TOPIC, CASE_TYPE, STATE_ABORTED, 1);
        
        ConsumerRecords<String, byte[]>  records = consumeMessages(TASKS_TOPIC);
        assertEquals(2, records.count());
    }
    
    @Test(timeout = 30000)
    public void testEmitterRecordTooLargeException() {
        deploymentId = setup(deploymentService, USER_TASK_CASE);
        
        ListAppender<ILoggingEvent> listAppender = addLogAppender();
        
        CaseFileInstance caseFileInstance = caseService.newCaseFileInstance(deploymentId, 
                                                                            USER_TASK_CASE, 
                                                                            caseFile(TOO_LARGE_SIZE).getData(),
                                                                            roleAssignments());
        
        String caseId = caseService.startCase(deploymentId, USER_TASK_CASE, caseFileInstance);
        assertNotNull(caseId);
        
        Optional<ILoggingEvent> logEvent = getLog(listAppender);
        assertEquals(RecordTooLargeException.class.getCanonicalName(), logEvent.get().getThrowableProxy().getClassName());
        
        consumeAndAssertRecords(CASES_TOPIC, CASE_TYPE, STATE_ACTIVE, 0);
        caseService.cancelCase(caseId);
        
        consumeAndAssertRecords(CASES_TOPIC, CASE_TYPE, STATE_ABORTED, 0);
        
        ConsumerRecords<String, byte[]>  records = consumeMessages(TASKS_TOPIC);
        assertEquals(2, records.count());
    }

}
