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

package org.kie.server.integrationtests.shared.basetests;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

@RunWith(Parameterized.class)
public abstract class RestJmsSharedBaseIntegrationTest extends KieServerBaseIntegrationTest {

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MarshallingFormat.JAXB, restConfiguration},
                                {MarshallingFormat.JSON, restConfiguration},
                                {MarshallingFormat.XSTREAM, restConfiguration}
                        }
        ));


        if (TestConfig.getRemotingUrl() != null && !TestConfig.skipJMS()) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]
                            {
                                    {MarshallingFormat.JAXB, jmsConfiguration},
                                    {MarshallingFormat.JSON, jmsConfiguration},
                                    {MarshallingFormat.XSTREAM, jmsConfiguration}
                            })
            );
        }

        return parameterData;
    }

    @Parameterized.Parameter(0)
    public MarshallingFormat marshallingFormat;

    @Parameterized.Parameter(1)
    public KieServicesConfiguration configuration;

    protected static KieCommands commandsFactory;
    protected static KieContainer kieContainer;

    protected static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        addExtraCustomClasses(extraClasses);
        if (TestConfig.isLocalServer()) {
            configuration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null);
        }
        return createDefaultClient(configuration, marshallingFormat);
    }


    /**
     * Instantiate custom object.
     *
     * @param objectClassIdentifier Object class identifier - usually class name.
     * @param constructorParameters Object's constructor parameters.
     * @return Instantiated object.
     */
    protected Object createInstance(String objectClassIdentifier, Object... constructorParameters) {
        Class<?>[] parameterClasses = new Class[constructorParameters.length];
        for(int i = 0; i < constructorParameters.length; i++) {
            parameterClasses[i] = constructorParameters[i].getClass();
        }

        try {
            Class<?> clazz = extraClasses.get(objectClassIdentifier);
            if (clazz != null) {
                Object object = clazz.getConstructor(parameterClasses).newInstance(constructorParameters);
                return object;
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create object due " + e.getMessage(), e);
        }
        throw new RuntimeException("Instantiated class isn't defined in extraClasses set. Please define it first.");
    }

    protected Object createPersonInstance(String name) {
        return createInstance(PERSON_CLASS_NAME, name);
    }

    /**
     * Change user used by client.
     *
     * @param username Name of user, default user taken from TestConfig in case of null parameter.
     */
    protected void changeUser(String username) throws Exception {
        if(username == null) {
            username = TestConfig.getUsername();
        }
        configuration.setUserName(username);
        client = createDefaultClient();
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

    protected void setValue(Object object, String fieldName, Object newValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, newValue);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to set value to field %s in object %s due " + e.getMessage(), fieldName, object), e);
        }
    }

    protected static KieServicesConfiguration createKieServicesJmsConfiguration() {
        try {
            InitialContext context = TestConfig.getInitialRemoteContext();

            Queue requestQueue = (Queue) context.lookup(TestConfig.getRequestQueueJndi());
            Queue responseQueue = (Queue) context.lookup(TestConfig.getResponseQueueJndi());
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(TestConfig.getConnectionFactory());

            KieServicesConfiguration jmsConfiguration = KieServicesFactory.newJMSConfiguration(
                    connectionFactory, requestQueue, responseQueue, TestConfig.getUsername(),
                    TestConfig.getPassword());

            return jmsConfiguration;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JMS client configuration!", e);
        }
    }

    protected static KieServicesConfiguration createKieServicesRestConfiguration() {
        return KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
    }

}
