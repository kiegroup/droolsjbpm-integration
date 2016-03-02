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

package org.kie.server.integrationtests.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
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

    protected Map<String, Class<?>> extraClasses = new ConcurrentHashMap<String, Class<?>>();

    protected KieServicesClient createDefaultClient() throws Exception {
        KieServicesClient kieServicesClient = null;
        // Add all extra custom classes defined in tests.
        addExtraCustomClasses(extraClasses);
        if (TestConfig.isLocalServer()) {
            configuration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null);
        }

        configuration.setMarshallingFormat(marshallingFormat);
        configuration.addJaxbClasses(new HashSet<Class<?>>(extraClasses.values()));
        configuration.setTimeout(10000000);
        additionalConfiguration(configuration);

        if(extraClasses.size() > 0) {
            // Use classloader of extra classes as client classloader
            ClassLoader classLoader = extraClasses.values().iterator().next().getClassLoader();
            kieServicesClient = KieServicesFactory.newKieServicesClient(configuration, classLoader);
        } else {
            kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        }
        setupClients(kieServicesClient);
        return kieServicesClient;
    }

    /**
     * Add custom classes needed by marshallers.
     *
     * @param extraClasses Map with classname keys and respective Class instances.
     */
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {}

    /**
     * Additional configuration of KieServicesConfiguration like timeout and such.
     *
     * @param configuration Kie server configuration to be configured.
     */
    protected void additionalConfiguration(KieServicesConfiguration configuration) throws Exception {}

    /**
     * Initialize Execution server clients.
     * Override to initialize specific clients.
     *
     * @param kieServicesClient Kie services client.
     */
    protected void setupClients(KieServicesClient kieServicesClient){}

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
}
