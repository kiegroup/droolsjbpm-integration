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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.core.model.Task.PREVIOUS_TASK_OR_USER;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockTask;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockUser;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProblemFactChangeUtilTest {

    private static final String USER_ID = "USER_ID";
    private static final long TASK_ID1 = 1;
    private static final long TASK_ID2 = 2;
    private static final long TASK_ID3 = 3;
    private static final long TASK_ID4 = 4;
    private static final long TASK_ID5 = 5;

    @Mock
    private ScoreDirector<TaskAssigningSolution> scoreDirector;

    private User user;

    private List<Task> userTasks;

    @Before
    public void setUp() {
        userTasks = Arrays.asList(mockTask(TASK_ID1, true),
                                  mockTask(TASK_ID2, true),
                                  mockTask(TASK_ID3, false),
                                  mockTask(TASK_ID4, false),
                                  mockTask(TASK_ID5, false));
        user = mockUser(USER_ID, userTasks);
    }

    @Test
    public void releaseAllTasks() {
        ProblemFactChangeUtil.releaseAllTasks(user, scoreDirector);
        userTasks.forEach(task -> assertTaskWasReleased(task, scoreDirector));
    }

    @Test
    public void releaseNonPinnedTasks() {
        ProblemFactChangeUtil.releaseNonPinnedTasks(user, scoreDirector);
        userTasks.stream().filter(Task::isPinned).forEach(task -> assertTaskWasNotReleased(task, scoreDirector));
        userTasks.stream().filter(task -> !task.isPinned()).forEach(task -> assertTaskWasReleased(task, scoreDirector));
    }

    public static void assertTaskWasReleased(Task task, ScoreDirector<TaskAssigningSolution> scoreDirector) {
        verify(scoreDirector).beforeProblemPropertyChanged(task);
        assertThat(task.isPinned()).as("Invalid pinned status for task: %s", task.getInputData()).isFalse();
        verify(scoreDirector).afterProblemPropertyChanged(task);
        verify(scoreDirector).beforeVariableChanged(task, PREVIOUS_TASK_OR_USER);
        assertThat(task.getPreviousTaskOrUser()).as("Invalid previousTaskOrUser for task: %s", task.getId()).isNull();
        verify(scoreDirector).afterVariableChanged(task, PREVIOUS_TASK_OR_USER);
    }

    public static void assertTaskWasNotReleased(Task task, ScoreDirector<TaskAssigningSolution> scoreDirector) {
        verify(scoreDirector, never()).beforeProblemPropertyChanged(task);
        verify(scoreDirector, never()).afterProblemPropertyChanged(task);
        verify(scoreDirector, never()).beforeVariableChanged(task, PREVIOUS_TASK_OR_USER);
        verify(scoreDirector, never()).afterVariableChanged(task, PREVIOUS_TASK_OR_USER);
    }
}
