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

package org.kie.server.integrationtests.optaplanner;

import java.lang.reflect.Field;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.KieContainer;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public abstract class OptaplannerKieServerBaseIntegrationTest
        extends RestJmsSharedBaseIntegrationTest {

    protected static KieCommands commandsFactory;
    protected static KieContainer kieContainer;

    protected SolverServicesClient solverClient;

    @BeforeClass
    public static void setupFactory()
            throws Exception {
        commandsFactory = KieServices.Factory.get().getCommands();
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.solverClient = kieServicesClient.getServicesClient( SolverServicesClient.class );
    }

    @Override // Override to add kieContainer.getClassLoader() and increase timeout, just like jBPM's base overrides it
    protected KieServicesClient createDefaultClient() throws Exception {

        KieServicesClient kieServicesClient = null;
        // Add all extra custom classes defined in tests.
        addExtraCustomClasses(extraClasses);
        if (TestConfig.isLocalServer()) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null).setMarshallingFormat(marshallingFormat);
            localServerConfig.addJaxbClasses(new HashSet<Class<?>>(extraClasses.values()));
            localServerConfig.setTimeout(30000);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(localServerConfig, kieContainer.getClassLoader());
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            configuration.addJaxbClasses(new HashSet<Class<?>>(extraClasses.values()));
            configuration.setTimeout(30000);
            kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration, kieContainer.getClassLoader());
        }
        setupClients(kieServicesClient);

        return kieServicesClient;
    }

}
