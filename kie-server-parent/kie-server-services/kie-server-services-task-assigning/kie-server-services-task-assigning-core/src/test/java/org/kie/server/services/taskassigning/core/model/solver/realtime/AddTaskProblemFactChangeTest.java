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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;

import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_100TASKS_5USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_24TASKS_8USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_500TASKS_20USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_50TASKS_5USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER;

public class AddTaskProblemFactChangeTest extends AbstractProblemFactChangeTest {

    @Test
    public void addTaskProblemFactChange24Tasks8Users() throws Exception {
        addTaskProblemFactChange(SET_OF_24TASKS_8USERS_SOLUTION.resource(), Arrays.asList(24L, 25L, 30L, 40L));
    }

    @Test
    public void addTaskProblemFactChange24Tasks8UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        addTaskProblemFactChangeRandomSet(SET_OF_24TASKS_8USERS_SOLUTION.resource());
    }

    @Test
    public void addTaskProblemFactChange50Tasks5Users() throws Exception {
        checkRunTurtleTests();
        addTaskProblemFactChange(SET_OF_50TASKS_5USERS_SOLUTION.resource(), Arrays.asList(50L, 520L, 70L, 85L, 100L));
    }

    @Test
    public void addTaskProblemFactChange50Tasks5UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        addTaskProblemFactChangeRandomSet(SET_OF_50TASKS_5USERS_SOLUTION.resource());
    }

    @Test
    public void addTaskProblemFactChange100Tasks5Users() throws Exception {
        checkRunTurtleTests();
        addTaskProblemFactChange(SET_OF_100TASKS_5USERS_SOLUTION.resource(), Arrays.asList(100L, 105L, 200L, 350L));
    }

    @Test
    public void addTaskProblemFactChange100Tasks5UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        addTaskProblemFactChangeRandomSet(SET_OF_100TASKS_5USERS_SOLUTION.resource());
    }

    @Test
    public void addTaskProblemFactChange500Tasks20Users() throws Exception {
        checkRunTurtleTests();
        addTaskProblemFactChange(SET_OF_500TASKS_20USERS_SOLUTION.resource(), Arrays.asList(500L, 600L, 700L));
    }

    @Test
    public void addTaskProblemFactChange500Tasks20UsersRandom() throws Exception {
        checkRunDevelopmentOnlyTests();
        addTaskProblemFactChangeRandomSet(SET_OF_500TASKS_20USERS_SOLUTION.resource());
    }

    @Test
    public void addTaskProblemFactChangeTaskAlreadyExists() throws Exception {
        TaskAssigningSolution<?> solution = readTaskAssigningSolution(SET_OF_24TASKS_8USERS_SOLUTION.resource());
        long taskId = 20; //randomly selected task.
        Task task = new Task(taskId, null, 1);
        Assertions.assertThatThrownBy(() -> executeSequentialChanges(solution,
                                                                     Collections.singletonList(new ProgrammedProblemFactChange<>(new AddTaskProblemFactChange(task)))))
                .hasMessage(String.format("A task with the given identifier id: " + taskId + " already exists", taskId));
    }

    private void addTaskProblemFactChangeRandomSet(String solutionResource) throws Exception {
        TaskAssigningSolution<?> solution = readTaskAssigningSolution(solutionResource);
        int taskCount = solution.getTaskList().size();
        int randomChanges = taskCount / 2 + random.nextInt(taskCount / 2);
        List<Long> taskIds = new ArrayList<>();
        for (int i = 0; i < randomChanges; i++) {
            taskIds.add((long) taskCount++);
        }
        addTaskProblemFactChange(solution, taskIds);
    }

    private void addTaskProblemFactChange(TaskAssigningSolution<?> solution, List<Long> taskIds) throws Exception {
        solution.getUserList().add(PLANNING_USER);
        List<ProgrammedProblemFactChange<AddTaskProblemFactChange>> programmedChanges = taskIds.stream()
                .map(id -> new ProgrammedProblemFactChange<>(new AddTaskProblemFactChange(new Task(id, "NewTask_" + id, 1))))
                .collect(Collectors.toList());

        //each partial solution must have the change that was applied on it.
        executeSequentialChanges(solution, programmedChanges);
        programmedChanges.forEach(change -> assertAddTaskProblemFactChangeWasProduced(change.getChange(), change.getSolutionAfterChange()));

        //finally the last solution must have the result of all the changes.
        TaskAssigningSolution<?> lastSolution = programmedChanges.get(programmedChanges.size() - 1).getSolutionAfterChange();
        programmedChanges.forEach(change -> assertAddTaskProblemFactChangeWasProduced(change.getChange(), lastSolution));
    }

    private void addTaskProblemFactChange(String solutionResource, List<Long> taskIds) throws Exception {
        addTaskProblemFactChange(readTaskAssigningSolution(solutionResource), taskIds);
    }

    private void assertAddTaskProblemFactChangeWasProduced(AddTaskProblemFactChange change, TaskAssigningSolution<?> solution) {
        assertTrue(solution.getTaskList().stream().anyMatch(task -> Objects.equals(change.getTask().getId(), task.getId())));
    }
}
