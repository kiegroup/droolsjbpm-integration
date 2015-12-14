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

import static org.kie.remote.services.rest.query.data.QueryResourceData.QUERY_PARAM_DATE_FORMAT;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.services.api.DeploymentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbTaskSummaryListResponse;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.remote.services.rest.query.helpers.InternalProcInstQueryHelper;
import org.kie.remote.services.rest.query.helpers.InternalTaskQueryHelper;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;

@SuppressWarnings({"null"})
public class QueryResourceTest extends AbstractQueryResourceTest {

    private QueryResourceImpl queryResource;
    private InternalTaskQueryHelper queryTaskHelper;
    private InternalProcInstQueryHelper queryProcInstHelper;


    public QueryResourceTest() {
        super();
        jsonMapper.enable(Feature.INDENT_OUTPUT);
        jaxbClientMapper.setPrettyPrint(true);
        jaxbServerMapper.setPrettyPrint(true);
    }

    private int numProcesses = 6;

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_STRING_VAR_FILE, PROCESS_OBJ_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();

        queryResource = new QueryResourceImpl();

        IdentityProvider mockIdProvider = mock(IdentityProvider.class);
        when(mockIdProvider.getName()).thenReturn(USER_ID);
        queryResource.setIdentityProvider(mockIdProvider);

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

        queryResource.setUserGroupCallback(userGroupCallback);

        queryTaskHelper = new InternalTaskQueryHelper(queryResource);
        queryProcInstHelper = new InternalProcInstQueryHelper(queryResource);

