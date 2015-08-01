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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.services.api.DeploymentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.rest.QueryResourceImpl;
import org.kie.remote.services.rest.query.helpers.InternalProcInstQueryHelper;
import org.kie.remote.services.rest.query.helpers.InternalTaskQueryHelper;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceInfo;
import org.kie.services.client.serialization.jaxb.impl.query.JaxbQueryProcessInstanceResult;

/**
 * This tests Internal*QueryHelper logic
 */
public class QueryResourceQueryHelperTest extends AbstractQueryResourceTest {

    public static final String OBJECT_VARIABLE_PROCESS_ID = "org.jboss.qa.bpms.ObjectVariableProcess";
    public static final String OBJECT_VARIABLE_PROCESS_FILE = "BPMN2-ObjectVariables.bpmn2";

    private QueryResourceImpl queryResource;
    private InternalTaskQueryHelper queryTaskHelper;
    private InternalProcInstQueryHelper queryProcInstHelper;
    
    @Before
    public void init() {
        runtimeManager = createRuntimeManager(OBJECT_VARIABLE_PROCESS_FILE);
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
        
        queryTaskHelper = new InternalTaskQueryHelper(queryResource);
        queryProcInstHelper = new InternalProcInstQueryHelper(queryResource);
        
        runObjectVarProcess(ksession);
    }
   
    @After
    public void cleanup() {
        if( runtimeManager != null ) { 
            runtimeManager.disposeRuntimeEngine(engine);
            runtimeManager.close();
        }
    }

    protected long[] runObjectVarProcess( KieSession ksession) {
        long[] pids = new long[3];
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myobject", "Hello World!");

        ProcessInstance pi = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, params); // completed
        pids[0] = pi.getId();
        params.put("myobject", "Hello Ivo!");
        pi = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, params); // completed
        pids[1] = pi.getId();
        params.put("myobject", "Bye Ivo!");
        pi = ksession.startProcess(OBJECT_VARIABLE_PROCESS_ID, params); // completed
        pids[2] = pi.getId();
        
        return pids;
    }

    @Test
    public void notBeingFilteredTest() {
        int [] pageInfo = { 0, 0 };
        Map<String, String[]> queryParams = new HashMap<String, String[]>(); 
        addParams(queryParams, "processinstancestatus", "2");
        addParams(queryParams, "varregex_myobject", "Hello .*");
       
        JaxbQueryProcessInstanceResult result = queryProcInstHelper.queryTasksOrProcInstsAndVariables(queryParams, pageInfo);
        assertNotNull( "Null result", result );
        assertFalse( "Empty result (all)", result.getProcessInstanceInfoList().isEmpty() );
        assertEquals( "Process instance info results", 2, result.getProcessInstanceInfoList().size() );
        for( JaxbQueryProcessInstanceInfo queryInfo : result.getProcessInstanceInfoList() ) { 
           assertNotNull( "No process instance info!", queryInfo.getProcessInstance() );
           assertEquals( "No variable info!", 1, queryInfo.getVariables().size() );
        }
    }

}
