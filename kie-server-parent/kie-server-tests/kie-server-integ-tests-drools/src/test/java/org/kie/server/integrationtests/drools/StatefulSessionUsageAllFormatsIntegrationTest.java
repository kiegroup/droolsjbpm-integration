/*
 * Copyright 2015 JBoss Inc
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

import static org.junit.Assert.*;

public class StatefulSessionUsageAllFormatsIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "state-is-kept-for-stateful-session",
            "1.0.0-SNAPSHOT");



    @BeforeClass
    public static void deployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/state-is-kept-for-stateful-session").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    protected KieServicesClient createDefaultClient() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        try {
            extraClasses.add(Class.forName("org.kie.server.testing.Person", true, kieContainer.getClassLoader()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.marshaller = MarshallerFactory.getMarshaller(extraClasses, marshallingFormat, kieContainer.getClassLoader());
        KieServicesClient kieServicesClient = null;
        if (TestConfig.isLocalServer()) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);

            localServerConfig.addJaxbClasses(extraClasses);
            localServerConfig.setTimeout(10000);
            kieServicesClient = KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            kieServicesClient = KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }

        setupClients(kieServicesClient);
        return kieServicesClient;
    }

    @Test
    public void testErrorHandlingWhenContainerIsDisposedBetweenCalls() {
        client.createContainer("stateful-session2", new KieContainerResource("stateful-session2", releaseId));

        List<GenericCommand<?>> commands = new ArrayList<GenericCommand<?>>();

        BatchExecutionCommandImpl executionCommand = new BatchExecutionCommandImpl(commands);
        executionCommand.setLookup("kbase1.stateful");

        InsertObjectCommand insertObjectCommand = new InsertObjectCommand();
        insertObjectCommand.setOutIdentifier("person");
        insertObjectCommand.setObject(createPersonInstance("Darth"));

        FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();

        commands.add(insertObjectCommand);
        commands.add(fireAllRulesCommand);

        ServiceResponse<String> reply = ruleClient.executeCommands("stateful-session2", executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        // now dispose the container
        ServiceResponse<Void> disposeReply = client.disposeContainer("stateful-session2");
        assertEquals("Dispose reply response type.", ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());
        // and try to call the container again. The call should fail as the container no longer exists
        reply = ruleClient.executeCommands("stateful-session2", executionCommand);
        assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
        assertTrue("Expected message about non-instantiated container. Got: " + reply.getMsg(),
                reply.getMsg().contains("Container stateful-session2 is not instantiated"));
    }

    @Test
    public void testStateIsKeptBetweenCalls() {
        client.createContainer("stateful-session1", new KieContainerResource("stateful-session1", releaseId));

        List<GenericCommand<?>> commands = new ArrayList<GenericCommand<?>>();

        BatchExecutionCommandImpl executionCommand = new BatchExecutionCommandImpl(commands);
        executionCommand.setLookup("kbase1.stateful");

        InsertObjectCommand insertObjectCommand = new InsertObjectCommand();
        insertObjectCommand.setOutIdentifier("person1");
        insertObjectCommand.setObject(createPersonInstance("Darth"));

        FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();

        commands.add(insertObjectCommand);
        commands.add(fireAllRulesCommand);

        ServiceResponse<String> reply1 = ruleClient.executeCommands("stateful-session1", executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply1.getType());
        // first call should set the surname for the inserted person
        String result1 = reply1.getResult();
        ExecutionResultImpl actualData = marshaller.unmarshall(result1, ExecutionResultImpl.class);

        Object result = actualData.getValue("person1");

        assertEquals("Expected surname to be set to 'Vader'. Got response: " + result1, "Vader", valueOf(result, "surname"));
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false! Got response: " + result1, false, valueOf(result, "duplicated"));


        // insert second person and fire the rules. The duplicated field will be set to true if there are two
        // persons with name "Darth Vader" the first one is from second call and this call inserts the second one.
        // In case the state of the session was not kept between the calls, the field would not be set

        commands = new ArrayList<GenericCommand<?>>();

        executionCommand = new BatchExecutionCommandImpl(commands);
        executionCommand.setLookup("kbase1.stateful");

        insertObjectCommand = new InsertObjectCommand();
        insertObjectCommand.setOutIdentifier("person2");
        insertObjectCommand.setObject(createPersonInstance("Darth"));

        fireAllRulesCommand = new FireAllRulesCommand();

        commands.add(insertObjectCommand);
        commands.add(fireAllRulesCommand);

        ServiceResponse<String> reply2 = ruleClient.executeCommands("stateful-session1", executionCommand);
        String result2 = reply2.getResult();
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply2.getType());

        actualData = marshaller.unmarshall(result2, ExecutionResultImpl.class);

        result = actualData.getValue("person2");
        // and 'duplicated' flag should stay false, as only one person is in working memory
        assertEquals("The 'duplicated' field should be false! Got response: " + result2, true, valueOf(result, "duplicated"));

    }

    protected Object createPersonInstance(String name) {
        try {
            Class<?> personClass = Class.forName("org.kie.server.testing.Person", true, kieContainer.getClassLoader());
            Object person = personClass.getConstructor(new Class[]{String.class, String.class}).newInstance(name, "");

            return person;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create person class due " + e.getMessage(), e);
        }
    }

    protected Object valueOf(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            return null;
        }
    }
}
