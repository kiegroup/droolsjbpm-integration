/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.jbpm.cases.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.jbpm.DBExternalResource;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;

public abstract class RestJbpmCaseBaseIntegrationTest extends RestOnlyBaseIntegrationTest {

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    protected static final String USER_YODA = "yoda";
    protected static final String USER_JOHN = "john";

    protected ProcessServicesClient processClient;
    protected QueryServicesClient queryClient;
    protected CaseServicesClient caseClient;

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    @Before
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
