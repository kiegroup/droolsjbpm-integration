/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.taskassigning.PlanningItem;
import org.kie.server.api.model.taskassigning.PlanningItemList;
import org.kie.server.api.model.taskassigning.PlanningTask;
import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.api.model.taskassigning.TaskInputVariablesReadMode;
import org.kie.server.client.util.TaskDataReader;

import static org.kie.server.api.model.taskassigning.TaskStatus.InProgress;
import static org.kie.server.api.model.taskassigning.TaskStatus.Ready;
import static org.kie.server.api.model.taskassigning.TaskStatus.Reserved;
import static org.kie.server.api.model.taskassigning.TaskStatus.Suspended;

public class IntegrationTests {

    private static final String SERVER_URL = "http://localhost:8180/kie-server/services/rest/server|http://localhost:8080/kie-server/services/rest/server";
    //private static final String SERVER_URL = "http://localhost:8180/kie-server/services/rest/server";

    private static String CONTAINER_ID = "com.myspace:test-process:1.0.0-SNAPSHOT";
    private static String PROCESS_ID = "test-process.Produce_Task_For_HR";

    private static List<Integer> pageSizes = Arrays.asList(3000);

    private Map<String, StopWatch> timeRegistry = new HashMap<>();

    private TaskAssigningRuntimeClient client;

    @Before
    public void setUp() {
        timeRegistry.clear();
        client = newTaskAssigningRuntimeClient();
    }

    @Test
    public void findTasksWithTaskAssigningClientAllVariablesReadTime() {
        pageSizes.forEach(pageSize -> testFindTasksWithTaskAssigningClientTimes(pageSize, TaskInputVariablesReadMode.READ_FOR_ALL));
    }

    @Test
    public void findTasksWithTaskAssigningClientVariablesWhenPlanningTaskIsNull() {
        pageSizes.forEach(pageSize -> testFindTasksWithTaskAssigningClientTimes(pageSize, TaskInputVariablesReadMode.READ_WHEN_PLANNING_TASK_IS_NULL));
    }

    private void testFindTasksWithTaskAssigningClientTimes(int pageSize, TaskInputVariablesReadMode inputVariablesReadMode) {
        List<String> status = Arrays.asList(Ready,
                                            Reserved,
                                            InProgress,
                                            Suspended);

        registerStartTime("findTasks");
        List<TaskData> list = client.findTasks(0L, status, null, 0, pageSize, inputVariablesReadMode).getItems();
        StopWatch stopWatch = registerEndTime("findTasks");
        System.out.println("Tasks size with variablesReadMode: " + inputVariablesReadMode + " pageSize: " + pageSize + " totalTasks: " + list.size() + " in " + printTime((stopWatch)));
    }

    @Test
    public void findTasksWithTasksReaderAllVariablesReadTime() {
        pageSizes.forEach(pageSize -> findTasksWithTasksReaderTimes(pageSize, TaskInputVariablesReadMode.READ_FOR_ALL));
    }

    @Test
    public void findTasksWithTasksReaderVariablesWhenPlanningTaskIsNull() {
        pageSizes.forEach(pageSize -> findTasksWithTasksReaderTimes(pageSize, TaskInputVariablesReadMode.READ_WHEN_PLANNING_TASK_IS_NULL));
    }

    @Test
    public void findTasksWithTasksReaderNoVariables() {
        pageSizes.forEach(pageSize -> findTasksWithTasksReaderTimes(pageSize, TaskInputVariablesReadMode.DONT_READ));
    }

    private void findTasksWithTasksReaderTimes(int pageSize, TaskInputVariablesReadMode inputVariablesReadMode) {
        List<String> status = Arrays.asList(Ready,
                                            Reserved,
                                            InProgress,
                                            Suspended);

        registerStartTime("findTasks");
        List<TaskData> list = TaskDataReader.from(client).readTasks(0L, status, null, pageSize, inputVariablesReadMode).getTasks();
        StopWatch stopWatch = registerEndTime("findTasks");
        System.out.println("With TaskDataReader, Tasks size with variablesReadMode: " + inputVariablesReadMode + " pageSize: " + pageSize + " totalTasks: " + list.size() + " in " + printTime(stopWatch));
    }

    private static String printTime(StopWatch stopWatch) {
        return stopWatch.toString();
    }

    @Test
    public void createProcessInstances() {

        int processInstancesSize = 5;
        List<Long> processInstances = new ArrayList<>();

        ProcessServicesClient processServices = newKieServicesClient().getServicesClient(ProcessServicesClient.class);

        long processInstanceId;
        for (int i = 0; i < processInstancesSize; i++) {
            HashMap inputParams = new HashMap();
            inputParams.put("processVar1", "generatedValue.for.taskInput1_" + i);
            inputParams.put("processVar2", "generatedValue.for.taskInput2_" + i);
            processInstanceId = processServices.startProcess(CONTAINER_ID, PROCESS_ID);
            processInstances.add(processInstanceId);
        }
        String ids = processInstances.stream().map(Object::toString).collect(Collectors.joining(", "));
        System.out.println("Created process instances: [" + ids + "]");
    }

