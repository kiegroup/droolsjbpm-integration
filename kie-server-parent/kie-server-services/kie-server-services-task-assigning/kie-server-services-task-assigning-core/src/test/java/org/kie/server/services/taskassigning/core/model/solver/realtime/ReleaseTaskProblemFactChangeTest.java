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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.junit.Assert.assertEquals;
import static org.kie.server.services.taskassigning.core.model.Task.PREVIOUS_TASK_OR_USER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReleaseTaskProblemFactChangeTest {

    private Task task;
    private TaskOrUser previousTaskOrUser;
    private Task workingTask;
    private Task nextTask;

    @Mock
    private ScoreDirector<TaskAssigningSolution<?>> scoreDirector;

    private ReleaseTaskProblemFactChange change;

    @Before
    public void setUp() {
        task = new Task();
        previousTaskOrUser = spy(new Task());
        workingTask = spy(new Task());
        nextTask = spy(new Task());
        change = new ReleaseTaskProblemFactChange(task);
    }

    @Test
    public void doChangeNonPinnedTask() {
        workingTask.setPinned(false);
        doChange();
        verifyCommonResults();
    }

    @Test
    public void doChangePinnedTask() {
        workingTask.setPinned(true);
        doChange();
        verifyCommonResults();
        verify(scoreDirector).beforeProblemPropertyChanged(workingTask);
        verify(workingTask).setPinned(false);
        verify(scoreDirector).afterProblemPropertyChanged(workingTask);
    }

    @Test
    public void doChangeUnAssignedTask() {
        workingTask.setPreviousTaskOrUser(null);
        when(scoreDirector.lookUpWorkingObjectOrReturnNull(task)).thenReturn(workingTask);
        change.doChange(scoreDirector);
        verify(scoreDirector, never()).beforeVariableChanged(any(Task.class), anyString());
        verify(scoreDirector, never()).afterVariableChanged(any(Task.class), anyString());
        verify(scoreDirector, never()).beforeProblemPropertyChanged(any());
        verify(scoreDirector, never()).afterProblemPropertyChanged(any());
        verify(scoreDirector, never()).triggerVariableListeners();
    }

    @Test
    public void getTask() {
        assertEquals(task, change.getTask());
    }

    private void doChange() {
        workingTask.setPreviousTaskOrUser(previousTaskOrUser);
        workingTask.setNextTask(nextTask);
        nextTask.setPreviousTaskOrUser(workingTask);

        when(scoreDirector.lookUpWorkingObjectOrReturnNull(task)).thenReturn(workingTask);
        change.doChange(scoreDirector);
    }

    private void verifyCommonResults() {
        assertEquals(previousTaskOrUser, nextTask.getPreviousTaskOrUser());
        verify(scoreDirector).beforeVariableChanged(nextTask, PREVIOUS_TASK_OR_USER);
        verify(scoreDirector).afterVariableChanged(nextTask, PREVIOUS_TASK_OR_USER);
        verify(scoreDirector).triggerVariableListeners();
    }
}
