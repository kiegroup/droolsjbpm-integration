/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.rest.query;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.services.api.DeploymentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.remote.services.rest.query.helpers.InternalProcInstQueryHelper;
import org.kie.remote.services.rest.query.helpers.InternalTaskQueryHelper;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;

/**
 * This tests Internal*QueryHelper logic
 */
public class QueryResourceQueryHelperTest extends AbstractQueryResourceTest {

    public static final String OBJECT_VARIABLE_PROCESS_ID = "org.jboss.qa.bpms.ObjectVariableProcess";
    public static final String OBJECT_VARIABLE_PROCESS_FILE = "BPMN2-ObjectVariables.bpmn2";

    private QueryResourceImpl queryResource = null;
    private InternalTaskQueryHelper queryTaskHelper = null;
    private InternalProcInstQueryHelper queryProcInstHelper = null;

    private Map<String, String[]> queryParams;

    @Before
    public void init() {
        queryParams = new HashMap<String, String[]>();

        runtimeManager = createRuntimeManager(OBJECT_VARIABLE_PROCESS_FILE, PROCESS_STRING_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();

        queryResource = new QueryResourceImpl();

        IdentityProvider mockIdProvider = mock(IdentityProvider.class);
        when(mockIdProvider.getName()).thenReturn(USER_ID);
        queryResource.setIdentityProvider(mockIdProvider);

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getRequestURI()).thenReturn("http://localhost:8080/kie-wb/rest/query/runtime/process");
        when(mockHttpRequest.getParameterMap()).thenReturn(queryParams);
        queryResource.setHttpServletRequest(mockHttpRequest);

        HttpHeaders mockHeaders = mock(HttpHeaders.class);
        when(mockHeaders.getRequestHeaders()).thenReturn(new MultivaluedMapImpl<String, String>());
        queryResource.setHeaders(mockHeaders);

        // JbpmJUnitBaseTestCase.userGroupCallBack (classpath:/usergroups.properties)
        queryResource.setUserGroupCallback(userGroupCallback);

        ProcessRequestBean processRequestBean = new ProcessRequestBean();
        UserTaskServiceImpl userTaskService = new UserTaskServiceImpl();
        userTaskService.setNonProcessScopedTaskService((InternalTaskService) taskService);
        processRequestBean.setUserTaskService(userTaskService);
        DeploymentService mockDepService = mock(DeploymentService.class);
        when(mockDepService.getRuntimeManager(anyString())).thenReturn(runtimeManager);
        userTaskService.setDeploymentService(mockDepService);
        queryResource.setProcessRequestBean(processRequestBean);

        processRequestBean.setAuditLogService(new JPAAuditLogService(getEmf()));
        processRequestBean.setJPAService(new RemoteServicesQueryJPAService(getEmf()));

        queryTaskHelper = new InternalTaskQueryHelper(queryResource);
        queryProcInstHelper = new InternalProcInstQueryHelper(queryResource);
    }

    @After
    public void cleanup() {
        if( runtimeManager != null ) {
            runtimeManager.disposeRuntimeEngine(engine);
            runtimeManager.close();
        }
    }

    protected long[] runObjectVarProcess( KieSession ksession) {
        long[] pids = new long[3];

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myobject", "Hello World!");

        ProcessInstance pi = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, params); // completed
        pids[0] = pi.getId();
        params.put("myobject", "Hello Ivo!");
        pi = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, params); // completed
        pids[1] = pi.getId();
        params.put("myobject", "Bye Ivo!");
        pi = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, params); // completed
        pids[2] = pi.getId();

        return pids;
    }

    @Test
    public void notBeingFilteredTest() {
        // setup
        runObjectVarProcess(ksession);

        int [] pageInfo = { 0, 0 };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        addParams(queryParams, "processinstancestatus", "2");
        addParams(queryParams, "varregex_myobject", "Hello .*");

        JaxbQueryProcessInstanceResult result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "Null result", result );
        assertFalse( "Empty result (all)", result.getProcessInstanceInfoList().isEmpty() );
        assertEquals( "Process instance info results", 2, result.getProcessInstanceInfoList().size() );
        for( JaxbQueryProcessInstanceInfo queryInfo : result.getProcessInstanceInfoList() ) {
           assertNotNull( "No process instance info!", queryInfo.getProcessInstance() );
           assertEquals( "No variable info!", 1, queryInfo.getVariables().size() );
        }
    }

    @Test
    public void rejectTaskParamsForProcQueries() {
        String badParam = "taskid_min";
        addParams(queryParams, badParam, "2");

        try {
            queryResource.queryProcessInstances();
            fail( "The query proces instances operation should have failed!");
        } catch( KieRemoteRestOperationException krroe ) {
           assertTrue( krroe.getMessage().contains(badParam) );
           assertEquals( 400, krroe.getStatus() );
        }
    }

    @Test
    public void taskIdMinAndTaskIdMaxShouldBehave() {
        // setup
        runStringProcess(ksession, new Random().nextInt(1000));
        runStringProcess(ksession, new Random().nextInt(1000));

        // BZ-1277962: taskId_min and taskId_max behavior was switched
        Response resp = queryResource.queryTasks();
        JaxbQueryTaskResult respResult = (JaxbQueryTaskResult) resp.getEntity();
        List<JaxbQueryTaskInfo> taskInfoList = respResult.getTaskInfoList();
        List<TaskSummary> taskSummaries = new ArrayList<TaskSummary>(taskInfoList.size());
        for( JaxbQueryTaskInfo taskInfo : taskInfoList ) {
           taskSummaries.addAll(taskInfo.getTaskSummaries());
        }

        long minTaskId = Long.MAX_VALUE;
        long maxTaskId = Long.MIN_VALUE;
        for( TaskSummary taskSum : taskSummaries ) {
            long taskId = taskSum.getId();
            if( taskId < minTaskId ) {
                minTaskId = taskId;
            }
            if( taskId > maxTaskId ) {
                maxTaskId = taskId;
            }
        }

        // remove 2 task summaries, to make it interesting
        minTaskId++;
        maxTaskId--;

        addParams(queryParams, "taskId_min", Long.toString(minTaskId));
        addParams(queryParams, "taskId_max", Long.toString(maxTaskId));
        resp = queryResource.queryTasks();
        respResult = (JaxbQueryTaskResult) resp.getEntity();
        for( JaxbQueryTaskInfo taskInfo : respResult.getTaskInfoList() ) {
           for( TaskSummary taskSum : taskInfo.getTaskSummaries() ) {
               long taskId = taskSum.getId();
               assertTrue( "Task id is too large: " + taskId + " > " + maxTaskId, taskId <= maxTaskId );
               assertTrue( "Task id is too small: " + taskId + " < " + minTaskId, taskId >= minTaskId );
           }
        }
    }
}