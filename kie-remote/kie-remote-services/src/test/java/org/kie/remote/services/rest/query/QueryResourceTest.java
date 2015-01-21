package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.query.QueryResourceData.QUERY_PARAM_DATE_FORMAT;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.services.api.DeploymentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.QueryResourceImpl;
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
        super(true, true, "org.jbpm.domain");
        jsonMapper.enable(Feature.INDENT_OUTPUT);
        jaxbClientMapper.setPrettyPrint(true);
        jaxbServerMapper.setPrettyPrint(true);
    }

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_STRING_VAR_FILE, PROCESS_OBJ_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();
        
        setupTestData();
        
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

    private static boolean testDataInitialized = false;
   
    // must be at least 5
    private static int numTestProcesses = 10;
    
    private void setupTestData() { 
        if( ! testDataInitialized ) { 
            for( int i = 0; i < numTestProcesses; ++i ) { 
                runStringProcess(ksession);
                runObjectProcess(ksession, i);
            }
            testDataInitialized = true;
        }
    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

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
        addParams(queryParams, "vid", "inputStr");
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

    private static void addParams(Map<String, String[]> params, String name, String... values ) { 
       params.put(name,  values);
    }
   
    @Test
    public void queryProcessRestCallTest() throws Exception  { 
        int numProcesses = 6;
        for( int i = 0; i < numProcesses; ++i ) { 
            runObjectProcess(ksession, i);
        }
      
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
}
