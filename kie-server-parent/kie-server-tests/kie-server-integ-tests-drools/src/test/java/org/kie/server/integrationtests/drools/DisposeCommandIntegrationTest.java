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

package org.kie.server.integrationtests.drools;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class DisposeCommandIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "kjar-JBPM-5019",
            "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateful-session";
    private static final String KIE_SESSION_1 = "kbase1.stateful1";
    private static final String KIE_SESSION_2 = "kbase1.stateful2";
    private static final String PERSON_1_OUT_IDENTIFIER = "person1";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String GET_OBJECTS_OUT_IDENTIFIER = "myobjects";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/kjar-JBPM-5019").getFile());

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }

    @Before
    public void cleanContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }
    
    @Test
    public void testDisposeCommand() {
        // JBPM-5019
        
        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        
        
        // for session 1
        
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION_1);
        
        commands.add(commandsFactory.newInsert(person, PERSON_1_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        
        //for session 2
        
        commands = new ArrayList<Command<?>>();
        executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION_2);

        commands.add(commandsFactory.newInsert(person, PERSON_1_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        
        // command dispose session 2
        
        
        commands = new ArrayList<Command<?>>();
        executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION_2);
        
        commands.add(commandsFactory.newDispose());
        
        reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        
        // command get object session 1: Size=1
        
        commands = new ArrayList<Command<?>>();
        executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION_1);
        
        commands.add(commandsFactory.newGetObjects(GET_OBJECTS_OUT_IDENTIFIER));
        
        reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        ExecutionResults actualData = reply.getResult();
        List<?> value = (List<?>) actualData.getValue(GET_OBJECTS_OUT_IDENTIFIER);
        assertEquals(1, value.size());
        
        // command get object session 2: Size=0 because I disposed earlier a default session and now it would be recreated 
        
        commands = new ArrayList<Command<?>>();
        executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION_2);
        
        commands.add(commandsFactory.newGetObjects(GET_OBJECTS_OUT_IDENTIFIER));
        
        reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        
        actualData = reply.getResult();
        value = (List<?>) actualData.getValue(GET_OBJECTS_OUT_IDENTIFIER);
        assertEquals(0, value.size());
    }    

}
