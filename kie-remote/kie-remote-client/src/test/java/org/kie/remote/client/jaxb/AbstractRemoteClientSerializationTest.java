package org.kie.remote.client.jaxb;

import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_INSTANCE_ID_LIST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.jaxb.ComparePair;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRemoteClientSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractRemoteClientSerializationTest.class); 

    public abstract <T> T testRoundTrip(T in) throws Exception;
    
    @Test
    public void taskSummaryListTest() throws Exception {
        this.setupDataSource = true;
        this.sessionPersistence = true;
        super.setUp();
        
        RuntimeManager runtimeManager = createRuntimeManager(Strategy.SINGLETON, "test", "BPMN2-HumanTaskWithTaskContent.bpmn2");
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
        
        ProcessInstance procInst = ksession.startProcess("org.kie.remote.test.usertask.UserTask");
        long procInstId = procInst.getId();

        List<Long> statuses = new ArrayList<Long>();
        statuses.add(procInstId);
        
        List<Long> taskIds = ((InternalTaskService)taskService).getTasksByProcessInstanceId(procInstId);
        assertEquals( "Task list size", 1, taskIds.size());
        Task task = ((InternalTaskService)taskService).getTaskById(taskIds.get(0));
        assertNotNull( "No people assignments!", task.getPeopleAssignments() );
        assertNotNull( "No business adminstrators!", task.getPeopleAssignments().getBusinessAdministrators() );
        assertFalse( "Empty business adminstrators!", task.getPeopleAssignments().getBusinessAdministrators().isEmpty() );
        String busAdmin = task.getPeopleAssignments().getBusinessAdministrators().get(0).getId();
        
        Map<String, List<?>> fieldVals = new HashMap<String, List<?>>();
        fieldVals.put(PROCESS_INSTANCE_ID_LIST, statuses);
        List<org.kie.api.task.model.TaskSummary> taskSumList = ((InternalTaskService)taskService).getTasksByVariousFields(busAdmin, fieldVals, true);
        assertEquals( "Task summary list size", 1, taskSumList.size());
        TaskSummaryImpl taskSumImpl = (TaskSummaryImpl) taskSumList.get(0);
        taskSumImpl.setActualOwner(new UserImpl("Minnie"));
        taskSumImpl.setCreatedBy(new UserImpl("Mickey"));
       
        List<JaxbTaskSummary> jaxbTaskSumList = new ArrayList<JaxbTaskSummary>();
        Iterator<TaskSummary> iter = taskSumList.iterator();
        while( iter.hasNext() ) { 
            jaxbTaskSumList.add(new JaxbTaskSummary(iter.next()));
        }
        JaxbTaskSummaryListResponse jaxbTaskSumListResp = new JaxbTaskSummaryListResponse(jaxbTaskSumList);
        JaxbTaskSummaryListResponse jaxbTaskSumListRespCopy = testRoundTrip(jaxbTaskSumListResp);
        assertEquals( jaxbTaskSumListResp.getList().size(), jaxbTaskSumListRespCopy.getList().size() );
        TaskSummary taskSum = jaxbTaskSumListResp.getList().get(0);
        TaskSummary taskSumCopy = jaxbTaskSumListRespCopy.getList().get(0);
        ComparePair.compareObjectsViaFields(taskSum, taskSumCopy, 
                "actualOwner", "createdBy",
                "potentialOwners", // null
                "createdOn", "activationTime", "expirationTime",
                "subTaskStrategy"); // dates
    }
   
}