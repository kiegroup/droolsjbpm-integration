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

import org.kie.server.services.taskassigning.core.TaskAssigningRuntimeException;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

/**
 * Adds a Task to the working solution. If a task with the given identifier already exists an exception is thrown.
 */
public class AddTaskProblemFactChange implements ProblemFactChange<TaskAssigningSolution<?>> {

    private Task task;

    public AddTaskProblemFactChange(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution<?>> scoreDirector) {
        TaskAssigningSolution<?> solution = scoreDirector.getWorkingSolution();
        Task workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);
        if (workingTask != null) {
            throw new TaskAssigningRuntimeException(String.format("A task with the given identifier id: %s already exists", task.getId()));
        }
        scoreDirector.beforeEntityAdded(task);
        // Planning entity lists are already cloned by the SolutionCloner, no need to clone.
        solution.getTaskList().add(task);
        scoreDirector.afterEntityAdded(task);
        scoreDirector.triggerVariableListeners();
    }
}
