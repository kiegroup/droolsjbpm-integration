/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.task.assigning.model.solver;

import java.util.Objects;

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.TaskOrUser;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.jbpm.task.assigning.model.Task.END_TIME;
import static org.jbpm.task.assigning.model.Task.START_TIME;

/**
 * Given a chained graph:
 * <p>
 * User1 <-> Task1 <-> Task2 <-> sourceTask <-> Task4 <-> Task5 -> null
 * <p>
 * keeps the startTime and endTime of the tasks in the chain updated when any of the tasks in the chain changes.
 * e.g. when sourceTask changes, the startTime and endTime of tasks {sourceTask, Task4, Task5} is recalculated
 * accordingly.
 */
public class StartAndEndTimeUpdatingVariableListener implements VariableListener<Task> {

    @Override
    public void beforeEntityAdded(final ScoreDirector scoreDirector, final Task task) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(final ScoreDirector scoreDirector, final Task task) {
        updateStartAndEndTime(scoreDirector, task);
    }

    @Override
    public void beforeVariableChanged(final ScoreDirector scoreDirector, final Task task) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(final ScoreDirector scoreDirector, final Task task) {
        updateStartAndEndTime(scoreDirector, task);
    }

    @Override
    public void beforeEntityRemoved(final ScoreDirector scoreDirector, final Task task) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, Task task) {
        // Do nothing
    }

    private static void updateStartAndEndTime(final ScoreDirector scoreDirector, final Task sourceTask) {
        TaskOrUser previous = sourceTask.getPreviousTaskOrUser();
        Task shadowTask = sourceTask;
        Integer previousEndTime = previous == null ? null : previous.getEndTime();
        Integer startTime = previousEndTime;
        Integer endTime = calculateEndTime(shadowTask, startTime);
        while (shadowTask != null && !Objects.equals(shadowTask.getStartTime(), startTime)) {
            scoreDirector.beforeVariableChanged(shadowTask, START_TIME);
            shadowTask.setStartTime(startTime);
            scoreDirector.afterVariableChanged(shadowTask, START_TIME);

            scoreDirector.beforeVariableChanged(shadowTask, END_TIME);
            shadowTask.setEndTime(endTime);
            scoreDirector.afterVariableChanged(shadowTask, END_TIME);

            previousEndTime = shadowTask.getEndTime();
            shadowTask = shadowTask.getNextTask();
            startTime = previousEndTime;
            endTime = calculateEndTime(shadowTask, startTime);
        }
    }

    private static Integer calculateEndTime(final Task shadowTask, final Integer startTime) {
        if (startTime == null || shadowTask == null) {
            return 0;
        }
        return startTime + shadowTask.getDuration();
    }
}
