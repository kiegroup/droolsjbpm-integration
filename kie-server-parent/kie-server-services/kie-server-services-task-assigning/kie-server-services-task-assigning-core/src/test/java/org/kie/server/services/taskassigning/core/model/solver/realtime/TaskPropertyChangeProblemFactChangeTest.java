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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskPropertyChangeProblemFactChangeTest {

    private static final Integer NEW_PRIORITY = 1;

    private static final String NEW_STATUS = "InProgress";

    @Mock
    private ScoreDirector<TaskAssigningSolution> scoreDirector;

    @Mock
    private Task workingTask;

    @Mock
    private Task task;

    private TaskPropertyChangeProblemFactChange change;

    @Before
    public void setUp() {
        when(scoreDirector.lookUpWorkingObjectOrReturnNull(task)).thenReturn(workingTask);
        change = new TaskPropertyChangeProblemFactChange(task);
    }

    @Test
    public void getStatus() {
        change.setStatus(NEW_STATUS);
        assertEquals(NEW_STATUS, change.getStatus());
    }

    @Test
    public void getPriority() {
        change.setPriority(NEW_PRIORITY);
        assertEquals(NEW_PRIORITY, change.getPriority());
    }

    @Test
    public void getTask() {
        assertEquals(task, change.getTask());
    }

    @Test
    public void doChangeOnlyPriority() {
        change.setPriority(NEW_PRIORITY);
        change.doChange(scoreDirector);
        verify(scoreDirector, times(1)).beforeProblemPropertyChanged(workingTask);
        verify(scoreDirector, times(1)).afterProblemPropertyChanged(workingTask);
        verify(workingTask).setPriority(NEW_PRIORITY);
        verify(workingTask, never()).setStatus(NEW_STATUS);
        verify(scoreDirector).triggerVariableListeners();
    }

    @Test
    public void doChangeOnlyStatus() {
        change.setStatus(NEW_STATUS);
        change.doChange(scoreDirector);
        verify(scoreDirector, times(1)).beforeProblemPropertyChanged(workingTask);
        verify(scoreDirector, times(1)).afterProblemPropertyChanged(workingTask);
        verify(workingTask).setStatus(NEW_STATUS);
        verify(workingTask, never()).setPriority(NEW_PRIORITY);
        verify(scoreDirector).triggerVariableListeners();
    }

    @Test
    public void doChangeStatusAndPriority() {
        change.setPriority(NEW_PRIORITY);
        change.setStatus(NEW_STATUS);
        change.doChange(scoreDirector);
        verify(scoreDirector, times(2)).beforeProblemPropertyChanged(workingTask);
        verify(scoreDirector, times(2)).afterProblemPropertyChanged(workingTask);
        verify(workingTask).setStatus(NEW_STATUS);
        verify(workingTask).setPriority(NEW_PRIORITY);
        verify(scoreDirector).triggerVariableListeners();
    }

    @Test
    public void doChangeFailure() {
        when(scoreDirector.lookUpWorkingObjectOrReturnNull(task)).thenReturn(null);
        TaskPropertyChangeProblemFactChange change = new TaskPropertyChangeProblemFactChange(task);

        Assertions.assertThatThrownBy(() -> change.doChange(scoreDirector))
                .hasMessageContaining("was not found in current working solution");
    }
}
