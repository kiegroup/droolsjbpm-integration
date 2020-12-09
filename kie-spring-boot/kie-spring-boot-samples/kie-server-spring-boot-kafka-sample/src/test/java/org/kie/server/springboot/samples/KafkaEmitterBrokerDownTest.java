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

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.kafka.common.errors.TimeoutException;
import org.jbpm.casemgmt.api.CaseService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.springboot.samples.kafka.KieServerApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaEmitterBrokerDownTest extends KafkaFixture {
    
    @Autowired
    protected DeploymentService deploymentService;

    @Autowired
    protected ProcessService processService;
    
    protected String deploymentId;

    @BeforeClass
    public static void beforeClass() {
        generalSetup(false);
        System.setProperty("org.kie.jbpm.event.emitters.kafka.max.block.ms", "500");
    }

    @After
    public void cleanup() {
        cleanup(deploymentService);
        System.clearProperty("org.kie.jbpm.event.emitters.kafka.max.block.ms");
    }
    
    @Test(timeout = 30000)
    public void testEmitterNoKafkaBrokerTimeoutException() {
        deploymentId = setup(deploymentService, EVALUATION_PROCESS_ID);
        
        ListAppender<ILoggingEvent> listAppender = addLogAppender();
        
        Long processInstanceId = processService.startProcess(deploymentId, EVALUATION_PROCESS_ID, singletonMap("initiator", YODA));

        Optional<ILoggingEvent> logEvent = getLog(listAppender);
        assertEquals(TimeoutException.class.getCanonicalName(), logEvent.get().getThrowableProxy().getClassName());
        
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);

        processService.abortProcessInstance(processInstanceId);
        
        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
    }
}
