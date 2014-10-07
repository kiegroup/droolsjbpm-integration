package org.kie.remote.services.rest;


import static org.mockito.Mockito.*;
import static org.kie.remote.services.rest.query.QueryResourceData.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.CommandBasedAuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.commands.TaskQueryDataCommand;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.ParametrizedQuery;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.DeployResourceBase;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryTaskResult;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbVariableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryResourceTest extends JbpmJUnitBaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(DeployResourceBase.class);
    
    private static final String PROCESS_FILE = "BPMN2-HumanTaskWithVariables.bpmn2";
    private static final String PROCESS_ID = "human-task-with-procvars";
    private static final String USER_ID = "john";

    private KieSession ksession;
    private TaskService taskService;
    private RuntimeManager runtimeManager;
    private RuntimeEngine engine;

    private QueryResourceImpl queryResource;

    public QueryResourceTest() {
        super(true, true, "org.jbpm.domain");
    }

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_FILE);
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
                runProcess(ksession);
            }
            testDataInitialized = true;
        }
    }
    
    private void runProcess(KieSession ksession) { 
        Map<String, Object> params = new HashMap<String, Object>();
        String initValue = UUID.randomUUID().toString();
        params.put("inputStr", initValue );
        params.put("otherStr", initValue ); 
        ProcessInstance processInstance = ksession.startProcess(PROCESS_ID, params);
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

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void queryRestCallTest() throws Exception  {
        int [] pageInfo = { 0, 10 };
        int maxResults = 1000;
        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        
        // simple (everything) 
        JaxbQueryTaskResult result = queryResource.queryTasksAndVariables(queryParams, pageInfo, maxResults);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "empty result", result.getTaskInfoList().size() > 2 );
        for( JaxbQueryTaskInfo taskInfo : result.getTaskInfoList() ) { 
           long procInstId = taskInfo.getProcessInstanceId();
           assertEquals( procInstId, taskInfo.getTaskSummaries().get(0).getProcessInstanceId().longValue() );
        }
       
        // complicated 
        long procInstId = result.getTaskInfoList().get(0).getProcessInstanceId();
        long taskId = result.getTaskInfoList().get(0).getTaskSummaries().get(0).getId();
     
        addParams(queryParams, "processinstanceid", procInstId + ""); 
        addParams(queryParams, "processid_re", PROCESS_ID.substring(0, 10) + "*");
        addParams(queryParams, "taskid", taskId + "");
        addParams(queryParams, "taskowner", USER_ID );
        addParams(queryParams, "tst", "Completed" );
       
        Calendar cal = GregorianCalendar.getInstance();
        cal.roll(Calendar.DATE, +2);
        String tomorowStr = QUERY_PARAM_DATE_FORMAT.format(cal.getTime()).substring(0, 8);
        addParams(queryParams, "edt_max", tomorowStr);
        String yesterdayStr = "00:00:01";
        addParams(queryParams, "startdate_min", yesterdayStr);
        addParams(queryParams, "vid", "inputStr");
        addParams(queryParams, "vv_re", "check*");
        
        result = queryResource.queryTasksAndVariables(queryParams, pageInfo, maxResults);
        assertNotNull( "null result", result );
        assertFalse( "empty result", result.getTaskInfoList().isEmpty() );
        assertTrue( "more than 1 result", result.getTaskInfoList().size() == 1 );
        JaxbQueryTaskInfo taskInfo = result.getTaskInfoList().get(0);
        assertEquals( "more than 1 task sum", 1, taskInfo.getTaskSummaries().size() );
        assertEquals( "more than 1 variable", 1, taskInfo.getVariables().size() );
        TaskSummary taskSum = taskInfo.getTaskSummaries().get(0);
        JaxbVariableInfo varInfo = taskInfo.getVariables().get(0);
    }

    private static void addParams(Map<String, String[]> params, String name, String... values ) { 
       params.put(name,  values);
    }
}
