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

package org.kie.server.services.taskassigning.core.model.solver;

import java.util.List;

import org.kie.server.services.taskassigning.core.AbstractTaskAssigningCoreTest;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.junit.Test;
import org.optaplanner.core.api.solver.Solver;

import static org.kie.server.services.taskassigning.core.TestDataSet.SET_OF_24TASKS_8USERS_SOLUTION;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.extractTaskList;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.isPotentialOwner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSolver extends AbstractTaskAssigningCoreTest {

    private static final long TEST_TIMEOUT = 20000;

    @Test(timeout = TEST_TIMEOUT)
    public void startSolverAndSolution24Tasks8Users() throws Exception {
        testSolverStartAndSolution(1, SET_OF_24TASKS_8USERS_SOLUTION.resource());
    }

    /**
     * Tests that solver for the tasks assigning problem definition can be properly started, a solution can be produced,
     * and that some minimal constrains are met by de solution.
     */
    private void testSolverStartAndSolution(int stepCountLimit, String solutionResource) throws Exception {
        Solver<TaskAssigningSolution> solver = createNonDaemonSolver(stepCountLimit);
        TaskAssigningSolution solution = readTaskAssigningSolution(solutionResource);
        solution.getUserList().add(PLANNING_USER);
        TaskAssigningSolution result = solver.solve(solution);
        if (!result.getScore().isFeasible()) {
            fail(String.format("With current problem definition and stepCountLimit of %s it's expected " +
                                       "that a feasible solution has been produced.", stepCountLimit));
        }
        assertConstraints(result);
    }

    /**
     * Given a TaskAssigningSolution asserts the following constraints.
     * <p>
     * 1) All tasks are assigned to a user
     * 2) The assigned user for a task is a potentialOwner for the task or the PLANNING_USER
     * 3) All tasks are assigned.
     * @param solution a solution.
     */
    private void assertConstraints(TaskAssigningSolution solution) {
        int totalTasks = 0;
        for (User user : solution.getUserList()) {
            List<Task> taskList = extractTaskList(user);
            totalTasks += taskList.size();
            taskList.forEach(task -> assertAssignment(user, task, solution.getUserList()));
        }
        assertEquals(solution.getTaskList().size(), totalTasks);
    }

    private void assertAssignment(User user, Task task, List<User> availableUsers) {
        assertNotNull(task.getUser());
        assertEquals("Task is not assigned to expected user", user.getEntityId(), task.getUser().getEntityId());
        if (task.getPotentialOwners() == null || task.getPotentialOwners().isEmpty()) {
            assertEquals("Task without potentialOwners can only be assigned to the PLANNING_USER", PLANNING_USER.getEntityId(), user.getEntityId());
        } else if (PLANNING_USER.getEntityId().equals(user.getEntityId())) {
            availableUsers.forEach(availableUser -> {
                assertFalse(String.format("PLANNING_USER user was assigned but another potential owner was found. user: %s task: %s", user, task), isPotentialOwner(task, user));
            });
        } else {
            assertTrue(String.format("User: %s is not a potential owner for task: %s", user, task), isPotentialOwner(task, user));
        }
    }
}