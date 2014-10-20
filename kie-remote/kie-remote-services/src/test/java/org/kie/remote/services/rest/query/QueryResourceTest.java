package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.query.QueryResourceData.QUERY_PARAM_DATE_FORMAT;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;
import org.kie.test.MyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"null", "unchecked"})
public class QueryResourceTest extends JbpmJUnitBaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(QueryResourceTest.class);
    
    private static final String PROCESS_STRING_VAR_FILE = "BPMN2-HumanTaskWithStringVariables.bpmn2";
    private static final String PROCESS_STRING_VAR_ID = "org.var.human.task.string";
    private static final String PROCESS_OBJ_VAR_FILE = "BPMN2-HumanTaskWithObjectVariables.bpmn2";
    private static final String PROCESS_OBJ_VAR_ID = "org.var.human.task.object";
    private static final String USER_ID = "john";

    private KieSession ksession;
    private TaskService taskService;
    private RuntimeManager runtimeManager;
    private RuntimeEngine engine;

    private QueryResourceImpl queryResource;
    private InternalTaskQueryHelper queryTaskHelper;
    private InternalProcInstQueryHelper queryProcInstHelper;

    private static ObjectMapper jsonMapper = new ObjectMapper();
    private static JaxbSerializationProvider jaxbClientMapper = JaxbSerializationProvider.clientSideInstance();
    private static JaxbSerializationProvider jaxbServerMapper = JaxbSerializationProvider.serverSideInstance();
   
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
    
    private void setupTestData() { 
        if( ! testDataInitialized ) { 
            for( int i = 0; i < 5; ++i ) { 
                runStringProcess(ksession);
                runObjectProcess(ksession, i);
            }
            testDataInitialized = true;
        }
    }
  

    private <T> T roundTripJson(T in) throws Exception { 
        String jsonStr = jsonMapper.writeValueAsString(in);
        logger.debug("\n" + jsonStr);
        return (T) jsonMapper.readValue(jsonStr, in.getClass());
    }
   
    private <T> T roundTripXml(T in) throws Exception { 
        String xmlStr = jaxbServerMapper.serialize(in);
        logger.debug("\n" + xmlStr);
        return (T) jaxbClientMapper.deserialize(xmlStr);
    }
    
    private void runStringProcess(KieSession ksession) { 
        Map<String, Object> params = new HashMap<String, Object>();
        String initValue = UUID.randomUUID().toString();
        params.put("inputStr", initValue );
        params.put("otherStr", initValue ); 
        ProcessInstance processInstance = ksession.startProcess(PROCESS_STRING_VAR_ID, params);
        assertTrue( processInstance != null && processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        long procInstId = processInstance.getId();
        
        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        assertFalse( "No tasks found!", taskIds.isEmpty() );
        long taskId = taskIds.get(0); 
        taskService.start(taskId, USER_ID);
        
        Map<String, Object> taskResults = new HashMap<String, Object>();
        taskResults.put("taskOutputStr", "task-" + procInstId);
        taskService.complete(taskId, USER_ID, taskResults);
    
        assertNull("Process instance has not been finished.", ksession.getProcessInstance(procInstId) );
        
        AuditLogService logService = new JPAAuditLogService(getEmf());
        List<VariableInstanceLog> vils = logService.findVariableInstances(procInstId);
        assertTrue( "No variable instance logs found", vils != null && ! vils.isEmpty() );
        assertTrue( "Too few variable instance logs found", vils.size() > 3 );
    }

    private static Random random = new Random();
    
    private void runObjectProcess(KieSession ksession, int i) { 
        Map<String, Object> params = new HashMap<String, Object>();
        String initValue = "start-" + i;
        params.put("inputStr", new MyType(initValue, random.nextInt()));
        params.put("otherStr", new MyType(initValue, random.nextInt()));
        ProcessInstance processInstance = ksession.startProcess(PROCESS_OBJ_VAR_ID, params);
        assertTrue( processInstance != null && processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        long procInstId = processInstance.getId();
        
        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        assertFalse( "No tasks found!", taskIds.isEmpty() );
        long taskId = taskIds.get(0); 
        taskService.start(taskId, USER_ID);
        
        Map<String, Object> taskResults = new HashMap<String, Object>();
        taskResults.put("taskOutputStr", new MyType("task-" + procInstId, random.nextInt()));
        taskService.complete(taskId, USER_ID, taskResults);
    
        assertNull("Process instance has not been finished.", ksession.getProcessInstance(procInstId) );
        
        AuditLogService logService = new JPAAuditLogService(getEmf());
        List<VariableInstanceLog> vils = logService.findVariableInstances(procInstId);
        assertTrue( "No variable instance logs found", vils != null && ! vils.isEmpty() );
        assertTrue( "Too few variable instance logs found: " + vils.size(), vils.size() >= 3 );
        
        VariableInstanceLog lastVil = null;
        for( VariableInstanceLog vil : vils ) { 
            if( ! vil.getVariableId().equals("inputStr") ) { 
               continue; 
            }
            if( lastVil == null ) { 
                lastVil = vil;
            }
            if( lastVil.getId() < vil.getId() ) { 
                lastVil = vil;
            }
        }
        assertTrue( lastVil.getVariableId() + ": " + lastVil.getValue(), 
                lastVil.getValue().contains("check") || lastVil.getVariableId().equals("otherStr") );
    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void queryTaskRestCallTest() throws Exception  {
        int [] pageInfo = { 0, 10 };
        int maxResults = 1000;
        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        
        // simple (everything) 

        JaxbQueryTaskResult result = queryTaskHelper.queryTasksAndVariables(USER_ID, queryParams, pageInfo, maxResults);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "empty result", result.getTaskInfoList().size() > 2 );
        for( JaxbQueryTaskInfo taskInfo : result.getTaskInfoList() ) { 
           long procInstId = taskInfo.getProcessInstanceId();
           assertEquals( procInstId, taskInfo.getTaskSummaries().get(0).getProcessInstanceId().longValue() );
        }
      
        roundTripJson(result);
        roundTripXml(result);
        
        // complicated 
        long procInstId = result.getTaskInfoList().get(0).getProcessInstanceId();
        long taskId = result.getTaskInfoList().get(0).getTaskSummaries().get(0).getId();
     
        addParams(queryParams, "processinstanceid", procInstId + ""); 
        addParams(queryParams, "processid_re", PROCESS_STRING_VAR_ID.substring(0, 10) + "*");
        addParams(queryParams, "taskid", taskId + "");
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
        
        result = queryTaskHelper.queryTasksAndVariables(USER_ID, queryParams, pageInfo, maxResults);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "more than 1 result", result.getTaskInfoList().size() == 1 );
        JaxbQueryTaskInfo taskInfo = result.getTaskInfoList().get(0);
        assertEquals( "more than 1 task sum", 1, taskInfo.getTaskSummaries().size() );
        assertEquals( "more than 1 variable", 1, taskInfo.getVariables().size() );
        TaskSummary taskSum = taskInfo.getTaskSummaries().get(0);
        JaxbVariableInfo varInfo = taskInfo.getVariables().get(0);
      
        roundTripJson(result);
        roundTripXml(result);
    }

    private static void addParams(Map<String, String[]> params, String name, String... values ) { 
       params.put(name,  values);
    }
    
    @Test
    public void queryProcessRestCallTest() throws Exception  { 
        for( int i = 0; i < 3; ++i ) { 
            runObjectProcess(ksession, i);
        }
        
        int [] pageInfo = { 0, 10 };
        int maxResults = 1000;
        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        
        // simple (everything) 
        JaxbQueryProcessInstanceResult result = queryProcInstHelper.queryProcessInstancesAndVariables(queryParams, pageInfo, maxResults);
        assertNotNull( "null result", result );
        assertFalse( "empty result (all)", result.getProcessInstanceInfoList().isEmpty() );
        assertTrue( "not enough proc info's: " + result.getProcessInstanceInfoList().size(), result.getProcessInstanceInfoList().size() > 2 );
        long procInstMin = Long.MAX_VALUE;
        long procInstMax = -1l;
        for( JaxbQueryProcessInstanceInfo procInfo : result.getProcessInstanceInfoList() ) { 
           long procInstId = procInfo.getProcessInstance().getId();
           if( procInstId > procInstMax ) { 
               procInstMax = procInstId;
           }
           if( procInstId < procInstMin ) { 
               procInstMin = procInstId;
           }
           String procId = procInfo.getProcessInstance().getProcessId();
           boolean myType = procId.contains("object");
           for( JaxbVariableInfo varInfo : procInfo.getVariables() ) { 
               String varVal = (String) varInfo.getValue();
               String varName = varInfo.getName();
               assertTrue( procId + ": var value [" + varVal + "]", varVal.contains("{") || ! myType );
               assertTrue( procInstId + ": var value [" + varVal + "]", varVal.contains("check") || ! varName.equals("inputStr"));
           }
        }
       
        roundTripJson(result);
        roundTripXml(result);
       
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
        
        result = queryProcInstHelper.queryProcessInstancesAndVariables(queryParams, pageInfo, maxResults);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getProcessInstanceInfoList().isEmpty() );
        assertTrue( "more than 1 result", result.getProcessInstanceInfoList().size() == 1 );
        JaxbQueryProcessInstanceInfo procInfo = result.getProcessInstanceInfoList().get(0);
        assertNotNull( "no proc instance", procInfo.getProcessInstance() );
        assertEquals( "num variables", 1, procInfo.getVariables().size() );
      
        roundTripJson(result);
        roundTripXml(result);
        
        queryParams.clear();
        --procInstMax;
        ++procInstMin;
        addParams(queryParams, "piid_min", String.valueOf(procInstMin));
        addParams(queryParams, "processinstanceid_max", String.valueOf(procInstMax));
        result = queryProcInstHelper.queryProcessInstancesAndVariables(queryParams, pageInfo, maxResults);
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
    }
}
