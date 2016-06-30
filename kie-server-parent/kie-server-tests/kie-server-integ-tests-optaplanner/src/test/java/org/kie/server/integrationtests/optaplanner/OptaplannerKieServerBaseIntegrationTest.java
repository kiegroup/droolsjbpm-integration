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

import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.SolverServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public abstract class OptaplannerKieServerBaseIntegrationTest
        extends RestJmsSharedBaseIntegrationTest {

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

    @Override
    protected void additionalConfiguration(KieServicesConfiguration configuration) throws Exception {
        super.additionalConfiguration(configuration);
        configuration.setTimeout(60000);
    }


}
