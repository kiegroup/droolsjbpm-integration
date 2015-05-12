package org.kie.server.integrationtests.jbpm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.type.JaxbList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

import static org.junit.Assert.*;

public class ProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static KieContainer kieContainer;

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    protected KieServicesClient createDefaultClient() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        try {
            extraClasses.add(Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (TestConfig.isLocalServer()) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);

            localServerConfig.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(localServerConfig);
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(extraClasses);
            return KieServicesFactory.newKieServicesClient(configuration);
        }
    }

    @Test
    public void testStartCheckVariablesAndAbortProcess() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Class<?> personClass = Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader());
        Object person = personClass.getConstructor(new Class[]{String.class}).newInstance("john");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", "mary");
        parameters.put("number", new Integer(12345));

        List<Object> list = new ArrayList<Object>();
        list.add("item");

        parameters.put("list", new JaxbList(list));
        parameters.put("person", person);

        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        Object personVariable = client.getProcessInstanceVariable("definition-project", processInstanceId, "person", personClass);
        assertNotNull(personVariable);
        assertTrue(personClass.isAssignableFrom(personVariable.getClass()));

        Map<String, Object> variables = client.getProcessInstanceVariables("definition-project", processInstanceId);
        assertNotNull(variables);
        assertEquals(4, variables.size());
        assertTrue(variables.containsKey("test"));
        assertTrue(variables.containsKey("number"));
        assertTrue(variables.containsKey("list"));
        assertTrue(variables.containsKey("person"));

        assertNotNull(variables.get("test"));
        assertNotNull(variables.get("number"));
        assertNotNull(variables.get("list"));
        assertNotNull(variables.get("person"));

        assertTrue(String.class.isAssignableFrom(variables.get("test").getClass()));
        assertTrue(Integer.class.isAssignableFrom(variables.get("number").getClass()));
        assertTrue(List.class.isAssignableFrom(variables.get("list").getClass()));
        assertTrue(personClass.isAssignableFrom(variables.get("person").getClass()));

        assertEquals("mary", variables.get("test"));
        assertEquals(12345, variables.get("number"));
        assertEquals(1, ((List)variables.get("list")).size());
        assertEquals("item", ((List)variables.get("list")).get(0));
        assertEquals("john", valueOf(variables.get("person"), "name"));

        client.abortProcessInstance("definition-project", processInstanceId);

    }

    @Test(expected = KieServicesException.class)
    public void testStartNotExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.startProcess("definition-project", "not-existing", null);
    }


    @Test(expected = KieServicesException.class)
    public void testAbortExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.abortProcessInstance("definition-project", 9999l);
    }

    @Test(expected = KieServicesException.class)
    public void testStartCheckNonExistingVariables() throws Exception {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        Long processInstanceId = client.startProcess("definition-project", "definition-project.evaluation", parameters);
        try {
            client.getProcessInstanceVariable("definition-project", processInstanceId, "person");
        } finally {
            client.abortProcessInstance("definition-project", processInstanceId);
        }

    }

    private Object valueOf(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            return null;
        }
    }
}
