package org.kie.server.integrationtests.drools;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

public class ObjectHandlingIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar",
            "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateless-kjar";
    private static final String KIE_SESSION = "kbase1.stateless";
    private static final String PERSON_OUT_IDENTIFIER = "person";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String PERSON_EXPECTED_SURNAME = "Vader";
    private static final String PERSON_SURNAME_FIELD = "surname";
    private static final String GET_OBJECTS_IDENTIFIER = "get-objects";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testGetObjects() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), configuration.getMarshallingFormat(), kjarClassLoader);
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();
        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetObjects(GET_OBJECTS_IDENTIFIER));

        ServiceResponse<String> response = ruleClient.executeCommands(CONTAINER_ID, batchExecution);
        assertSuccess(response);
        String result = response.getResult();

        ExecutionResults actualData = marshaller.unmarshall(result, ExecutionResultImpl.class);
        List<Object> listOfObjects = (List<Object>) actualData.getValue(GET_OBJECTS_IDENTIFIER);
        assertEquals(1, listOfObjects.size());

        Object returnedPerson = listOfObjects.get(0);
        assertEquals("Expected surname to be set to 'Vader'. Got response: " + result,
                PERSON_EXPECTED_SURNAME, valueOf(returnedPerson, PERSON_SURNAME_FIELD));
    }
}
