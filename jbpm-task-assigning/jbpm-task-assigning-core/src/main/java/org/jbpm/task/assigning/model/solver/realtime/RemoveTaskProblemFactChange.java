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

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskAssigningSolution;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Implements the removal of a Task from the working solution. If a task with the given identifier not exists it does
 * no action.
 */
public class RemoveTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;

    public RemoveTaskProblemFactChange(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        TaskAssigningSolution solution = scoreDirector.getWorkingSolution();
        Task workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);
        if (workingTask != null) {
            TaskOrUser previousTaskOrUser = workingTask.getPreviousTaskOrUser();
            Task nextTask = workingTask.getNextTask();
            if (nextTask != null) {
                scoreDirector.beforeVariableChanged(nextTask, "previousTaskOrUser");
                nextTask.setPreviousTaskOrUser(previousTaskOrUser);
                scoreDirector.afterVariableChanged(nextTask, "previousTaskOrUser");
            }
            scoreDirector.beforeEntityRemoved(workingTask);
            // Planning entity lists are already cloned by the SolutionCloner, no need to clone.
            solution.getTaskList().remove(workingTask);
            scoreDirector.afterEntityRemoved(workingTask);
            scoreDirector.triggerVariableListeners();
        }
    }
}
