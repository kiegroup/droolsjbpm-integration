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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class RemoveTaskProblemFactChangeTest extends BaseProblemFactChangeTest {

    @Test
    public void removeTaskProblemFactChange24Tasks5UsersTest() throws Exception {
        removeTaskProblemFactChangeTest(_24TASKS_8USERS_SOLUTION, Arrays.asList(0L, 10L, 11L, 4L, 20L, 100L, 78L));
    }

    @Test
    public void removeTaskProblemFactChange24Tasks5UsersRandomTest() throws Exception {
        removeTaskProblemFactChangeRandomSetTest(_24TASKS_8USERS_SOLUTION);
    }

    @Test
    public void removeTaskProblemFactChange50Tasks50UsersTest() throws Exception {
        removeTaskProblemFactChangeTest(_50TASKS_5USERS_SOLUTION, Arrays.asList(0L, 10L, 11L, 4L, 20L, 30L, 35L, 40L, 45L, 57L, 60L));
    }

    @Test
    public void removeTaskProblemFactChange50Tasks50UsersRandomTest() throws Exception {
        removeTaskProblemFactChangeRandomSetTest(_50TASKS_5USERS_SOLUTION);
    }

    @Test
    public void removeTaskProblemFactChange100Tasks5UsersTest() throws Exception {
        removeTaskProblemFactChangeTest(_100TASKS_5USERS_SOLUTION, Arrays.asList(5L, 15L, 11L, 4L, 20L, 30L, 36L, 40L, 45L, 58L, 99L, 130L, 200L));
    }

    @Test
    public void removeTaskProblemFactChange100Tasks5UsersRandomTest() throws Exception {
        removeTaskProblemFactChangeRandomSetTest(_100TASKS_5USERS_SOLUTION);
    }

    @Test
    public void removeTaskProblemFactChange500Tasks5UsersTest() throws Exception {
        removeTaskProblemFactChangeTest(_500TASKS_20USERS_SOLUTION, Arrays.asList(5L, 15L, 11L, 4L, 20L, 30L, 36L, 40L, 45L, 58L, 99L, 300L, 400L, 25L, 1000L, 1001L));
    }

    @Test
    public void removeTaskProblemFactChange500Tasks5UsersRandomTest() throws Exception {
        removeTaskProblemFactChangeRandomSetTest(_500TASKS_20USERS_SOLUTION);
    }

    private void removeTaskProblemFactChangeTest(String solutionResource, List<Long> taskIds) throws Exception {
        removeTaskProblemFactChangeTest(readTaskAssigningSolution(solutionResource), taskIds);
    }

    private void removeTaskProblemFactChangeTest(TaskAssigningSolution solution, List<Long> taskIds) throws Exception {
        List<ProgrammedProblemFactChange<RemoveTaskProblemFactChange>> programmedChanges = taskIds.stream()
                .map(id -> findTaskOrCreate(solution, id))
                .map(task -> new ProgrammedProblemFactChange<>(new RemoveTaskProblemFactChange(task)))
                .collect(Collectors.toList());

        //each partial solution must have the change that was applied on it.
        executeSequentialChanges(solution, programmedChanges);
        programmedChanges.forEach(change -> assertRemoveTaskProblemFactChangeWasProduced(change.getChange(), change.getSolutionAfterChange()));

        //finally the last solution must have the result of all the changes.
        TaskAssigningSolution lastSolution = programmedChanges.get(programmedChanges.size() - 1).getSolutionAfterChange();
        programmedChanges.forEach(change -> assertRemoveTaskProblemFactChangeWasProduced(change.getChange(), lastSolution));
    }

    private void removeTaskProblemFactChangeRandomSetTest(String solutionResource) throws Exception {
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);
        int taskCount = solution.getTaskList().size();
        int randomChanges = taskCount / 2 + random.nextInt(taskCount / 2);
        List<Long> taskIds = new ArrayList<>();
        for (int i = 0; i < randomChanges; i++) {
            taskIds.add((long) taskCount++);
        }
        removeTaskProblemFactChangeTest(solution, taskIds);
    }

    /**
     * Given a RemoveTaskProblemFact change and a solution that was produced as the result of applying the change,
     * asserts that the pointed task is not present in the solution.
     * @param change The change that was executed for producing the solution.
     * @param solution The produced solution.
     */
    private void assertRemoveTaskProblemFactChangeWasProduced(RemoveTaskProblemFactChange change, TaskAssigningSolution solution) {
        assertFalse(solution.getTaskList().stream().anyMatch(task -> Objects.equals(change.getTask().getId(), task.getId())));
    }

    private static Task findTaskOrCreate(TaskAssigningSolution solution, long id) {
        return solution.getTaskList().stream()
                .filter(task -> Objects.equals(task.getId(), id))
                .findFirst().orElse(new Task(id, "NonExisting_" + id, 1));
    }
}
