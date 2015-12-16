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

package org.kie.remote.client.jaxb;

import static org.kie.internal.query.QueryParameterIdentifiers.PROCESS_INSTANCE_ID_LIST;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.impl.model.xml.JaxbI18NText;
import org.jbpm.services.task.impl.model.xml.JaxbTask;
import org.jbpm.services.task.impl.model.xml.JaxbTaskData;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.JsonSerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummary;
import org.kie.test.util.compare.ComparePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteClientSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(RemoteClientSerializationTest.class);

    public RemoteClientSerializationTest() {
        super(true, true, "org.jbpm.persistence.jpa");
    }

    protected JsonSerializationProvider jsonProvider = new JsonSerializationProvider();
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected JaxbSerializationProvider jaxbProvider = ClientJaxbSerializationProvider.newInstance();
    {
        jaxbProvider.setPrettyPrint(true);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        jaxbProvider.addJaxbClassesAndReinitialize(extraClass);
    }

    public <T> T testJaxbRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }

    public <T> T testJsonRoundTrip(T in) throws Exception {
        String xmlObject = jsonProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jsonProvider.deserialize(xmlObject, in.getClass());
    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    @Test
    public void taskSummaryListTest() throws Exception {
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
        assertFalse( "Task summaries available", taskSumList.isEmpty());
        TaskSummaryImpl taskSumImpl = (TaskSummaryImpl) taskSumList.get(0);
        taskSumImpl.setActualOwner(new UserImpl("Minnie"));
        taskSumImpl.setCreatedBy(new UserImpl("Mickey"));

        List<JaxbTaskSummary> jaxbTaskSumList = new ArrayList<JaxbTaskSummary>();
        Iterator<TaskSummary> iter = taskSumList.iterator();
        while( iter.hasNext() ) {
            jaxbTaskSumList.add(new JaxbTaskSummary(iter.next()));
        }
        JaxbTaskSummaryListResponse jaxbTaskSumListResp = new JaxbTaskSummaryListResponse(jaxbTaskSumList);
        JaxbTaskSummaryListResponse jaxbTaskSumListRespCopy = testJaxbRoundTrip(jaxbTaskSumListResp);
        assertEquals( jaxbTaskSumListResp.getList().size(), jaxbTaskSumListRespCopy.getList().size() );
        TaskSummary taskSum = jaxbTaskSumListResp.getList().get(0);
        TaskSummary taskSumCopy = jaxbTaskSumListRespCopy.getList().get(0);
        ComparePair.compareObjectsViaFields(taskSum, taskSumCopy,
                "actualOwner", "createdBy",
                "potentialOwners", // null
                "createdOn", "activationTime", "expirationTime",
                "subTaskStrategy"); // dates
    }

    @Test
    public void jsonTaskStringTest() throws Exception {
        objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

       JaxbTask serverTask = new JaxbTask();
       List<I18NText> names = new ArrayList<I18NText>();
       serverTask.setPriority(2);
       serverTask.setNames(names);
       serverTask.setId(6l);
       JaxbI18NText text = new JaxbI18NText();
       text.setId(2l);
       text.setLanguage("nl-NL");
       text.setText("Doei!");
       names.add(text);
       JaxbTaskData taskData = new JaxbTaskData();
       serverTask.setTaskData(taskData);
       taskData.setActualOwnerId("me");
       taskData.setCreatedById("you");
       taskData.setCreatedOn(new Date());
       taskData.setDeploymentId("this");
       taskData.setDocumentContentId(0l);
       taskData.setDocumentType("this");
       taskData.setFaultContentId(1l);
       taskData.setFaultName("whoops");
       taskData.setFaultType("that");
       taskData.setOutputType("theirs");
       taskData.setSkipable(true);
       taskData.setWorkItemId(3l);
       taskData.setProcessInstanceId(3l);
       taskData.setOutputContentId(3l);
       taskData.setParentId(3l);
       taskData.setProcessSessionId(2l);

       String jsonTaskStr = objectMapper.writeValueAsString(serverTask);
       logger.debug( jsonTaskStr );
       assertFalse( "String contains 'realClass' attribute", jsonTaskStr.contains("realClass"));
       Task clientTask = jsonProvider.deserialize(jsonTaskStr, JaxbTask.class);
       long id = clientTask.getId();
       assertEquals("task id", 6, id);
    }
}