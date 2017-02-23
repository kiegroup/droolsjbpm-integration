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

package org.kie.server.integrationtests.policy;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public abstract class CaseIdBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    protected static final String USER_YODA = "yoda";

    protected ProcessServicesClient processClient;
    protected QueryServicesClient queryClient;
    protected CaseServicesClient caseClient;

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    @After
    public void abortAllProcesses() {
        List<Integer> status = new ArrayList<Integer>();
        status.add(ProcessInstance.STATE_ACTIVE);
        List<org.kie.server.api.model.instance.ProcessInstance> activeInstances = queryClient.findProcessInstancesByStatus(status, 0, 100);
        if (activeInstances != null) {
            for (org.kie.server.api.model.instance.ProcessInstance instance : activeInstances) {
                processClient.abortProcessInstance(instance.getContainerId(), instance.getId());
            }
        }
    }

    @Override
    protected void setupClients(KieServicesClient client) {
        processClient = client.getServicesClient(ProcessServicesClient.class);
        queryClient = client.getServicesClient(QueryServicesClient.class);
        caseClient = client.getServicesClient(CaseServicesClient.class);
    }
}
