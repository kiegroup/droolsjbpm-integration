/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.*;

public class DecisionTasksIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "decision-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "decision-project";
    private static final String RULE_PROCESS_ID = "evaluation.ruletask";
    private static final String DECISION_PROCESS_ID = "BPMN2-BusinessRuleTask";

    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    private static final String PERSON_JOHN = "john";



    @BeforeClass
    public static void buildAndDeployArtifacts() {
        if (TestConfig.isLocalServer()) {
            System.setProperty("kie.server.base.http.url", TestConfig.getEmbeddedKieServerHttpUrl());
        }
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/decision-project");
        if (TestConfig.isLocalServer()) {
            System.clearProperty("kie.server.base.http.url");
        }
        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testProcessWithBusinessRuleTask() throws Exception {

        Long processInstanceId = null;
        try {
            Object john = createPersonInstance(PERSON_JOHN);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("person", john);

            processInstanceId = processClient.startProcess(CONTAINER_ID, RULE_PROCESS_ID, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, processInstance.getState().intValue());


            List<VariableInstance> variables = queryClient.findVariablesCurrentState(processInstanceId);
            assertNotNull(variables);
            assertEquals(2, variables.size());
            Map<String, String> mappedVars = variables.stream().collect(Collectors.toMap(VariableInstance::getVariableName, VariableInstance::getValue));
            assertEquals("Person{name='john' age='35'}", mappedVars.get("person"));
            processInstanceId = null;

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

    @Test
    public void testProcessWithDecisionTask() throws Exception {

        Long processInstanceId = null;
        try {

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("age", 16);
            parameters.put("yearsOfService", 1);

            processInstanceId = processClient.startProcess(CONTAINER_ID, DECISION_PROCESS_ID, parameters);
            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            ProcessInstance processInstance = queryClient.findProcessInstanceById(processInstanceId);
            assertNotNull(processInstance);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, processInstance.getState().intValue());


            List<VariableInstance> variables = queryClient.findVariablesCurrentState(processInstanceId);
            assertNotNull(variables);
            assertEquals(4, variables.size());
            Map<String, String> mappedVars = variables.stream().collect(Collectors.toMap(VariableInstance::getVariableName, VariableInstance::getValue));
            assertEquals("27", mappedVars.get("vacationDays"));
            processInstanceId = null;

        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            }
        }
    }

}
