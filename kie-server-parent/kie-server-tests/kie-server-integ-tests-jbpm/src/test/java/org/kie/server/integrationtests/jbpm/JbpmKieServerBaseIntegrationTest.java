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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.executor.api.STATUS;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.RequestInfoInstance;
import org.kie.server.client.JobServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public abstract class JbpmKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    protected static KieContainer kieContainer;

    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();

    protected static final String USER_YODA = "yoda";
    protected static final String USER_JOHN = "john";
    protected static final String USER_ADMINISTRATOR = "Administrator";

    protected static final String PROCESS_ID_USERTASK = "definition-project.usertask";
    protected static final String PROCESS_ID_EVALUATION = "definition-project.evaluation";
    protected static final String PROCESS_ID_CALL_EVALUATION = "definition-project.call-evaluation";
    protected static final String PROCESS_ID_GROUPTASK = "definition-project.grouptask";
    protected static final String PROCESS_ID_ASYNC_SCRIPT = "AsyncScriptTask";
    protected static final String PROCESS_ID_TIMER = "definition-project.timer-process";
    protected static final String PROCESS_ID_SIGNAL_PROCESS = "definition-project.signalprocess";
    protected static final String PROCESS_ID_SIGNAL_START = "signal-start";
    protected static final String PROCESS_ID_CUSTOM_TASK = "customtask";

    protected static final long SERVICE_TIMEOUT = 30000;
    protected static final long TIMEOUT_BETWEEN_CALLS = 200;

    protected ProcessServicesClient processClient;
    protected UserTaskServicesClient taskClient;
    protected QueryServicesClient queryClient;
    protected JobServicesClient jobServicesClient;

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    @Override
    protected void additionalConfiguration(KieServicesConfiguration configuration) throws Exception {
        super.additionalConfiguration(configuration);
        configuration.setTimeout(30000);
    }

    @Override
    protected void setupClients(KieServicesClient client) {
        this.processClient = client.getServicesClient(ProcessServicesClient.class);
        this.taskClient = client.getServicesClient(UserTaskServicesClient.class);
        this.queryClient = client.getServicesClient(QueryServicesClient.class);
        this.jobServicesClient = client.getServicesClient(JobServicesClient.class);
    }

    protected Object createPersonInstance(String name) {
        try {
            Class<?> personClass = Class.forName("org.jbpm.data.Person", true, kieContainer.getClassLoader());
            Object person = personClass.getConstructor(new Class[]{String.class}).newInstance(name);

            return person;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create person class due " + e.getMessage(), e);
        }
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

    @Override
    protected void disposeAllContainers() {
        List<Integer> status = new ArrayList<Integer>();
        status.add(org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE);
        List<ProcessInstance> activeInstances = queryClient.findProcessInstancesByStatus(status, 0, 100);
        if (activeInstances != null) {
            for (ProcessInstance instance : activeInstances) {
                processClient.abortProcessInstance(instance.getContainerId(), instance.getId());
            }
        }

        super.disposeAllContainers();
    }


    protected void waitForJobToFinish(Long jobId) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while(Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            RequestInfoInstance result = jobServicesClient.getRequestById(jobId, false, false);

            // If job finished (to one of final states) then return.
            if(STATUS.CANCELLED.toString().equals(result.getStatus()) ||
                    STATUS.DONE.toString().equals(result.getStatus()) ||
                    STATUS.ERROR.toString().equals(result.getStatus())) {
                return;
            }
            Thread.sleep(TIMEOUT_BETWEEN_CALLS);
        }
        throw new TimeoutException("Timeout while waiting for job executor to finish job.");
    }

    protected void waitForProcessInstanceToFinish(String containerId, long processInstanceId) throws Exception {
        long timeoutTime = Calendar.getInstance().getTimeInMillis() + SERVICE_TIMEOUT;
        while(Calendar.getInstance().getTimeInMillis() < timeoutTime) {
            ProcessInstance processInstance = processClient.getProcessInstance(containerId, processInstanceId);

            // If process instance is finished (to one of final states) then return.
            if(((Integer)org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED).equals(processInstance.getState()) ||
                    ((Integer)org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED).equals(processInstance.getState())) {
                return;
            }
            Thread.sleep(TIMEOUT_BETWEEN_CALLS);
        }
        throw new TimeoutException("Timeout while waiting for process instance to finish.");
    }
}
