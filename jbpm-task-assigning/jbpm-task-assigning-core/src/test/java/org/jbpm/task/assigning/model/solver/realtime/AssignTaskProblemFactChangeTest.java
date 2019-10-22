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

package org.jbpm.task.assigning.model.solver.realtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.jbpm.task.assigning.model.User;
import org.junit.Test;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssignTaskProblemFactChangeTest extends BaseProblemFactChangeTest {

    private static final String FIXED_TEST = "Fixed";
    private static final String RANDOM_TEST = "Random";

    private class WorkingSolutionAwareProblemFactChange
            extends AssignTaskProblemFactChange {

        private Consumer<TaskAssigningSolution> solutionBeforeChangesConsumer;

        WorkingSolutionAwareProblemFactChange(Task task,
                                              User user,
                                              Consumer<TaskAssigningSolution> solutionBeforeChangesConsumer) {
            super(task, user);
            this.solutionBeforeChangesConsumer = solutionBeforeChangesConsumer;
        }

        @Override
        public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
            TaskAssigningSolution solution = scoreDirector.getWorkingSolution();
            if (solutionBeforeChangesConsumer != null) {
                solutionBeforeChangesConsumer.accept(solution);
            }
            super.doChange(scoreDirector);
        }
    }

    private class ProgrammedAssignTaskProblemFactChange extends ProgrammedProblemFactChange<AssignTaskProblemFactChange> {

        StringBuilder workingSolutionBeforeChange = new StringBuilder();

        ProgrammedAssignTaskProblemFactChange(Task task, User user) {
            setChange(new WorkingSolutionAwareProblemFactChange(task,
                                                                user,
                                                                workingSolution -> printSolution(workingSolution, workingSolutionBeforeChange)));
        }

        String workingSolutionBeforeChangeAsString() {
            return workingSolutionBeforeChange.toString();
        }

        String solutionAfterChangeAsString() {
            return printSolution(super.getSolutionAfterChange());
        }
    }

    @Test
    public void assignTaskProblemFactChange24Tasks8UsersTest() throws Exception {
        assignTaskProblemFactChangeFixedChangeSetTest(_24TASKS_8USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange24Tasks8UsersRandomTest() throws Exception {
        assignTaskProblemFactChangeRandomChangeSetTest(_24TASKS_8USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange50Tasks5UsersTest() throws Exception {
        assignTaskProblemFactChangeFixedChangeSetTest(_50TASKS_5USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange50Tasks5UsersRandomTest() throws Exception {
        assignTaskProblemFactChangeRandomChangeSetTest(_50TASKS_5USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange100Tasks5UsersTest() throws Exception {
        assignTaskProblemFactChangeFixedChangeSetTest(_100TASKS_5USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange100Tasks5UsersRandomTest() throws Exception {
        assignTaskProblemFactChangeRandomChangeSetTest(_100TASKS_5USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange500Tasks5UsersTest() throws Exception {
        assignTaskProblemFactChangeFixedChangeSetTest(_500TASKS_20USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChange500Tasks5UsersRandomTest() throws Exception {
        assignTaskProblemFactChangeRandomChangeSetTest(_500TASKS_20USERS_SOLUTION);
    }

    @Test
    public void assignTaskProblemFactChangeUserNotFoundTest() throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(_24TASKS_8USERS_SOLUTION);
        Task task = solution.getTaskList().get(0);
        User user = new User(-12345, "Non Existing");
        expectedException.expectMessage("Expected user: " + user + " was not found in current working solution");
        executeSequentialChanges(solution, Collections.singletonList(new ProgrammedAssignTaskProblemFactChange(task, user)));
    }

    private void assignTaskProblemFactChangeFixedChangeSetTest(String solutionResource) throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);
        solution.getUserList().add(User.PLANNING_USER);

        //prepare the list of changes to program
        List<ProgrammedAssignTaskProblemFactChange> programmedChanges = new ArrayList<>();

        //assign Task_0 to User_0
        Task task = solution.getTaskList().get(0);
        User user = solution.getUserList().get(0);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_10 to User_0
        task = solution.getTaskList().get(10);
        user = solution.getUserList().get(0);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_15 to User_2
        task = solution.getTaskList().get(15);
        user = solution.getUserList().get(2);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_13 to User_3
        task = solution.getTaskList().get(13);
        user = solution.getUserList().get(3);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_13 to User_4
        task = solution.getTaskList().get(13);
        user = solution.getUserList().get(4);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_13 to User_5
        task = solution.getTaskList().get(13);
        user = solution.getUserList().get(5);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_15 to User_5
        task = solution.getTaskList().get(15);
        user = solution.getUserList().get(5);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_16 to User_5
        task = solution.getTaskList().get(16);
        user = solution.getUserList().get(5);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign Task_17 to User_5
        task = solution.getTaskList().get(17);
        user = solution.getUserList().get(5);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //repeat assign Task_17 to User_5
        task = solution.getTaskList().get(17);
        user = solution.getUserList().get(5);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //repeat assign Task_15 to User_5
        task = solution.getTaskList().get(15);
        user = solution.getUserList().get(5);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        long nextTaskId = solution.getTaskList().stream()
                .mapToLong(Task::getId)
                .max().orElse(-1) + 1;

        //assign a brand new task "NewTask_x and assign to User_0
        user = solution.getUserList().get(0);
        task = new Task(nextTaskId, "NewTask_" + nextTaskId, 1);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign a brand new task "NewTask_x and assign to User_2
        nextTaskId++;
        user = solution.getUserList().get(2);
        task = new Task(nextTaskId, "NewTask_" + nextTaskId, 1);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        //assign a brand new task "NewTask_x and assign to User_5
        nextTaskId++;
        user = solution.getUserList().get(5);
        task = new Task(nextTaskId, "NewTask_" + nextTaskId, 1);
        programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(task, user));

        assignTaskProblemFactChangeTest(solution, solutionResource, FIXED_TEST, programmedChanges);
    }

    private void assignTaskProblemFactChangeRandomChangeSetTest(String solutionResource) throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);
        solution.getUserList().add(User.PLANNING_USER);

        int taskCount = solution.getTaskList().size();
        int userCount = solution.getUserList().size();
        int randomChanges = taskCount / 2 + random.nextInt(taskCount / 2);

        //prepare the list of changes to program
        List<ProgrammedAssignTaskProblemFactChange> programmedChanges = new ArrayList<>();

        Task randomTask;
        User randomUser;
        for (int i = 0; i < randomChanges; i++) {
            randomTask = solution.getTaskList().get(random.nextInt(taskCount));
            randomUser = solution.getUserList().get(random.nextInt(userCount));
            programmedChanges.add(new ProgrammedAssignTaskProblemFactChange(randomTask, randomUser));
        }
        assignTaskProblemFactChangeTest(solution, solutionResource, RANDOM_TEST, programmedChanges);
    }

    private void assignTaskProblemFactChangeTest(TaskAssigningSolution solution,
                                                 String solutionResource,
                                                 String testType,
                                                 List<ProgrammedAssignTaskProblemFactChange> programmedChanges) throws Exception {
        TaskAssigningSolution initialSolution = executeSequentialChanges(solution, programmedChanges);
        if (writeTestFiles()) {
            String resourceName = solutionResource.substring(solutionResource.lastIndexOf("/") + 1);
            writeToTempFile("AssignTaskProblemFactChangeTest.assignTaskProblemFactChangeTest." + testType + ".InitialSolution_", printSolution(initialSolution));
            for (int i = 0; i < programmedChanges.size(); i++) {
                ProgrammedAssignTaskProblemFactChange scheduledChange = programmedChanges.get(i);
                try {
                    writeToTempFile("AssignTaskProblemFactChangeTest.assignTaskProblemFactChangeTest." + testType + ".WorkingSolutionBeforeChange_" + resourceName + "_" + i + "__", scheduledChange.workingSolutionBeforeChangeAsString());
                    writeToTempFile("AssignTaskProblemFactChangeTest.assignTaskProblemFactChangeTest." + testType + ".SolutionAfterChange_" + resourceName + "_" + i + "__", scheduledChange.solutionAfterChangeAsString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //each partial solution must have the change that was applied on it.
        programmedChanges.forEach(change -> assertAssignTaskProblemFactChangeWasProduced(change.getChange(), change.getSolutionAfterChange()));

        //finally the last solution must have the result of all the changes.
        TaskAssigningSolution lastSolution = programmedChanges.get(programmedChanges.size() - 1).getSolutionAfterChange();
        Map<Long, AssignTaskProblemFactChange> summarizedChanges = new HashMap<>();
        programmedChanges.forEach(change -> {
            //if  task was changed multiple times record only the last change.
            summarizedChanges.put(change.getChange().getTask().getId(), change.getChange());
        });
        summarizedChanges.values().forEach(change -> assertAssignTaskProblemFactChangeWasProduced(change, lastSolution));
    }

    /**
     * Given an AssignTaskProblemFactChange and a solution that was produced as the result of applying the change,
     * asserts that the assignment defined by the change is not violated (exists in) by the solution.
     * The assignment defined in the change must also be pinned in the produced solution as well as any other
     * previous assignment for the given user.
     * @param change The change that was executed for producing the solution.
     * @param solution The produced solution.
     */
    private void assertAssignTaskProblemFactChangeWasProduced(AssignTaskProblemFactChange change, TaskAssigningSolution solution) {
        User internalUser = solution.getUserList().stream()
                .filter(user -> Objects.equals(user.getId(), change.getUser().getId()))
                .findFirst().orElse(null);

        assertNotNull(internalUser);
        Task internalTask = solution.getTaskList().stream()
                .filter(task -> Objects.equals(task.getId(), change.getTask().getId()))
                .findFirst().orElse(null);
        assertNotNull(internalTask);
        assertEquals(internalUser, internalTask.getUser());
        assertTrue(internalTask.isPinned());
        //all the previous tasks must be pinned by construction and be assigned to the user
        TaskOrUser previousTaskOrUser = internalTask.getPreviousTaskOrUser();
        while (previousTaskOrUser != null) {
            if (previousTaskOrUser instanceof Task) {
                Task previousTask = (Task) previousTaskOrUser;
                assertTrue(previousTask.isPinned());
                assertEquals(internalUser, previousTask.getUser());
                previousTaskOrUser = previousTask.getPreviousTaskOrUser();
            } else {
                assertEquals(internalUser, previousTaskOrUser);
                previousTaskOrUser = null;
            }
        }
        //all the next tasks must to the user.
        Task nextTask = internalTask.getNextTask();
        while (nextTask != null) {
            assertEquals(internalUser, nextTask.getUser());
            nextTask = nextTask.getNextTask();
        }
    }

    private static void writeToTempFile(String fileName, String content) throws IOException {
        File tmpFile = File.createTempFile(fileName, null);
        Files.write(tmpFile.toPath(), content.getBytes());
    }
}
