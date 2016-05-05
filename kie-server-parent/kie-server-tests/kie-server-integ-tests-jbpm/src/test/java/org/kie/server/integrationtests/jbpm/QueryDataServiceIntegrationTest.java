/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.QueryServicesClient;

import static org.junit.Assert.*;

public class QueryDataServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "query-definition-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "query-definition-project";
    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/query-definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Override
    protected void additionalConfiguration(KieServicesConfiguration configuration) throws Exception {
        super.additionalConfiguration(configuration);
        // Having timeout issues due to kjar dependencies -> raised timeout.
        configuration.setTimeout(300000);
    }

    @Test
    public void testCRUDOnQueryDefinition() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("PROCESS");
        try {

            queryClient.registerQuery(query);

            List<QueryDefinition> queries = queryClient.getQueries(0, 10);
            assertNotNull(queries);
            assertEquals(1, queries.size());

            QueryDefinition registeredQuery = queries.get(0);
            assertNotNull(registeredQuery);
            assertEquals(query.getName(), registeredQuery.getName());
            assertEquals(query.getSource(), registeredQuery.getSource());
            assertEquals(query.getExpression(), registeredQuery.getExpression());
            assertEquals(query.getTarget(), registeredQuery.getTarget());

            registeredQuery = queryClient.getQuery(query.getName());

            assertNotNull(registeredQuery);
            assertEquals(query.getName(), registeredQuery.getName());
            assertEquals(query.getSource(), registeredQuery.getSource());
            assertEquals(query.getExpression(), registeredQuery.getExpression());
            assertEquals(query.getTarget(), registeredQuery.getTarget());

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());

            List<QueryDefinition> queries = queryClient.getQueries(0, 10);
            assertNotNull(queries);
            assertEquals(0, queries.size());
        }

    }

    @Test
    public void testGetProcessInstancesWithQueryDataService() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            List<ProcessInstance> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, 0, 3, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, "status", 1, 3, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testGetProcessInstancesWithVariablesQueryDataService() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstancesWithVars");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select pil.*, v.variableId, v.value " +
                "from ProcessInstanceLog pil " +
                "inner join (select vil.processInstanceId ,vil.variableId, max(vil.ID) maxvilid  from VariableInstanceLog vil " +
                "group by vil.processInstanceId, vil.variableId) x on (x.processInstanceId = pil.processInstanceId) " +
                "inner join VariableInstanceLog v " +
                "on (v.variableId = x.variableId  and v.id = x.maxvilid and v.processInstanceId = pil.processInstanceId) " +
                "where pil.status = 1");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            List<ProcessInstance> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI_WITH_VARS, 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            for (ProcessInstance instance : instances) {
                assertNotNull(instance.getVariables());
                assertEquals(2, instance.getVariables().size());

                assertEquals("waiting for signal", instance.getVariables().get("stringData"));
                assertEquals("Person{name='john'}", instance.getVariables().get("personData"));
            }

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testGetProcessInstancesFilteredWithVariablesQueryDataService() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstancesWithVars");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select pil.*, v.variableId, v.value " +
                "from ProcessInstanceLog pil " +
                "inner join (select vil.processInstanceId ,vil.variableId, max(vil.ID) maxvilid  from VariableInstanceLog vil " +
                "group by vil.processInstanceId, vil.variableId) x on (x.processInstanceId = pil.processInstanceId) " +
                "inner join VariableInstanceLog v " +
                "on (v.variableId = x.variableId  and v.id = x.maxvilid and v.processInstanceId = pil.processInstanceId) " +
                "where pil.status = 1");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            QueryFilterSpec filterSpec = new QueryFilterSpecBuilder()
                                            .greaterThan("processinstanceid", processInstanceIds.get(3))
                                            .get();


            List<ProcessInstance> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI_WITH_VARS, filterSpec, 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(1, instances.size());

            for (ProcessInstance instance : instances) {
                assertNotNull(instance.getVariables());
                assertEquals(2, instance.getVariables().size());

                assertEquals("waiting for signal", instance.getVariables().get("stringData"));
                assertEquals("Person{name='john'}", instance.getVariables().get("personData"));
            }

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testGetProcessInstancesWithQueryDataServiceUsingCustomMapper() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            List<ProcessInstance> instances = queryClient.query(query.getName(), "CustomMapper", 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            List<Long> found = collectInstances(instances);
            assertEquals(processInstanceIds, found);

            instances = queryClient.query(query.getName(), "CustomMapper", 0, 3, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.query(query.getName(), "CustomMapper", "status", 1, 3, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testGetProcessInstancesWithQueryDataServiceUsingCustomQueryBuilder() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("min", processInstanceIds.get(4));
            params.put("max", processInstanceIds.get(0));

            List<ProcessInstance> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_PI, "test", params, 0, 10, ProcessInstance.class);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testGetProcessInstancesWithQueryDataServiceRawMapper() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            List<List> instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_RAW, 0, 10, List.class);
            assertNotNull(instances);
            assertEquals(5, instances.size());

            for (List row : instances) {
                assertEquals(15, row.size());
            }

            instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_RAW, 0, 3, List.class);
            assertNotNull(instances);
            assertEquals(3, instances.size());

            instances = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_RAW, "status", 1, 3, List.class);
            assertNotNull(instances);
            assertEquals(2, instances.size());

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());
        }

    }

    @Test
    public void testQueryDataServiceReplaceQuery() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        Long pid = processClient.startProcess(CONTAINER_ID, "definition-project.usertask", parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("getTasksByState");
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression("select * from AuditTaskImpl where status = 'Reserved'");
        query.setTarget("CUSTOM");
        try {

            queryClient.registerQuery(query);

            List<TaskInstance> tasks = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_TASK, 0, 10, TaskInstance.class);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            Long taskId = tasks.get(0).getId();

            query.setExpression("select * from AuditTaskImpl where status = 'InProgress'");

            queryClient.replaceQuery(query);

            tasks = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_TASK, 0, 10, TaskInstance.class);
            assertNotNull(tasks);
            assertEquals(0, tasks.size());

            taskClient.startTask(CONTAINER_ID, taskId, USER_YODA);

            tasks = queryClient.query(query.getName(), QueryServicesClient.QUERY_MAP_TASK, 0, 10, TaskInstance.class);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertEquals(taskId, tasks.get(0).getId());
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, pid);
            queryClient.unregisterQuery(query.getName());
        }
    }

    @Test
    public void testCRUDOnQueryDefinitionWithDSAsProperty() throws Exception {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        String expectedResolvedDS = System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stringData", "waiting for signal");
        parameters.put("personData", createPersonInstance("john"));

        List<Long> processInstanceIds = createProcessInstances(parameters);

        QueryDefinition query = new QueryDefinition();
        query.setName("allProcessInstances");
        query.setSource("${org.kie.server.persistence.ds}");
        query.setExpression("select * from ProcessInstanceLog where status = 1");
        query.setTarget("PROCESS");
        try {

            queryClient.registerQuery(query);

            List<QueryDefinition> queries = queryClient.getQueries(0, 10);
            assertNotNull(queries);
            assertEquals(1, queries.size());

            QueryDefinition registeredQuery = queries.get(0);
            assertNotNull(registeredQuery);
            assertEquals(query.getName(), registeredQuery.getName());
            assertEquals(expectedResolvedDS, registeredQuery.getSource());
            assertEquals(query.getExpression(), registeredQuery.getExpression());
            assertEquals(query.getTarget(), registeredQuery.getTarget());

            registeredQuery = queryClient.getQuery(query.getName());

            assertNotNull(registeredQuery);
            assertEquals(query.getName(), registeredQuery.getName());
            assertEquals(expectedResolvedDS, registeredQuery.getSource());
            assertEquals(query.getExpression(), registeredQuery.getExpression());
            assertEquals(query.getTarget(), registeredQuery.getTarget());

        } finally {
            abortProcessInstances(processInstanceIds);
            queryClient.unregisterQuery(query.getName());

            List<QueryDefinition> queries = queryClient.getQueries(0, 10);
            assertNotNull(queries);
            assertEquals(0, queries.size());
        }

    }

    protected List<Long> createProcessInstances(Map<String, Object> parameters) {
        List<Long> processInstanceIds = new ArrayList<Long>();

        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, "definition-project.signalprocess", parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, "definition-project.usertask", parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, "definition-project.signalprocess", parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, "definition-project.usertask", parameters));
        processInstanceIds.add(processClient.startProcess(CONTAINER_ID, "definition-project.signalprocess", parameters));

        Collections.sort(processInstanceIds);
        return processInstanceIds;
    }

    protected void abortProcessInstances(List<Long> processInstanceIds) {
        for (Long piId : processInstanceIds) {
            processClient.abortProcessInstance(CONTAINER_ID, piId);
        }
    }

    protected List<String> collectDefinitions(List<ProcessDefinition> definitions) {
        List<String> ids = new ArrayList<String>();

        for (ProcessDefinition definition : definitions) {
            ids.add(definition.getId());
        }
        return ids;
    }

    protected List<Long> collectInstances(List<ProcessInstance> instances) {
        List<Long> ids = new ArrayList<Long>();

        for (ProcessInstance instance : instances) {
            ids.add(instance.getId());
        }
        Collections.sort(ids);
        return ids;
    }
}
