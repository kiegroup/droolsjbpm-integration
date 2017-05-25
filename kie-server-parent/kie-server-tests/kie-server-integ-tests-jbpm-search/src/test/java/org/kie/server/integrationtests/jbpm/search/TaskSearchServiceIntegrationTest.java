/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm.search;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.api.model.definition.TaskField;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.util.TaskQueryFilterSpecBuilder;

import java.util.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TaskSearchServiceIntegrationTest extends JbpmQueriesKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Before
    public void cleanup()  {
        super.cleanup();
    }

    @Test
    public void testFindTaskWithIncompatibleTypeFilter() throws Exception {
        assertClientException(
                () -> searchServicesClient.findHumanTasksWithFilters(createQueryFilterEqualsTo(TaskField.NAME, 1), 0, 100),
                500,
                "Can't lookup on specified data set: getTasksWithFilters");
    }

    @Test
    public void testFindTaskWithCreatedOnEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterGreaterThan(TaskField.CREATEDON, Date.from(Instant.EPOCH)), task.getId());
    }

    @Test
    public void testFindTaskWithProcessIdEqualsToFilter() throws Exception {
        Long processInstanceId  = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.PROCESSID, PROCESS_ID_USERTASK), task.getId());
    }

    @Test
    public void testFindTaskWithDescriptionEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.DESCRIPTION, ""), task.getId());
    }

    @Test
    public void testFindTaskWithProcessInstanceIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.PROCESSINSTANCEID, processInstanceId), task.getId());
    }

    @Test
    public void testFindTaskWithActualOwnerEqualsToFilter() throws Exception {
        Long processInstanceId  = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.ACTUALOWNER, USER_YODA), task.getId());
    }

    @Test
    public void testFindTaskWithCreatedByEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.CREATEDBY, USER_YODA), task.getId());
    }

    @Test
    public void testFindTaskWithNameEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.NAME, "First task"), task.getId());
    }
    @Test
    public void testFindTaskWithTaskIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.TASKID, task.getId()), task.getId());
    }

    @Test
    public void testFindTaskWithDeploymentIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.DEPLOYMENTID, CONTAINER_ID), task.getId());
    }

    @Test
    public void testFindTaskWithStatusEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterEqualsTo(TaskField.STATUS, task.getStatus()), task.getId());
    }


        @Test
    public void testFindTaskWithPriorityGreaterThanFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterGreaterThan(TaskField.PRIORITY, -1), task.getId());
    }

    @Test
    public void testFindTaskWithActivationTimeGreaterThanFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterGreaterThan(TaskField.ACTIVATIONTIME, Date.from(Instant.EPOCH)), task.getId());
    }

    @Test
    public void testFindTaskWithParentIdGreaterThanAndEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);
        testFindTaskInstanceWithSearchService(createQueryFilterGreaterThanAndEqualsTo(TaskField.PARENTID, -2, -1), task.getId());
    }

    @Test
    public void testFindTaskWithAndEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_USERTASK);
        Assertions.assertThat(processInstanceId).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner(USER_YODA, 0, 10);
        Assertions.assertThat(tasks.size()).isGreaterThan(0);
        TaskSummary task = tasks.get(0);

        HashMap<TaskField, Comparable<?>> compareList = new HashMap<>();
        compareList.put(TaskField.TASKID, task.getId());
        compareList.put(TaskField.DEPLOYMENTID, CONTAINER_ID);
        compareList.put(TaskField.PROCESSINSTANCEID, processInstanceId);
        compareList.put(TaskField.NAME, "First task");
        compareList.put(TaskField.CREATEDBY, USER_YODA);
        compareList.put(TaskField.ACTUALOWNER, USER_YODA);
        compareList.put(TaskField.DESCRIPTION, "");
        compareList.put(TaskField.DUEDATE, task.getExpirationTime());
        compareList.put(TaskField.PRIORITY, task.getPriority());
        compareList.put(TaskField.STATUS, task.getStatus());

        List<Long> resultsIds = new ArrayList<>();
        List<TaskInstance> results = null;

        results = searchServicesClient.
                findHumanTasksWithFilters(createQueryFilterAndEqualsTo(compareList), 0, 100);

        resultsIds = new ArrayList<>();
        for (TaskInstance res : results) {
            resultsIds.add(res.getId());
        }

        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results.size()).isGreaterThanOrEqualTo(0);
        Assertions.assertThat(resultsIds).contains(task.getId());

        final TaskInstance[] instance = new TaskInstance[1];
        results.forEach((t) -> {
            if (t.getId().equals(task.getId())){
                instance[0] = t;
            }
        });

        Assertions.assertThat(instance[0].getContainerId()).isEqualTo(CONTAINER_ID);
        Assertions.assertThat(instance[0].getProcessInstanceId()).isEqualTo(processInstanceId);
        Assertions.assertThat(instance[0].getName()).isEqualTo("First task");
        Assertions.assertThat(instance[0].getActualOwner()).isEqualTo(USER_YODA);
        Assertions.assertThat(instance[0].getCreatedBy()).isEqualTo(USER_YODA);
        Assertions.assertThat(instance[0].getDescription()).isEqualTo("");
        Assertions.assertThat(instance[0].getExpirationDate()).isEqualTo(task.getExpirationTime());
        Assertions.assertThat(instance[0].getPriority()).isEqualTo(task.getPriority());
        Assertions.assertThat(instance[0].getStatus()).isEqualTo(task.getStatus());
    }

    private void testFindTaskInstanceWithSearchService(TaskQueryFilterSpec filter, Long taskInstanceId) {
        List<Long> resultsIds = new ArrayList<>();
        List<TaskInstance> results = null;

        results = searchServicesClient.
                findHumanTasksWithFilters(filter, 0, 100);

        resultsIds = new ArrayList<>();
        for (TaskInstance res : results) {
            resultsIds.add(res.getId());
        }

        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results.size()).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(resultsIds).contains(taskInstanceId);
    }

    private TaskQueryFilterSpec createQueryFilterEqualsTo(TaskField taskField, Comparable<?> equalsTo) {
        return  new TaskQueryFilterSpecBuilder().equalsTo(taskField, equalsTo).get();
    }

    private TaskQueryFilterSpec createQueryFilterGreaterThan(TaskField taskField, Comparable<?> greaterThan) {
        return  new TaskQueryFilterSpecBuilder().greaterThan(taskField, greaterThan).get();
    }

    private TaskQueryFilterSpec createQueryFilterGreaterThanAndEqualsTo(TaskField taskField, Comparable<?> greaterThan, Comparable<?> equalsTo) {
        return  new TaskQueryFilterSpecBuilder().greaterThan(taskField, greaterThan).equalsTo(taskField, equalsTo).get();
    }

    private TaskQueryFilterSpec createQueryFilterAndEqualsTo(Map<TaskField, Comparable<?>> filterProperties) {
        TaskQueryFilterSpecBuilder result = new TaskQueryFilterSpecBuilder();
        filterProperties.forEach(result::equalsTo);
        return  result.get();
    }

}
