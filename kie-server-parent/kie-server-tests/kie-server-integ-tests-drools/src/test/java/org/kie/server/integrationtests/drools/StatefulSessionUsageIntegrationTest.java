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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.*;

public class StatefulSessionUsageIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "state-is-kept-for-stateful-session",
            "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateful-session";
    private static final String KIE_SESSION = "kbase1.stateful";
    private static final String PERSON_1_OUT_IDENTIFIER = "person1";
    private static final String PERSON_2_OUT_IDENTIFIER = "person2";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String PERSON_SURNAME_FIELD = "surname";
    private static final String PERSON_DUPLICATED_FIELD = "duplicated";
    private static final String PERSON_EXPECTED_SURNAME = "Vader";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/state-is-kept-for-stateful-session").getFile());

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testErrorHandlingWhenContainerIsDisposedBetweenCalls() {
        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_1_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        // now dispose the container
        ServiceResponse<Void> disposeReply = client.disposeContainer(CONTAINER_ID);
        assertEquals("Dispose reply response type.", ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());
        // and try to call the container again. The call should fail as the container no longer exists
        reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
        assertTrue("Expected message about non-instantiated container. Got: " + reply.getMsg(),
                reply.getMsg().contains(String.format("Container %s is not instantiated", CONTAINER_ID)));
    }

    @Test
    public void testStateIsKeptBetweenCalls() {
        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_1_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> reply1 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person

        ExecutionResults actualData = reply1.getResult();

        Object result = actualData.getValue(PERSON_1_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'", PERSON_EXPECTED_SURNAME, valueOf(result, PERSON_SURNAME_FIELD));
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false!", false, valueOf(result, PERSON_DUPLICATED_FIELD));


        // insert second person and fire the rules. The duplicated field will be set to true if there are two
        // persons with name "Darth Vader" the first one is from second call and this call inserts the second one.
        // In case the state of the session was not kept between the calls, the field would not be set

        commands = new ArrayList<Command<?>>();

        executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_2_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());

        ServiceResponse<ExecutionResults> reply2 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply2.getType());

        actualData = reply2.getResult();

        result = actualData.getValue(PERSON_2_OUT_IDENTIFIER);
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false!", true, valueOf(result, PERSON_DUPLICATED_FIELD));

    }

    @Test
    public void testInsertFireGetQuery() {
        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_1_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newQuery("query-result", "get people"));

        ServiceResponse<ExecutionResults> reply1 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person

        ExecutionResults actualData = reply1.getResult();

        Object result = actualData.getValue(PERSON_1_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'", PERSON_EXPECTED_SURNAME, valueOf(result, PERSON_SURNAME_FIELD));
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false!", false, valueOf(result, PERSON_DUPLICATED_FIELD));

        QueryResults queryResult = (QueryResults) actualData.getValue("query-result");
        assertNotNull(queryResult);
        assertEquals(1, queryResult.size());

        Iterator<QueryResultsRow> rowIt = queryResult.iterator();

        while (rowIt.hasNext()) {
            QueryResultsRow row = rowIt.next();
            assertNotNull(row);

            Object personResult = row.get("person");
            assertEquals("Expected surname to be set to 'Vader'", PERSON_EXPECTED_SURNAME, valueOf(personResult, PERSON_SURNAME_FIELD));

            FactHandle personFH = row.getFactHandle("person");
            assertNotNull(personFH);

            personResult = personFH;
            assertEquals("Expected surname to be null", null, valueOf(personResult, PERSON_SURNAME_FIELD));
        }
    }

    @Test
    public void testInsertFireGetQueryMultipleResults() {
        client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        Object person2 = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_1_OUT_IDENTIFIER));
        commands.add(commandsFactory.newInsert(person2, PERSON_2_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newQuery("query-result", "get people"));

        ServiceResponse<ExecutionResults> reply1 = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person
        ExecutionResults actualData = reply1.getResult();

        Object result = actualData.getValue(PERSON_1_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'.", PERSON_EXPECTED_SURNAME, valueOf(result, PERSON_SURNAME_FIELD));
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false!", true, valueOf(result, PERSON_DUPLICATED_FIELD));

        result = actualData.getValue(PERSON_2_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'", PERSON_EXPECTED_SURNAME, valueOf(result, PERSON_SURNAME_FIELD));
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false!", true, valueOf(result, PERSON_DUPLICATED_FIELD));

        QueryResults queryResult = (QueryResults) actualData.getValue("query-result");
        assertNotNull(queryResult);
        assertEquals(2, queryResult.size());

        Iterator<QueryResultsRow> rowIt = queryResult.iterator();

        while (rowIt.hasNext()) {
            QueryResultsRow row = rowIt.next();
            assertNotNull(row);

            Object personResult = row.get("person");
            assertEquals("Expected surname to be set to 'Vader'", PERSON_EXPECTED_SURNAME, valueOf(personResult, PERSON_SURNAME_FIELD));

            FactHandle personFH = row.getFactHandle("person");
            assertNotNull(personFH);

            personResult = personFH;
            assertEquals("Expected surname to be null", null, valueOf(personResult, PERSON_SURNAME_FIELD));
        }
    }
}
