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

package org.kie.server.services.taskassigning.core.model.solver.realtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_100TASKS_5USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_24TASKS_8USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_500TASKS_20USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_50TASKS_5USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER;

public class AssignTaskProblemFactChangeTest extends AbstractProblemFactChangeTest {

    private static final String FIXED_TEST = "Fixed";
    private static final String RANDOM_TEST = "Random";

    private class WorkingSolutionAwareProblemFactChange
            extends AssignTaskProblemFactChange {

        private Consumer<TaskAssigningSolution<?>> solutionBeforeChangesConsumer;

        WorkingSolutionAwareProblemFactChange(Task task,
                                              User user,
                                              Consumer<TaskAssigningSolution<?>> solutionBeforeChangesConsumer) {
            super(task, user);
            this.solutionBeforeChangesConsumer = solutionBeforeChangesConsumer;
        }

        @Override
        public void doChange(ScoreDirector<TaskAssigningSolution<?>> scoreDirector) {
            TaskAssigningSolution<?> solution = scoreDirector.getWorkingSolution();
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
    public void assignTaskProblemFactChange24Tasks8Users() throws Exception {
        assignTaskProblemFactChangeFixedChangeSet(SET_OF_24TASKS_8USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange24Tasks8UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        assignTaskProblemFactChangeRandomChangeSet(SET_OF_24TASKS_8USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange50Tasks5Users() throws Exception {
        checkRunTurtleTests();
        assignTaskProblemFactChangeFixedChangeSet(SET_OF_50TASKS_5USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange50Tasks5UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        assignTaskProblemFactChangeRandomChangeSet(SET_OF_50TASKS_5USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange100Tasks5Users() throws Exception {
        checkRunTurtleTests();
        assignTaskProblemFactChangeFixedChangeSet(SET_OF_100TASKS_5USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange100Tasks5UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        assignTaskProblemFactChangeRandomChangeSet(SET_OF_100TASKS_5USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange500Tasks20Users() throws Exception {
        checkRunTurtleTests();
        assignTaskProblemFactChangeFixedChangeSet(SET_OF_500TASKS_20USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChange500Tasks20UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        assignTaskProblemFactChangeRandomChangeSet(SET_OF_500TASKS_20USERS_SOLUTION.resource());
    }

    @Test
    public void assignTaskProblemFactChangeUserNotFound() throws Exception {
        TaskAssigningSolution<?> solution = readTaskAssigningSolution(SET_OF_24TASKS_8USERS_SOLUTION.resource());
        Task task = solution.getTaskList().get(0);
        User user = new User(-12345, "Non Existing");
        Assertions.assertThatThrownBy(() -> executeSequentialChanges(solution, Collections.singletonList(new ProgrammedAssignTaskProblemFactChange(task, user))))
                .hasMessage(String.format("Expected user: " + user + " was not found in current working solution", user));
    }

    private void assignTaskProblemFactChangeFixedChangeSet(String solutionResource) throws Exception {
        TaskAssigningSolution<?> solution = readTaskAssigningSolution(solutionResource);
        solution.getUserList().add(PLANNING_USER);

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

        assignTaskProblemFactChange(solution, solutionResource, FIXED_TEST, programmedChanges);
    }

    private void assignTaskProblemFactChangeRandomChangeSet(String solutionResource) throws Exception {
        TaskAssigningSolution<?> solution = readTaskAssigningSolution(solutionResource);
        solution.getUserList().add(PLANNING_USER);

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
        assignTaskProblemFactChange(solution, solutionResource, RANDOM_TEST, programmedChanges);
    }

    private void assignTaskProblemFactChange(TaskAssigningSolution<?> solution,
                                             String solutionResource,
                                             String testType,
                                             List<ProgrammedAssignTaskProblemFactChange> programmedChanges) throws Exception {
        TaskAssigningSolution<?> initialSolution = executeSequentialChanges(solution, programmedChanges);
        if (writeTestFiles()) {
            writeProblemFactChangesTestFiles(initialSolution,
                                             solutionResource,
                                             "AssignTaskProblemFactChangeTest.assignTaskProblemFactChangeTest",
                                             testType,
                                             programmedChanges,
                                             ProgrammedAssignTaskProblemFactChange::workingSolutionBeforeChangeAsString,
                                             ProgrammedAssignTaskProblemFactChange::solutionAfterChangeAsString);
        }

        //each partial solution must have the change that was applied on it.
        for (ProgrammedAssignTaskProblemFactChange change : programmedChanges) {
            assertAssignTaskProblemFactChangeWasProduced(change.getChange(), change.getSolutionAfterChange());
        }

        //finally the last solution must have the result of all the changes.
        TaskAssigningSolution<?> lastSolution = programmedChanges.get(programmedChanges.size() - 1).getSolutionAfterChange();
        Map<Long, AssignTaskProblemFactChange> summarizedChanges = new HashMap<>();
        programmedChanges.forEach(change -> {
            //if  task was changed multiple times record only the last change.
            summarizedChanges.put(change.getChange().getTask().getId(), change.getChange());
        });
        for (AssignTaskProblemFactChange change : summarizedChanges.values()) {
            assertAssignTaskProblemFactChangeWasProduced(change, lastSolution);
        }
    }

    /**
     * Given an AssignTaskProblemFactChange and a solution that was produced as the result of applying the change,
     * asserts that the assignment defined by the change is not violated (exists in) by the solution.
     * The assignment defined in the change must also be pinned in the produced solution as well as any other
     * previous assignment for the given user.
     * @param change The change that was executed for producing the solution.
     * @param solution The produced solution.
     */
    private void assertAssignTaskProblemFactChangeWasProduced(AssignTaskProblemFactChange change, TaskAssigningSolution<?> solution) throws Exception {
        User internalUser = solution.getUserList().stream()
                .filter(user -> Objects.equals(user.getId(), change.getUser().getId()))
                .findFirst().orElseThrow(() -> new Exception("User: " + change.getUser() + " was not found in solution."));

        Task internalTask = solution.getTaskList().stream()
                .filter(task -> Objects.equals(task.getId(), change.getTask().getId()))
                .findFirst().orElseThrow(() -> new Exception("Task: " + change + " was not found in solution."));
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
}
