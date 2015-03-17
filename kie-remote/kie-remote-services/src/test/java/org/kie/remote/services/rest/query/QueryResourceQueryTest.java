package org.kie.remote.services.rest.query;

import static org.kie.remote.services.rest.query.QueryResourceData.QUERY_PARAM_DATE_FORMAT;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.services.task.commands.TaskQueryDataCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.ParametrizedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryResourceQueryTest extends AbstractQueryResourceTest {

    @Before
    public void init() {
        runtimeManager = createRuntimeManager(PROCESS_STRING_VAR_FILE);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();
       
        addObjectProcessInstances = false;
        setupTestData();
    }
    
    @After
    public void cleanup() {
        if( runtimeManager != null ) { 
            runtimeManager.disposeRuntimeEngine(engine);
            runtimeManager.close();
        }
    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void queryModificationServiceTest() throws Exception {
        RemoteServicesQueryCommandBuilder taskQueryBuilder = new RemoteServicesQueryCommandBuilder(USER_ID);
        
        TaskQueryDataCommand cmd = taskQueryBuilder.createTaskQueryDataCommand();  
        List<TaskSummary> taskResult = taskService.execute(cmd);
        assertNotNull( "Null taskResult!", taskResult );
        assertFalse( "No task summaries found.", taskResult.isEmpty() );
    
        TaskSummary taskSum = taskResult.get(0);
        String deploymentId = taskSum.getDeploymentId();
        long procInstId = taskSum.getProcessInstanceId();
       
        Calendar cal = GregorianCalendar.getInstance();
        cal.roll(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();
        cal.roll(Calendar.DAY_OF_YEAR, -2);
        Date yesterday = cal.getTime();
 
        AuditLogService auditLogService = new JPAAuditLogService(getEmf());
        
        logger.debug( "tomorrow: " + QUERY_PARAM_DATE_FORMAT.format(tomorrow) );
        logger.debug( "yesterday: " + QUERY_PARAM_DATE_FORMAT.format(yesterday) );
        taskQueryBuilder.clear();
        taskQueryBuilder
        .deploymentId(deploymentId)
        .startDateMin(yesterday)
        .endDateMax(tomorrow)
        .processInstanceId(procInstId)
        .processInstanceStatus(ProcessInstance.STATE_COMPLETED)
        .processId(PROCESS_STRING_VAR_ID)
        .taskId(taskSum.getId())
        .taskStatus(Status.Completed)
        .value("check-" + procInstId)
        .variableId("inputStr");
        
        taskResult = taskService.execute(taskQueryBuilder.createTaskQueryDataCommand());
        assertNotNull( "Null taskResult!", taskResult );
        assertFalse( "No task summaries found.", taskResult.isEmpty() );
        assertEquals( "Num task summaries found.", 1, taskResult.size() );
        assertEquals( taskSum.getId(), taskResult.get(0).getId() );
        
        RemoteServicesQueryCommandBuilder varLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
        
        varLogQueryBuilder
        .deploymentId(deploymentId)
        .startDateMin(yesterday)
        .endDateMax(tomorrow)
        .processInstanceId(procInstId)
        .processInstanceStatus(ProcessInstance.STATE_COMPLETED)
        .processId(PROCESS_STRING_VAR_ID)
        .taskId(taskSum.getId())
        .taskStatus(Status.Completed)
        .like().value("*-" + procInstId)
        .variableId("input*");

        List<org.kie.api.runtime.manager.audit.VariableInstanceLog> varResult 
            = ((AuditLogService) engine.getAuditService()).queryVariableInstanceLogs(varLogQueryBuilder.getQueryData());

        assertNotNull( "Null var Result!", varResult );
        assertFalse( "No var logs found.", varResult.isEmpty() );

        for( org.kie.api.runtime.manager.audit.VariableInstanceLog log : varResult ) { 
            assertEquals( "deployment id", deploymentId, log.getExternalId() );
            assertTrue( "incorrect start date", log.getDate().after(yesterday) );
            assertTrue( "incorrect end date", log.getDate().before(tomorrow) );
            assertEquals( "process instance id", procInstId, log.getProcessInstanceId().longValue() );
            assertEquals( "process id", PROCESS_STRING_VAR_ID, log.getProcessId() );
            assertTrue( "Incorrect var name: " + log.getVariableId(), log.getVariableId().startsWith("input") );
            assertTrue( "Incorrect var value: " + log.getValue(), log.getValue().endsWith("-" + procInstId) );
        }
        
        RemoteServicesQueryCommandBuilder procLogQueryBuilder = new RemoteServicesQueryCommandBuilder();
       
        ParametrizedQuery<ProcessInstanceLog> procQuery = auditLogService.processInstanceLogQuery().processInstanceId(procInstId).buildQuery();
        List<ProcessInstanceLog> procLogs = procQuery.getResultList();
        assertFalse( "No proc logs?!?", procLogs.isEmpty() );
       
        ParametrizedQuery<org.kie.api.runtime.manager.audit.VariableInstanceLog> varQuery 
        = auditLogService.variableInstanceLogQuery()
        .intersect()
        .processInstanceId(procInstId)
        .last()
        .like().value("*-" + procInstId)
        .variableId("input*")
        .buildQuery();
        
        List<org.kie.api.runtime.manager.audit.VariableInstanceLog> varLogs = varQuery.getResultList();
        assertFalse( "No last var logs?!?", varLogs.isEmpty() );
    
        procLogQueryBuilder
        .taskId(taskSum.getId())
        .taskStatus(Status.Completed)
        .like().value("*-" + procInstId)
        .variableId("input*");
        
        List<org.kie.api.runtime.manager.audit.ProcessInstanceLog> procResult 
            = ((AuditLogService) engine.getAuditService()).queryProcessInstanceLogs(procLogQueryBuilder.getQueryData());
        
        assertNotNull( "Null proc Result!", procResult );
        assertFalse( "No proc logs found.", procResult.isEmpty() );
        assertEquals( "Num proc logs found.", 1, procResult.size() );
        org.kie.api.runtime.manager.audit.ProcessInstanceLog procLog = procResult.get(0);
        assertEquals( "Incorrect proc inst id: " + procLog.getProcessInstanceId(), procInstId, procLog.getProcessInstanceId().longValue() );
        assertEquals( "Incorrect external id: " + procLog.getExternalId(), deploymentId, procLog.getExternalId() );
      
        // variable value
        org.kie.api.runtime.manager.audit.VariableInstanceLog varLog = varLogs.get(0);
        varQuery = auditLogService.variableInstanceLogQuery()
        .intersect()
        .processInstanceId(procInstId)
        .last()
        .variableValue(varLog.getVariableId(), varLog.getValue())
        .buildQuery();
        
        varLogs = varQuery.getResultList();
        assertFalse( "No last var logs?!?", varLogs.isEmpty() );
        assertEquals( "Num varlogs", 1, varLogs.size());
        assertEquals( "Num varlogs", varLog, varLogs.get(0) );
   
        // variable value regex
        varQuery = auditLogService.variableInstanceLogQuery()
        .intersect()
        .processInstanceId(procInstId)
        .like()
        .variableValue(varLog.getVariableId(), "*" + varLog.getValue().substring(3))
        .buildQuery();
        
        varLogs = varQuery.getResultList();
        assertFalse( "No last var logs?!?", varLogs.isEmpty() );
        assertEquals( "Num varlogs", 1, varLogs.size());
        assertEquals( "Num varlogs", varLog, varLogs.get(0) );
    }

}
