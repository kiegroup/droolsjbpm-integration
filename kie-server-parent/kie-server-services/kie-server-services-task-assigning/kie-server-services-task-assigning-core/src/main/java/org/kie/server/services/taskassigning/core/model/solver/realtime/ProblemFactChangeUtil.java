/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.List;

import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.kie.server.services.taskassigning.core.model.Task.PREVIOUS_TASK_OR_USER;
import static org.kie.server.services.taskassigning.core.model.solver.TaskHelper.extractTasks;

public class ProblemFactChangeUtil {

    private ProblemFactChangeUtil() {
    }

    /**
     * Releases all the tasks linked to a given user.
     * @param workingUser a user instance to get the tasks from. Important! the user must belong to the solution
     * currently managed by the scoreDirector, i.e. the scoreDirector.getWorkingSolution().
     * @param scoreDirector a scoreDirector instance for executing the required beforeVariableChanged and
     * afterVariableChanged methods.
     */
    public static void releaseAllTasks(User workingUser, ScoreDirector<TaskAssigningSolution> scoreDirector) {
        releaseTasks(workingUser, true, scoreDirector);
    }

    /**
     * Releases all the non-pinned tasks linked to a given user.
     * @param workingUser a user instance to get the tasks from. Important! the user must belong to the solution
     * currently managed by the scoreDirector, i.e. the scoreDirector.getWorkingSolution().
     * @param scoreDirector a scoreDirector instance for executing the required beforeVariableChanged and
     * afterVariableChanged methods.
     */
    public static void releaseNonPinnedTasks(User workingUser, ScoreDirector<TaskAssigningSolution> scoreDirector) {
        releaseTasks(workingUser, false, scoreDirector);
    }

    /**
     * Release the tasks associated previously associated by OptaPlanner to a user.
     * note: Optimizes the generated graph e.g. User <-> T1 <-> T2 <-> T3 <-> T4 navigation and structure changing
     * by iterating in reverse order.
     * @param workingUser a user instance previously populated by OptaPlanner.
     * @param includePinnedTasks true if the pinned tasks must also be released, false if only non pinned tasks must
     * be released.
     * @param scoreDirector a scored director instance for notifying the changes.
     */
    private static void releaseTasks(User workingUser, boolean includePinnedTasks, ScoreDirector<TaskAssigningSolution> scoreDirector) {
        final List<Task> tasks = extractTasks(workingUser, testedTask -> includePinnedTasks || !testedTask.isPinned());
        Task task;
        for (int index = tasks.size() - 1; index >= 0; index--) {
            task = tasks.get(index);
            scoreDirector.beforeProblemPropertyChanged(task);
            task.setPinned(false);
            scoreDirector.afterProblemPropertyChanged(task);
            scoreDirector.beforeVariableChanged(task, PREVIOUS_TASK_OR_USER);
            task.setPreviousTaskOrUser(null);
            scoreDirector.afterVariableChanged(task, PREVIOUS_TASK_OR_USER);
        }
    }
}