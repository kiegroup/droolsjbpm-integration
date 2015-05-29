package org.kie.server.integrationtests.jbpm;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.api.runtime.KieContainer;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public abstract class JbpmKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    protected static KieContainer kieContainer;

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();


    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }


    protected Object createPersonInstance(String name) {
        try {
            Class<?> personClass = Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader());
            Object person = personClass.getConstructor(new Class[]{String.class}).newInstance(name);

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

    /**
     * Change user used by client.
     *
     * @param username Name of user, default user taken from TestConfig in case of null parameter.
     */
    protected void changeUser(String username) {
        if(username == null) {
            username = TestConfig.getUsername();
        }
        configuration.setUserName(username);
        client = createDefaultClient();
    }
}
