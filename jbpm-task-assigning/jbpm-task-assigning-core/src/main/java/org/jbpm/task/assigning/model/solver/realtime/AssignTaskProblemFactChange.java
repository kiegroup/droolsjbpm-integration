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

import org.jbpm.task.assigning.TaskAssigningRuntimeException;
import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.jbpm.task.assigning.model.User;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import static org.jbpm.task.assigning.model.Task.PREVIOUS_TASK_OR_USER;

/**
 * Implements the "direct" assignment of an existing Task to a User.
 * This PFC can be useful in scenarios were e.g. a system administrator manually assigns a Task to a given user from the
 * jBPM tasks list administration. While it's expected that environments that relied the tasks assigning to OptaPlanner
 * shouldn't do this "direct" assignments, we still provide this PFC for dealing with this edge case scenarios.
 * Note that this use cases might break hard constraints or introduce considerable score penalization for soft
 * constraints.
 * Additionally since the "direct" assignment comes from an "external" system it'll remain pinned.
 * <p>
 * Both the task and user to work with are looked up by using their corresponding id's. If the task is not found it'll
 * be created and added to the working solution, while if the user is not found and exception will be thrown.
 */
public class AssignTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;
    private User user;

    public AssignTaskProblemFactChange(Task task, User user) {
        this.task = task;
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        TaskAssigningSolution solution = scoreDirector.getWorkingSolution();

        User workingUser = scoreDirector.lookUpWorkingObjectOrReturnNull(user);
        if (workingUser == null) {
            throw new TaskAssigningRuntimeException(String.format("Expected user: %s was not found in current working solution", user));
        }

        Task workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);

        TaskOrUser insertPosition = findInsertPosition(workingUser);
        Task insertPositionNextTask = insertPosition.getNextTask();
        boolean isNew = false;

        if (workingTask == null) {
            // The task will be created by this PFC.
            // ensure that the task to be added doesn't have any out-side manually assigned values for the values that
            // are calculated by OptaPlanner
            task.setPreviousTaskOrUser(null);
            task.setUser(null);
            task.setPinned(false);
            task.setNextTask(null);
            task.setStartTime(null);
            task.setEndTime(null);
            workingTask = task;
            isNew = true;
        }

        if (insertPosition == workingTask) {
            //nothing to do, the task is already pinned and belongs to user. (see findInsertPosition)
        } else if (insertPosition.getNextTask() == workingTask) {
            //the task is already in the correct position but not pinned. (see findInsertPosition)
            scoreDirector.beforeProblemPropertyChanged(workingTask);
            workingTask.setPinned(true);
            scoreDirector.afterProblemPropertyChanged(workingTask);
            scoreDirector.triggerVariableListeners();
        } else {
            //the task needs to be re-positioned, might belong to user or not.
            if (workingTask.getPreviousTaskOrUser() != null) {
                //un-link the task from his previous chain/position.
                TaskOrUser previousTaskOrUser = workingTask.getPreviousTaskOrUser();
                Task nextTask = workingTask.getNextTask();
                if (nextTask != null) {
                    //re-link the chain where the workingTask belonged if any
                    scoreDirector.beforeVariableChanged(nextTask, PREVIOUS_TASK_OR_USER);
                    nextTask.setPreviousTaskOrUser(previousTaskOrUser);
                    scoreDirector.afterVariableChanged(nextTask, PREVIOUS_TASK_OR_USER);
                }
            }

            if (isNew) {
                workingTask.setPreviousTaskOrUser(insertPosition);
                scoreDirector.beforeEntityAdded(workingTask);
                // Planning entity lists are already cloned by the SolutionCloner, no need to clone.
                solution.getTaskList().add(workingTask);
                scoreDirector.afterEntityAdded(workingTask);
            } else {
                scoreDirector.beforeVariableChanged(workingTask, PREVIOUS_TASK_OR_USER);
                workingTask.setPreviousTaskOrUser(insertPosition);
                scoreDirector.afterVariableChanged(workingTask, PREVIOUS_TASK_OR_USER);
            }

            if (insertPositionNextTask != null) {
                scoreDirector.beforeVariableChanged(insertPositionNextTask, PREVIOUS_TASK_OR_USER);
                insertPositionNextTask.setPreviousTaskOrUser(workingTask);
                scoreDirector.afterVariableChanged(insertPositionNextTask, PREVIOUS_TASK_OR_USER);
            }

            scoreDirector.beforeProblemPropertyChanged(workingTask);
            workingTask.setPinned(true);
            scoreDirector.afterProblemPropertyChanged(workingTask);
            scoreDirector.triggerVariableListeners();
        }
    }

    /**
     * Find the first available "position" where a task can be added in the tasks chain for a given user.
     * <p>
     * For a chain like:
     * <p>
     * U -> T1 -> T2 -> T3 -> T4 -> null
     * <p>
     * if e.g. T3 is returned, a new task Tn will be later added in the following position.
     * <p>
     * U -> T1 -> T2 -> T3 -> Tn -> T4 -> null
     * Given that we are using a chained structure, to pin a task Tn to a given user, we must be sure that all the
     * previous tasks in the chain are pinned to the same user. For keeping the structure consistency a task Tn is
     * inserted after the last pinned chain. In the example above we have that existing tasks T1, T2 and T3 are pinned.
     * @param user the for adding a task to.
     * @return the proper TaskOrUser object were a task can be added. This method will never return null.
     */
    private TaskOrUser findInsertPosition(User user) {
        TaskOrUser result = user;
        Task nextTask = user.getNextTask();
        while (nextTask != null && nextTask.isPinned()) {
            result = nextTask;
            nextTask = nextTask.getNextTask();
        }
        return result;
    }
}
