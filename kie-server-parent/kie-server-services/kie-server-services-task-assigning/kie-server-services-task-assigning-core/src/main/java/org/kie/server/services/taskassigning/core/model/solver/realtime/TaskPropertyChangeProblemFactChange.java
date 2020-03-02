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

public class TaskPropertyChangeProblemFactChange implements ProblemFactChange<TaskAssigningSolution> {

    private Task task;

    private String status;

    private Integer priority;

    public TaskPropertyChangeProblemFactChange(Task task) {
        this.task = task;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Task getTask() {
        return task;
    }

    @Override
    public void doChange(ScoreDirector<TaskAssigningSolution> scoreDirector) {
        Task workingTask = scoreDirector.lookUpWorkingObjectOrReturnNull(task);
        if (workingTask == null) {
            throw new TaskAssigningRuntimeException(String.format("Expected task: %s was not found in current working solution", task));
        }
        if (priority != null) {
            scoreDirector.beforeProblemPropertyChanged(workingTask);
            workingTask.setPriority(priority);
            scoreDirector.afterProblemPropertyChanged(workingTask);
        }
        if (status != null) {
            scoreDirector.beforeProblemPropertyChanged(workingTask);
            workingTask.setStatus(status);
            scoreDirector.afterProblemPropertyChanged(workingTask);
        }
        scoreDirector.triggerVariableListeners();
    }
}
