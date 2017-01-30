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

package org.kie.server.integrationtests.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.junit.Assert.*;

public class KieServerRouterJbpmIntegrationTest extends KieServerRouterBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");


    protected static final String CONTAINER_ALIAS = "project";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        createContainer(CONTAINER_ID, releaseId, CONTAINER_ALIAS);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void testStartProcessWithContainerAlias() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", USER_MARY);
        parameters.put("number", new Integer(12345));
        List<Object> list = new ArrayList<Object>();
        list.add("item");
        parameters.put("list", list);
        parameters.put("person", person);

        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ALIAS, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV1);
        assertTrue(processInstanceIdV1.longValue() > 0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ALIAS, processInstanceIdV1);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        ServiceResponse<KieContainerResourceList> containersResponse = client.listContainers();
        KieServerAssert.assertSuccess(containersResponse);

        List<KieContainerResource> containerResources = containersResponse.getResult().getContainers();
        assertEquals(1, containerResources.size());
    }

    @Test
    public void testStartProcessWithContainerId() throws Exception {

        Object person = createPersonInstance(USER_JOHN);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("test", USER_MARY);
        parameters.put("number", new Integer(12345));
        List<Object> list = new ArrayList<Object>();
        list.add("item");
        parameters.put("list", list);
        parameters.put("person", person);

        Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

        assertNotNull(processInstanceIdV1);
        assertTrue(processInstanceIdV1.longValue() > 0);

        ProcessInstance processInstance = processClient.getProcessInstance(CONTAINER_ID, processInstanceIdV1);
        assertNotNull(processInstance);
        assertEquals(CONTAINER_ID, processInstance.getContainerId());

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        assertEquals(1, tasks.size());

        processClient.abortProcessInstance(CONTAINER_ID, processInstanceIdV1);
    }

    @Test
    public void testStartProcessAndQueryViaAdvancedQueries() throws Exception {

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("PROCESS");

        queryClient.registerQuery(query);
        try {
            Object person = createPersonInstance(USER_JOHN);

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("test", USER_MARY);
            parameters.put("number", new Integer(12345));
            List<Object> list = new ArrayList<Object>();
            list.add("item");
            parameters.put("list", list);
            parameters.put("person", person);

            Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

            assertNotNull(processInstanceIdV1);
            assertTrue(processInstanceIdV1.longValue() > 0);

            List<ProcessInstance> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            queryClient.replaceQuery(query);

            QueryFilterSpec filterSpec = new QueryFilterSpecBuilder()
                    .greaterThan("processinstanceid", 0)
                    .get();


            instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, filterSpec, 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            processClient.abortProcessInstance(CONTAINER_ID, processInstanceIdV1);
        } catch (Exception e){
          e.printStackTrace();
          fail(e.getMessage());
        } finally {
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testStartProcessAndQueryViaAdvancedQueriesRawContent() throws Exception {

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("PROCESS");

        queryClient.registerQuery(query);
        try {
            Object person = createPersonInstance(USER_JOHN);

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("test", USER_MARY);
            parameters.put("number", new Integer(12345));
            List<Object> list = new ArrayList<Object>();
            list.add("item");
            parameters.put("list", list);
            parameters.put("person", person);

            Long processInstanceIdV1 = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);

            assertNotNull(processInstanceIdV1);
            assertTrue(processInstanceIdV1.longValue() > 0);

            List<List> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_RAW, 0, 10, List.class);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            for (List row : instances) {
                assertEquals(16, row.size());
            }


            processClient.abortProcessInstance(CONTAINER_ID, processInstanceIdV1);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            queryClient.unregisterQuery(query.getName());
        }

    }
}