        setupTestData();
    }

    @After
    public void cleanup() {
        if( runtimeManager != null ) {
            runtimeManager.disposeRuntimeEngine(engine);
            runtimeManager.close();
        }
    }

    private static boolean testDataInitialized = false;

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void simpleQueryTaskRestCallTest() throws Exception  {
        int [] pageInfo = { 0, 0 };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        // simple (everything)
        JaxbQueryTaskResult result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "empty result", result.getTaskInfoList().size() > 2 );
        assertTrue( "pagination should not have happened: " + result.getTaskInfoList().size(), result.getTaskInfoList().size() >= 20 );

        for( JaxbQueryTaskInfo taskInfo : result.getTaskInfoList() ) {
           long procInstId = taskInfo.getProcessInstanceId();
           assertEquals( procInstId, taskInfo.getTaskSummaries().get(0).getProcessInstanceId().longValue() );
        }

        roundTripJson(result);
        roundTripXml(result);

        // complicated
        String varVal = null;
        String varName = null;
        Date varDate = null;
        for( JaxbVariableInfo varInfo : result.getTaskInfoList().get(0).getVariables() ) {
           if( varDate == null ) {
              varVal = varInfo.getValue().toString();
              varName = varInfo.getName();
              varDate = varInfo.getModificationDate();
           } else if( varInfo.getModificationDate().after(varDate) && varInfo.getName().equals(varName) ) {
               varVal = varInfo.getValue().toString();
           }
        }
        addParams(queryParams, "var_" + varName, varVal);

        result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);

        // typo test
        queryParams.clear();
        addParams(queryParams, "potentialowner" , "anton");

        result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
    }

    @Test
    public void queryTaskRestCallTest() throws Exception  {
        int [] pageInfo = { 0, 0 };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        // simple (everything)
        JaxbQueryTaskResult result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "empty result", result.getTaskInfoList().size() > 2 );
        assertTrue( "pagination should not have happened: " + result.getTaskInfoList().size(), result.getTaskInfoList().size() >= 20 );

        for( JaxbQueryTaskInfo taskInfo : result.getTaskInfoList() ) {
           long procInstId = taskInfo.getProcessInstanceId();
           assertEquals( procInstId, taskInfo.getTaskSummaries().get(0).getProcessInstanceId().longValue() );
        }

        roundTripJson(result);
        roundTripXml(result);

        // pagination
        pageInfo[1] = 3; // page size
        result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
        assertTrue( "Expected a page of size " + pageInfo[1] + ", not "  + result.getTaskInfoList().size(), result.getTaskInfoList().size() == pageInfo[1] );

        pageInfo[0] = 2; // page number 2
        JaxbQueryTaskResult otherResult = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
        Set<Long> origResultSet = new HashSet<Long>();
        long lastProcInstId = -1;
        for( JaxbQueryTaskInfo taskInfo : result.getTaskInfoList() ) {
            assertTrue( "Not in process instance id order:" + lastProcInstId + " !< "  + taskInfo.getProcessInstanceId(), lastProcInstId < taskInfo.getProcessInstanceId() );
            lastProcInstId = taskInfo.getProcessInstanceId();
            origResultSet.add(taskInfo.getProcessInstanceId());
        }
        Set<Long> newResultSet = new HashSet<Long>();
        for( JaxbQueryTaskInfo taskInfo : otherResult.getTaskInfoList() ) {
            newResultSet.add(taskInfo.getProcessInstanceId());
        }
        for( JaxbQueryTaskInfo taskInfo : otherResult.getTaskInfoList() ) {
            long procInstId = taskInfo.getProcessInstanceId();
            assertFalse( "Original results should not be available in new results: " + procInstId,
                   origResultSet.contains(procInstId) );
        }
        pageInfo[0] = pageInfo[1] = 0; // reset page info for other tests

        // complicated
        long procInstId = result.getTaskInfoList().get(0).getProcessInstanceId();
        long taskId = result.getTaskInfoList().get(0).getTaskSummaries().get(0).getId();

        addParams(queryParams, "processinstanceid", procInstId + "");
        addParams(queryParams, "processid_re", PROCESS_STRING_VAR_ID.substring(0, 10) + "*");
        addParams(queryParams, "taskid_min", taskId + "");
        addParams(queryParams, "taskid_max", taskId + "");
        addParams(queryParams, "taskowner", USER_ID );
        addParams(queryParams, "tst", "Completed" );

        Calendar cal = GregorianCalendar.getInstance();
        cal.roll(Calendar.DAY_OF_YEAR, +2);
        String tomorowStr = QUERY_PARAM_DATE_FORMAT.format(cal.getTime()).substring(0, 8);
        addParams(queryParams, "edt_max", tomorowStr);
        String yesterdayStr = "00:00:01";
        addParams(queryParams, "startdate_min", yesterdayStr);
        addParams(queryParams, "var_inputStr", "check-1");
        addParams(queryParams, "vv_re", "check*");

        result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "more than 1 result", result.getTaskInfoList().size() == 1 );
        JaxbQueryTaskInfo taskInfo = result.getTaskInfoList().get(0);
        assertEquals( "more than 1 task sum", 1, taskInfo.getTaskSummaries().size() );
        assertEquals( "more than 1 variable", 1, taskInfo.getVariables().size() );
        TaskSummary taskSum = taskInfo.getTaskSummaries().get(0);
        assertNotNull( taskSum );
        assertEquals( taskId, taskSum.getId().longValue() );
        JaxbVariableInfo varInfo = taskInfo.getVariables().get(0);
        assertEquals( "inputStr", varInfo.getName() );

        roundTripJson(result);
        roundTripXml(result);
    }

    @Test
    public void queryProcessRestCallTest() throws Exception  {
        int pageSize = 5;
        assertTrue( numProcesses > pageSize );
        int [] pageInfo = { 0, pageSize };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        // simple (everything)
        JaxbQueryProcessInstanceResult result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "Null result", result );
        assertFalse( "Empty result (all)", result.getProcessInstanceInfoList().isEmpty() );
        assertTrue( "Not enough proc info's: " + result.getProcessInstanceInfoList().size(), result.getProcessInstanceInfoList().size() > 2 );
        assertEquals( "Max results", result.getProcessInstanceInfoList().size() , pageSize );

        long procInstMin = Long.MAX_VALUE;
        long procInstMax = -1l;
        for( JaxbQueryProcessInstanceInfo procInfo : result.getProcessInstanceInfoList() ) {
           long procInstId = procInfo.getProcessInstance().getId();
           // ordered by proc inst id
           assertTrue( procInstId + " ! >= max " + procInstMax, procInstId >= procInstMax );
           procInstMax = procInstId;
           if( procInstId < procInstMin ) {
               procInstMin = procInstId;
           }

           String procId = procInfo.getProcessInstance().getProcessId();
           boolean myType = procId.contains("object");
           for( JaxbVariableInfo varInfo : procInfo.getVariables() ) {
               String varVal = (String) varInfo.getValue();
               String varName = varInfo.getName();
               // test object
               assertTrue( procId + ": var value [" + varVal + "]", varVal.contains("{") || ! myType );
               assertTrue( procInstId + ": var [" + varVal + "]", varVal.contains("check") || ! varName.equals("inputStr") );
           }
        }

        roundTripJson(result);
        roundTripXml(result);

        // pagination
        pageSize = 2;
        int pageNum = 2;
        pageInfo[0] = pageNum; // offset = ((pageNum-1)*pageSize)-1
        int offset = (pageNum-1)*pageSize;
        pageInfo[1] = pageSize; // max results
        JaxbQueryProcessInstanceResult newResult = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "null result", newResult );
        assertFalse( "empty result (all)", newResult.getProcessInstanceInfoList().isEmpty() );
        assertTrue( "Expected max results of " + pageSize + ", not "+ newResult.getProcessInstanceInfoList().size(),
                newResult.getProcessInstanceInfoList().size() == pageSize );
        assertEquals( "Expected offset of " + offset + " | ",
                result.getProcessInstanceInfoList().get(offset).getProcessInstance().getId(),
                newResult.getProcessInstanceInfoList().get(0).getProcessInstance().getId() );
        pageInfo[0] = pageInfo[1] = 0;

        // complicated
        Long procInstId = null;
        String varName = null;
        String varVal = null;
        for( JaxbQueryProcessInstanceInfo procInfo : result.getProcessInstanceInfoList() ) {
            if( procInfo.getProcessInstance().getProcessId().equals(PROCESS_OBJ_VAR_ID) ) {
                procInstId = procInfo.getProcessInstance().getId();
                JaxbVariableInfo varInfo = procInfo.getVariables().get(0);
                if( ! varInfo.getName().equals("inputStr") ) {
                   varInfo = procInfo.getVariables().get(1);
                }
                varName = varInfo.getName();
                varVal = (String) varInfo.getValue();
                break;
            }
        }
        assertNotNull( "proc inst id", procInstId );
        assertNotNull( "var id", varName );
        assertNotNull( "var value ", varVal );

        addParams(queryParams, "processinstanceid", procInstId + "");
        addParams(queryParams, "processid_re", "*" + PROCESS_OBJ_VAR_ID.substring(10));

        Calendar cal = GregorianCalendar.getInstance();
        cal.roll(Calendar.DAY_OF_YEAR, +2);
        String tomorowStr = QUERY_PARAM_DATE_FORMAT.format(cal.getTime()).substring(0, 8);
        addParams(queryParams, "enddate_max", tomorowStr);
        String yesterdayStr = "00:00:01";
        addParams(queryParams, "stdt_min", yesterdayStr);
        addParams(queryParams, "var_" + varName, varVal );

        result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getProcessInstanceInfoList().isEmpty() );
        assertTrue( "more than 1 result", result.getProcessInstanceInfoList().size() == 1 );
        JaxbQueryProcessInstanceInfo procInfo = result.getProcessInstanceInfoList().get(0);
        assertNotNull( "no proc instance", procInfo.getProcessInstance() );
        assertEquals( "num variables", 1, procInfo.getVariables().size() );

        roundTripJson(result);
        roundTripXml(result);

        // more complicated
        queryParams.clear();
        --procInstMax;
        ++procInstMin;
        addParams(queryParams, "piid_min", String.valueOf(procInstMin));
        addParams(queryParams, "processinstanceid_max", String.valueOf(procInstMax));
        result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getProcessInstanceInfoList().isEmpty() );
        assertEquals( "number results", procInstMax - procInstMin+1, result.getProcessInstanceInfoList().size() );
        long findMin = Long.MAX_VALUE;
        long findMax = -1;
        for( JaxbQueryProcessInstanceInfo jaxbProcInfo : result.getProcessInstanceInfoList() ) {
           procInstId = jaxbProcInfo.getProcessInstance().getId();
           if( procInstId > findMax ) {
               findMax = procInstId;
           }
           if( procInstId < findMin ) {
               findMin = procInstId;
           }
        }
        assertEquals( "process instance id max", procInstMax, findMax );
        assertEquals( "process instance id min", procInstMin, findMin );

        // test bad parameter
        addParams(queryParams, "taskowner", USER_ID );
        boolean exThrown = false;
        try {
            result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        } catch( Exception e ) {
           exThrown = true;
        }
        assertTrue( "Exception not thrown on invalid parameter 'taskowner'", exThrown);


        // varregex test
        queryParams.clear();
        addParams(queryParams, "processinstanceid", procInstId + "");
        addParams(queryParams, "processid_re", "*" + PROCESS_OBJ_VAR_ID.substring(10));

        addParams(queryParams, "vr_" + varName, "X" + varVal.substring(0, 3) + "*" );

        result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "null result", result );
        assertTrue( "Expected empty result: " + result.getProcessInstanceInfoList().size(), result.getProcessInstanceInfoList().isEmpty() );
    }

    @Test
    public void duplicateTaskSummaryResultsTest() {
        int [] pageInfo = { 0, 0 };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        JaxbQueryTaskResult result = queryTaskHelper.queryTaskOrProcInstAndAssociatedVariables(USER_ID, queryParams, pageInfo);
        assertNotNull( "Null result", result );
        assertFalse( "Empty result (all)", result.getTaskInfoList().isEmpty() );

        long processInstanceId = 0;
        for( JaxbQueryTaskInfo queryInfo : result.getTaskInfoList() ) {
           assertNotNull( "Null task summaries info!", queryInfo.getTaskSummaries() );
           assertFalse( "No task summaries info!", queryInfo.getTaskSummaries().isEmpty() );
           processInstanceId = queryInfo.getTaskSummaries().get(0).getProcessInstanceId();
        }

        addParams(queryParams, "processInstanceId", "" + processInstanceId);
    }

    @Test
    public void testTaskSummary() {
        Map<String, Object> processParams = new HashMap<String, Object>();
        String initValue = UUID.randomUUID().toString();
        processParams.put("inputStr", "proc-" + numTestProcesses + "-" + initValue );
        processParams.put("otherStr", "proc-" + numTestProcesses + "-" + initValue );
        processParams.put("secondStr", numTestProcesses + "-second-" + random.nextInt(Integer.MAX_VALUE));
        org.kie.api.runtime.process.ProcessInstance processInstance = ksession.startProcess(PROCESS_STRING_VAR_ID, processParams);
        assertTrue( processInstance != null && processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        long procInstId = processInstance.getId();

        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        assertFalse( "No tasks found!", taskIds.isEmpty() );
        long taskId = taskIds.get(0);
        taskService.start(taskId, USER_ID);

        Map<String, String[]> queryParams  = new HashMap<String, String[]>();
        addParams(queryParams, "processInstanceId", String.valueOf(procInstId));
        JaxbTaskSummaryListResponse resp = queryResource.doTaskSummaryQuery(queryParams, "/rest/task/query");

        List<TaskSummary> taskSumList = resp.getResult();
        assertNotNull( "Null task summary list", taskSumList );
        assertFalse( "Empty task summary list", taskSumList.isEmpty() );
        for( TaskSummary taskSum : taskSumList ) {
            assertEquals( "Incorrect process instance id on task summary", procInstId, taskSum.getProcessInstanceId().longValue() );
        }

        Task task = taskService.getTaskById(taskSumList.get(0).getId());
        assertNotNull( "Null task returned", task );
        assertNotNull( "Null actual owner on task " + task.getId() + " [status: " + task.getTaskData().getStatus().toString() + "]",
                task.getTaskData().getActualOwner() );
        String actualOwnerId = task.getTaskData().getActualOwner().getId();
        assertEquals( "Incorrect status for task " + task.getId(), Status.InProgress, task.getTaskData().getStatus() );

        for( TaskSummary taskSum : taskSumList ) {
            assertNotNull( "Null actual owner in task summary " + taskSum.getId() + " [status: " + taskSum.getStatus() + "]" ,
                    taskSum.getActualOwner() );
            assertNotNull( "Null actual owner id in task summary " + taskSum.getId() + " [status: " + taskSum.getStatus() + "]",
                    taskSum.getActualOwner().getId() );
            assertEquals( "Incorrect actual owner on task summary" , actualOwnerId, taskSum.getActualOwnerId() );
        }
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH:mm:ss.SSS");

    @Test
    public void startDateTest() {
        int [] pageInfo = { 0, 0 };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();

        JaxbQueryProcessInstanceResult result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "Null result", result );
        assertFalse( "Empty result (all)", result.getProcessInstanceInfoList().isEmpty() );

        JaxbQueryProcessInstanceInfo procInfo = result.getProcessInstanceInfoList().get(0);
        long procInstId = procInfo.getProcessInstance().getId();

        JPAAuditLogService auditService = new JPAAuditLogService(getEmf());
        ProcessInstanceLog log = auditService.findProcessInstance(procInstId);
        List<org.kie.api.runtime.manager.audit.ProcessInstanceLog> logs
            = auditService.processInstanceLogQuery().startDate(log.getStart()).build().getResultList();
        assertNotNull( "Null List of ProcessInstanceLog", logs );
        assertFalse( "Empty List of ProcessInstanceLog", logs.isEmpty() );
        assertEquals( "List of ProcessInstanceLog", 1, logs.size() );
        assertEquals( "ProcessInstanceLog retrieved by start date", log.getStart(), logs.get(0).getStart() );

        Date startDate = log.getStart();

        String startDateStr = sdf.format(startDate);
        addParams(queryParams, "startdate", startDateStr);
        result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);

        assertNotNull( "Null result", result );
        assertFalse( "Empty result (all)", result.getProcessInstanceInfoList().isEmpty() );
        assertEquals( "Process instance info list", 1, result.getProcessInstanceInfoList().size() );

        procInfo = result.getProcessInstanceInfoList().get(0);
        assertEquals( "Process instance id", procInstId, procInfo.getProcessInstance().getId() );
    }

    @Test
    public void twoVariableTest() throws Exception  {
        int pageSize = 5;
        assertTrue( numProcesses > pageSize );
        int [] pageInfo = { 0, pageSize };
        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        addParams(queryParams, "processinstancestatus", "" + ProcessInstance.STATE_COMPLETED );

        // simple (everything)
        JaxbQueryProcessInstanceResult result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertTrue( "Empty result (status complete)", result != null && result.getProcessInstanceInfoList() != null && ! result.getProcessInstanceInfoList().isEmpty() );
        int origNumResults = result.getProcessInstanceInfoList().size();

        JaxbQueryProcessInstanceInfo foundProcInfo = null;
        for( JaxbQueryProcessInstanceInfo queryProcInfo : result.getProcessInstanceInfoList() ) {
            assertEquals( "Incorrect process instance state!", ProcessInstance.STATE_COMPLETED, queryProcInfo.getProcessInstance().getState() );
            for( JaxbVariableInfo varInfo : queryProcInfo.getVariables() ) {
                if( varInfo.getName().equals("secondStr") && foundProcInfo == null) {
                   foundProcInfo = queryProcInfo;
                }
            }
        }
        assertNotNull( "Could not find process instance!" , foundProcInfo );

        Map<String, String> varValueMap = new HashMap<String, String>();
        int numVars = 0;
        for( JaxbVariableInfo varInfo : foundProcInfo.getVariables() ) {
            String name = varInfo.getName();
            String value = varInfo.getValue().toString();
            varValueMap.put(name, value);
            addParams(queryParams, "var_" + name, value );
            ++numVars;
        }

        result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertFalse( "Null result ('COMPLETE' + var1 OR var2 OR var3)", result == null || result.getProcessInstanceInfoList() == null );
        assertFalse( "Empty result ('COMPLETE' + var1 OR var2 OR var3)", result.getProcessInstanceInfoList().isEmpty() );

        for( JaxbQueryProcessInstanceInfo procInstInfo : result.getProcessInstanceInfoList() ) {
            int variablesFound = 0;
            for( JaxbVariableInfo varInfo : procInstInfo.getVariables() ) {
                String value = varValueMap.get(varInfo.getName());
                if( varInfo.getValue().toString().equals(value) ) {
                    ++variablesFound;
                }
            }
            assertEquals( "Variables found", numVars, variablesFound );
        }
        assertEquals( "Incorrect num results", 1, result.getProcessInstanceInfoList().size() );

        // test OR functionality (listing var_ parameters A and B gets results that reference var A OR var B)
        queryParams.clear();
        addParams(queryParams, "varregex_inputStr", "*check*");
        addParams(queryParams, "vr_secondStr", "*-second-*");
        addParams(queryParams, "processinstancestatus", "" + ProcessInstance.STATE_COMPLETED );

        result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertFalse( "Null result ('COMPLETE' + var1 OR var2)", result == null || result.getProcessInstanceInfoList() == null );
        assertFalse( "Empty result ('COMPLETE' + var1 OR var2)", result.getProcessInstanceInfoList().isEmpty() );
        assertEquals( "Num results ('COMPLETE' + var1 OR var2)", origNumResults, result.getProcessInstanceInfoList().size() );
    }

}
