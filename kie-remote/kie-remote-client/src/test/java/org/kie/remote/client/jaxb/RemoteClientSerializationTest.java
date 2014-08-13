package org.kie.remote.client.jaxb;

import static org.kie.remote.client.jaxb.ConversionUtil.convertDateToXmlGregorianCalendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.impl.model.xml.JaxbTaskSummary;
import org.jbpm.services.task.jaxb.ComparePair;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskQueryService;
import org.kie.remote.jaxb.gen.Status;
import org.kie.remote.jaxb.gen.TaskSummary;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteClientSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(RemoteClientSerializationTest.class); 

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
        addClassesToSerializationProvider(JaxbTaskSummary.class);
        
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
       
        List<TaskSummary> genTaskSumList = new ArrayList<TaskSummary>();
        Iterator<org.kie.api.task.model.TaskSummary> iter = taskSumList.iterator();
        while( iter.hasNext() ) { 
            genTaskSumList.add(convertTaskSumToGenJaxbTaskSum(iter.next()));
        }
        JaxbTaskSummaryListResponse jaxbTaskSumListResp = new JaxbTaskSummaryListResponse(genTaskSumList);
        JaxbTaskSummaryListResponse jaxbTaskSumListRespCopy = testRoundTrip(jaxbTaskSumListResp);
        assertEquals( jaxbTaskSumListResp.getList().size(), jaxbTaskSumListRespCopy.getList().size() );
        TaskSummary taskSum = jaxbTaskSumListResp.getList().get(0);
        TaskSummary taskSumCopy = jaxbTaskSumListRespCopy.getList().get(0);
        ComparePair.compareObjectsViaFields(taskSum, taskSumCopy, 
                "potentialOwners", // null
                "createdOn", "activationTime", "expirationTime",
                "subTaskStrategy"); // dates
    }
   
    private List<org.kie.remote.jaxb.gen.TaskSummary> convertToJaxbTaskSummaryList(Collection<org.kie.api.task.model.TaskSummary> list) {
        if( list == null || list.isEmpty() ) { 
            return new ArrayList<org.kie.remote.jaxb.gen.TaskSummary>();
        }
        List<org.kie.remote.jaxb.gen.TaskSummary> newList = new ArrayList<org.kie.remote.jaxb.gen.TaskSummary>(list.size());
        Iterator<org.kie.api.task.model.TaskSummary> iter = list.iterator();
        while(iter.hasNext()) { 
            org.kie.remote.jaxb.gen.TaskSummary taskSum = convertTaskSumToGenJaxbTaskSum(iter.next());
            newList.add(taskSum);
        }
        return newList;
    }
   
    static org.kie.remote.jaxb.gen.TaskSummary convertTaskSumToGenJaxbTaskSum(org.kie.api.task.model.TaskSummary taskSum) { 
        org.kie.remote.jaxb.gen.TaskSummary genTaskSum = new org.kie.remote.jaxb.gen.TaskSummary();
       
        genTaskSum.setActivationTime(convertDateToXmlGregorianCalendar(taskSum.getActivationTime()));
        genTaskSum.setActualOwner(taskSum.getActualOwnerId());
        genTaskSum.setCreatedBy(taskSum.getCreatedById());
        genTaskSum.setCreatedOn(convertDateToXmlGregorianCalendar(taskSum.getCreatedOn()));
        genTaskSum.setDeploymentId(taskSum.getDeploymentId());
        genTaskSum.setDescription(taskSum.getDescription());
        genTaskSum.setExpirationTime(convertDateToXmlGregorianCalendar(taskSum.getExpirationTime()));
        genTaskSum.setId(taskSum.getId());
        genTaskSum.setName(taskSum.getName());
        genTaskSum.setParentId(taskSum.getParentId());
        genTaskSum.setPriority(taskSum.getPriority());
        genTaskSum.setProcessId(taskSum.getProcessId());
        genTaskSum.setProcessInstanceId(taskSum.getProcessInstanceId());
        genTaskSum.setProcessSessionId(taskSum.getProcessSessionId());
        genTaskSum.setQuickTaskSummary(taskSum.isQuickTaskSummary());
        genTaskSum.setSkipable(taskSum.isSkipable());
        genTaskSum.setStatus(Status.fromValue(taskSum.getStatus().toString()));
        genTaskSum.setSubject(taskSum.getSubject());
        
        return genTaskSum;
    }
}