    @Test
    public void destroyProcessInstances() {
        List<Long> processInstanceIds = new ArrayList<>();

        ProcessServicesClient processServices = newKieServicesClient().getServicesClient(ProcessServicesClient.class);

        List<ProcessInstance> processInstances = processServices.findProcessInstances(CONTAINER_ID, 0, 10000);
        processInstances.forEach(processInstance -> {
            if (PROCESS_ID.equals(processInstance.getProcessId()) &&
                    processInstance.getState() == org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE) {
                processInstanceIds.add(processInstance.getId());
                processServices.abortProcessInstance(CONTAINER_ID, processInstance.getId());
            }
        });
        String ids = processInstanceIds.stream().map(Object::toString).collect(Collectors.joining(", "));
        System.out.println("Destroyed process instances: [" + ids + "]");
    }

    @Test
    public void executePlanning() {
        List<String> status = Arrays.asList(Ready,
                                            Reserved,
                                            InProgress,
                                            Suspended);
        int pageSize = 3000;

        registerStartTime("readTasks");
        List<TaskData> taskList = TaskDataReader.from(client).readTasks(0L, status, null, pageSize, TaskInputVariablesReadMode.DONT_READ).getTasks();
        StopWatch readTasksStopWatch = registerEndTime("readTasks");

        String[] potentialOwners = {"user0", "user1", "user2", "user3", "user4", "user5"};
        int publishWindowSize = 2;

        Map<String, List<PlanningItem>> userIdToPlanningItems = new HashMap<>();
        List<PlanningItem> userPlanningItems;

        int userIndex = -1;
        String userId;
        int i = 0;
        for (TaskData taskData : taskList) {
            userIndex = (userIndex + 1) % potentialOwners.length;
            userId = potentialOwners[userIndex];
            userPlanningItems = userIdToPlanningItems.computeIfAbsent(userId, s -> new ArrayList<>());

            PlanningItem planningItem = PlanningItem.builder()
                    .taskId(taskData.getTaskId())
                    .processInstanceId(taskData.getProcessInstanceId())
                    .containerId(taskData.getContainerId())
                    .planningTask(PlanningTask.builder()
                                          .taskId(taskData.getTaskId())
                                          .assignedUser(userId)
                                          .index(userPlanningItems.size())
                                          .published(userPlanningItems.size() < publishWindowSize)
                                          .build())
                    .build();
            if (i++ < 10000) {
                userPlanningItems.add(planningItem);
            }
        }
        List<PlanningItem> planningItems = userIdToPlanningItems.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        registerStartTime("executePlanning");
        client.executePlanning(new PlanningItemList(planningItems), "wbadmin");
        StopWatch executePlanningStopWatch = registerEndTime("executePlanning");

        System.out.println(taskList.size() + " tasks were read with the TaskDataReader and a pageSize: " + pageSize + " in: " + printTime(readTasksStopWatch));
        System.out.println("Planning with " + planningItems.size() + " items and " + potentialOwners.length + " assigned users items was executed in: " + printTime(executePlanningStopWatch));
    }

    @Test
    public void testExecutePlanningFailure() {
        List<PlanningItem> planningItems = new ArrayList<>();
        planningItems.add(PlanningItem.builder().taskId((long) 1)
                                  .containerId(CONTAINER_ID)
                                  .processInstanceId((long) 1)
                                  .planningTask(PlanningTask.builder()
                                                        .assignedUser("rollback")
                                                        .taskId((long) 1)
                                                        .index(3333)
                                                        .published(false)
                                                        .build()).build());
        PlanningItemList planningItemList = new PlanningItemList();
        planningItemList.setPlanningItems(planningItems.toArray(new PlanningItem[0]));
        client.executePlanning(planningItemList, "wbadmin");
    }

    private KieServicesClient newKieServicesClient() {
        return TaskAssigningRuntimeClientFactory.createKieServicesClient(SERVER_URL,
                                                                         "wbadmin",
                                                                         "wbadmin");
    }

    private TaskAssigningRuntimeClient newTaskAssigningRuntimeClient() {
        KieServicesClient servicesClient = TaskAssigningRuntimeClientFactory.createKieServicesClient(SERVER_URL,
                                                                                                     "wbadmin",
                                                                                                     "wbadmin");
        return servicesClient.getServicesClient(TaskAssigningRuntimeClient.class);
    }

    private void registerStartTime(String timeId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        timeRegistry.put(timeId, stopWatch);
    }

    private StopWatch registerEndTime(String timeId) {
        StopWatch stopWatch = timeRegistry.get(timeId);
        stopWatch.stop();
        return stopWatch;
    }
}
