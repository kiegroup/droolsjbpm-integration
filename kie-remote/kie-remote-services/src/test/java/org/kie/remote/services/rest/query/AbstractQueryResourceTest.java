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

package org.kie.remote.services.rest.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.services.jaxb.ServerJaxbSerializationProvider;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.test.MyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

abstract class AbstractQueryResourceTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(QueryResourceQueryBuilderTest.class);
    
    protected static final String PROCESS_STRING_VAR_FILE = "BPMN2-HumanTaskWithStringVariables.bpmn2";
    protected static final String PROCESS_STRING_VAR_ID = "org.var.human.task.string";
    protected static final String PROCESS_OBJ_VAR_FILE = "BPMN2-HumanTaskWithObjectVariables.bpmn2";
    protected static final String PROCESS_OBJ_VAR_ID = "org.var.human.task.object";
    protected static final String USER_ID = "john";
    
    protected static ObjectMapper jsonMapper = new ObjectMapper();
    protected static JaxbSerializationProvider jaxbClientMapper = ServerJaxbSerializationProvider.newInstance();
    protected static JaxbSerializationProvider jaxbServerMapper = ClientJaxbSerializationProvider.newInstance();

    protected KieSession ksession;
    protected TaskService taskService;
    protected RuntimeManager runtimeManager;
    protected RuntimeEngine engine;
    protected RemoteServicesQueryJPAService jpaService;

    protected List<Long> procInstIds = new ArrayList<Long>();

    public AbstractQueryResourceTest() {
        super(true, true, "org.jbpm.domain");
        
        Logger logger = LoggerFactory.getLogger(DatabaseMetadata.class);
        if( logger instanceof ch.qos.logback.classic.Logger ) { 
            ((ch.qos.logback.classic.Logger) logger).setLevel(Level.OFF);
        }
    }
    
    protected <T> T roundTripJson(T in) throws Exception { 
        String jsonStr = jsonMapper.writeValueAsString(in);
        logger.debug("\n" + jsonStr);
        return (T) jsonMapper.readValue(jsonStr, in.getClass());
    }
   
    protected <T> T roundTripXml(T in) throws Exception { 
        String xmlStr = jaxbServerMapper.serialize(in);
        logger.debug("\n" + xmlStr);
        return (T) jaxbClientMapper.deserialize(xmlStr);
    }
  
    // must be at least 5
    protected static int numTestProcesses = 10;
    protected boolean testDataInitialized = false;
    protected boolean addObjectProcessInstances = true;
    
    protected void setupTestData() { 
        if( ! testDataInitialized ) { 
            for( int i = 0; i < numTestProcesses; ++i ) { 
                runStringProcess(ksession, i);
                if( addObjectProcessInstances ) { 
                    runObjectProcess(ksession, i);
                }
            }
            testDataInitialized = true;
        }
    } 
        
    protected void runStringProcess(KieSession ksession, int i) { 
        Map<String, Object> params = new HashMap<String, Object>();
        String initValue = UUID.randomUUID().toString();
        params.put("inputStr", "proc-" + i + "-" + initValue );
        params.put("otherStr", "proc-" + i + "-" + initValue ); 
        params.put("secondStr", i + "-second-" + random.nextInt(Integer.MAX_VALUE));
        ProcessInstance processInstance = ksession.startProcess(PROCESS_STRING_VAR_ID, params);
        assertTrue( processInstance != null && processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        long procInstId = processInstance.getId();
        procInstIds.add(procInstId);
        
        List<Long> taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        assertFalse( "No tasks found!", taskIds.isEmpty() );
        long taskId = taskIds.get(0); 
        taskService.start(taskId, USER_ID);
        
        Map<String, Object> taskResults = new HashMap<String, Object>();
        taskResults.put("taskOutputStr", "task-1-" + procInstId);
        taskService.complete(taskId, USER_ID, taskResults);
    
        AuditLogService logService = new JPAAuditLogService(getEmf());
        List<VariableInstanceLog> vils = logService.findVariableInstances(procInstId);
        assertTrue( "No variable instance logs found", vils != null && ! vils.isEmpty() );
        assertTrue( "Too few variable instance logs found", vils.size() > 3 );
        
        taskIds = taskService.getTasksByProcessInstanceId(procInstId);
        assertFalse( "No tasks found!", taskIds.isEmpty() );
        taskId = taskIds.get(1); 
        Task task = taskService.getTaskById(taskId);
        taskService.start(taskId, USER_ID);
        
        taskResults = new HashMap<String, Object>();
        taskResults.put("taskOutputStr", "task-2-" + procInstId);
        taskService.complete(taskId, USER_ID, taskResults);
        
        assertNull("Process instance has not been finished.", ksession.getProcessInstance(procInstId) );
    }

    protected static Random random = new Random();
    
    protected void runObjectProcess(KieSession ksession, int i) { 
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

    protected static void addParams(Map<String, String[]> params, String name, String... values ) { 
       params.put(name,  values);
    }
}
