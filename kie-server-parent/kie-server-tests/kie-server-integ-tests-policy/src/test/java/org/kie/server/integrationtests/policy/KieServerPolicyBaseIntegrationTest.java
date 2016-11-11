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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.api.KieServices;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.DocumentServicesClient;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.admin.UserTaskAdminServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public abstract class KieServerPolicyBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    protected static final String USER_YODA = "yoda";
    protected static final String USER_JOHN = "john";
    protected static final String USER_ADMINISTRATOR = "Administrator";
    protected static final String USER_MARY = "mary";

    protected static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    protected static final String CONTAINER_ID = "definition-project";

    protected static final String PROCESS_ID_EVALUATION = "definition-project.evaluation";
    protected static final String PROCESS_ID_EVALUATION_2 = "definition-project.evaluation2";

    protected ProcessServicesClient processClient;
    protected UserTaskServicesClient taskClient;
    protected QueryServicesClient queryClient;
    protected JobServicesClient jobServicesClient;
    protected DocumentServicesClient documentClient;
    protected RuleServicesClient ruleClient;
    // admin clients
    protected ProcessAdminServicesClient processAdminClient;
    protected UserTaskAdminServicesClient userTaskAdminClient;

    @BeforeClass
    public static void setupFactory() throws Exception {
        commandsFactory = KieServices.Factory.get().getCommands();
    }

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    @Before
    public void abortAllProcesses() {
        List<Integer> status = new ArrayList<Integer>();
        status.add(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        List<ProcessInstance> activeInstances = queryClient.findProcessInstancesByStatus(status, 0, 100);
        if (activeInstances != null) {
            for (ProcessInstance instance : activeInstances) {
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
        documentClient = client.getServicesClient(DocumentServicesClient.class);
        ruleClient = client.getServicesClient(RuleServicesClient.class);
        processAdminClient = client.getServicesClient(ProcessAdminServicesClient.class);
        userTaskAdminClient = client.getServicesClient(UserTaskAdminServicesClient.class);
    }
}
