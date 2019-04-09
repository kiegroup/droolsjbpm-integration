/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.spring.jbpm.services;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.kie.spring.beans.NonSerializableObject;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/jbpm/services/jta-services.xml"})
@Rollback(true)
@Transactional
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class JtaJbpmServicesRollBackTXTest extends AbstractJbpmServicesTest {

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private UserTaskService userTaskService;

    @Autowired
    private ProcessService processService;


    @After
    public void cleanup() {
        deploymentService.undeploy(new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION));
    }

    @Test
    public void testWithPerProcessInstanceStrategy() {

        testProcessWithStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
    }

    

    private void testProcessWithStrategy(RuntimeStrategy strategy) {

        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);
        deploymentUnit.setStrategy(strategy);

        deploymentService.deploy(deploymentUnit);
        String userId = "john";

        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "hiring");

        try {
        	processService.setProcessVariable(processInstanceId, "test", new NonSerializableObject());
        	fail("transaction should be rolled back.");
        	
        }catch(Exception e) {
        	
        	assertEquals("Unable to commit transaction", e.getMessage());
        	
        } finally {
        	processService.abortProcessInstance(processInstanceId);
        }

        

       
    }



}
