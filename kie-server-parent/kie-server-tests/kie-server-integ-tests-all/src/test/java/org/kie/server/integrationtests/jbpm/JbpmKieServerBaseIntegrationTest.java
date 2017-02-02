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

package org.kie.server.integrationtests.jbpm;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.api.KieServices;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.admin.CaseAdminServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public abstract class JbpmKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    protected static final String USER_YODA = "yoda";
    protected static final String USER_JOHN = "john";
    protected static final String USER_ADMINISTRATOR = "Administrator";
    protected static final String USER_MARY = "mary";

    protected static final String PROCESS_ID_USERTASK = "definition-project.usertask";
    protected static final String PROCESS_ID_EVALUATION = "definition-project.evaluation";
    protected static final String PROCESS_ID_GROUPTASK = "definition-project.grouptask";

    protected ProcessServicesClient processClient;
    protected UserTaskServicesClient taskClient;
    protected QueryServicesClient queryClient;
    protected JobServicesClient jobServicesClient;
    protected RuleServicesClient ruleClient;
    protected UIServicesClient uiServicesClient;
    protected CaseServicesClient caseClient;
    protected CaseAdminServicesClient caseAdminClient;

    @BeforeClass
    public static void setupFactory() throws Exception {
        commandsFactory = KieServices.Factory.get().getCommands();
    }

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
        taskClient = client.getServicesClient(UserTaskServicesClient.class);
        queryClient = client.getServicesClient(QueryServicesClient.class);
        jobServicesClient = client.getServicesClient(JobServicesClient.class);
        ruleClient = client.getServicesClient(RuleServicesClient.class);
        uiServicesClient = client.getServicesClient(UIServicesClient.class);
        caseClient = client.getServicesClient(CaseServicesClient.class);
        caseAdminClient = client.getServicesClient(CaseAdminServicesClient.class);
    }
}
