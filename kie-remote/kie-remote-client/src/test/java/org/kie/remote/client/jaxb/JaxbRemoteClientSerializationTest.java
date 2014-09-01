package org.kie.remote.client.jaxb;

import java.util.ArrayList;
import java.util.Collection;
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
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskQueryService;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbRemoteClientSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(JaxbRemoteClientSerializationTest.class); 

    protected JaxbSerializationProvider jaxbProvider = new JaxbSerializationProvider();
    { 
        jaxbProvider.setPrettyPrint(true);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClasses(extraClass);
    }

    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }
    
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
        
        Map<String, List<?>> fieldVals = new HashMap<String, List<?>>();
        fieldVals.put(TaskQueryService.PROCESS_INST_ID_LIST, statuses);
        List<org.kie.api.task.model.TaskSummary> taskSumList = ((InternalTaskService)taskService).getTasksByVariousFields(fieldVals, true);
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
   
    private List<JaxbTaskSummary> convertToJaxbTaskSummaryList(Collection<org.kie.api.task.model.TaskSummary> list) {
        if( list == null || list.isEmpty() ) { 
            return new ArrayList<JaxbTaskSummary>();
        }
        List<JaxbTaskSummary> newList = new ArrayList<JaxbTaskSummary>(list.size());
        Iterator<TaskSummary> iter = list.iterator();
        while(iter.hasNext()) { 
            JaxbTaskSummary taskSum = new JaxbTaskSummary(iter.next());
            newList.add(taskSum);
        }
        return newList;
    }
}