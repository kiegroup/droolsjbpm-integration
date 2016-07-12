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

import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.instance.SolverInstance;
import org.kie.server.api.model.instance.SolverInstanceList;
import org.kie.server.client.KieServicesClient;
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

    @Before
    public void abortAllSolvers() {
        for(KieContainerResource container : getContainers()) {
            abortSolvers(container.getContainerId());
        }
    }

    public void abortSolvers(String containerId) {
        for(SolverInstance solver : getSolvers(containerId)) {
            ServiceResponse<Void> response = solverClient.disposeSolver(containerId, solver.getSolverId());
            Assume.assumeTrue(ServiceResponse.ResponseType.SUCCESS.equals(response.getType()));
        }
    }

    private List<KieContainerResource> getContainers() {
        ServiceResponse<KieContainerResourceList> response = client.listContainers();
        Assume.assumeTrue(ServiceResponse.ResponseType.SUCCESS.equals(response.getType()));
        return response.getResult().getContainers();
    }

    private List<SolverInstance> getSolvers(String containerId) {
        ServiceResponse<SolverInstanceList> response = solverClient.getSolvers(containerId);
        Assume.assumeTrue(ServiceResponse.ResponseType.SUCCESS.equals(response.getType()));
        return response.getResult().getContainers();
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.solverClient = kieServicesClient.getServicesClient( SolverServicesClient.class );
    }

}
