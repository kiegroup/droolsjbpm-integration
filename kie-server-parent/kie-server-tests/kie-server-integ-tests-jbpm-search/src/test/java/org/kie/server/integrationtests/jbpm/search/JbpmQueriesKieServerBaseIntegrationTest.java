/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm.search;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.SearchServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.jbpm.search.util.DBExternalResource;
import org.kie.server.integrationtests.shared.KieServerSynchronization;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import java.util.ArrayList;
import java.util.List;


public abstract class JbpmQueriesKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    protected ProcessServicesClient processClient;
    protected UserTaskServicesClient taskClient;
    protected QueryServicesClient queryClient;
    protected SearchServicesClient searchServicesClient;
    protected JobServicesClient jobServicesClient;

    protected static final String USER_YODA = "yoda";

    protected static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    protected static final String GROUP_ID = "org.kie.server.testing";
    protected static final String VERSION = "1.0.0.Final";
    protected static final String CONTAINER_ID = "definition-project";
    protected static final String PROCESS_ID_EVALUATION = "definition-project.evaluation-search";
    protected static final String PROCESS_ID_USERTASK = "definition-project.usertask-search";
    protected static final String PROCESS_NAME_EVALUATION = "evaluation-search";
    protected static final String FIRST_TASK_NAME = "First task first";

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    @After
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
        searchServicesClient = client.getServicesClient(SearchServicesClient.class);
        jobServicesClient = client.getServicesClient(JobServicesClient.class);
    }

    @BeforeClass
    public static void deleteLog() throws Exception {
        JobServicesClient jsc = createDefaultStaticClient().getServicesClient(JobServicesClient.class);
        long id = jsc.scheduleRequest(JobRequestInstance.builder().command("org.jbpm.executor.commands.LogCleanupCommand").build());
        KieServerSynchronization.waitForJobToFinish(jsc, id);
    }
}
