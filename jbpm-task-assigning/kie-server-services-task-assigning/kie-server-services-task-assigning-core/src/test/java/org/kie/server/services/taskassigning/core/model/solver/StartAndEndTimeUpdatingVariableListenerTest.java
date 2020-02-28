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

import java.util.stream.Stream;

import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;
import org.kie.server.services.taskassigning.core.model.solver.StartAndEndTimeUpdatingVariableListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StartAndEndTimeUpdatingVariableListenerTest {

    @Mock
    private ScoreDirector scoreDirector;

    private User anchor;

    private StartAndEndTimeUpdatingVariableListener listener;

    private Task task1;
    private Task task2;
    private Task task3;
    private Task task4;
    private Task task5;

    @Before
    public void setUp() {
        listener = new StartAndEndTimeUpdatingVariableListener();

        anchor = new User(1, "User1");
        task1 = new Task(1, "Task1", 1);
        task1.setStartTimeInMinutes(anchor.getEndTimeInMinutes());
        task1.setDurationInMinutes(1);
        task1.setEndTime(task1.getStartTimeInMinutes() + task1.getDurationInMinutes());
        task1.setPreviousTaskOrUser(anchor);

        task2 = new Task(2, "Task2", 1);
        task2.setDurationInMinutes(2);
        task2.setPreviousTaskOrUser(task1);
        task2.setStartTimeInMinutes(task1.getEndTimeInMinutes());
        task2.setEndTime(task2.getStartTimeInMinutes() + task2.getDurationInMinutes());
        task1.setNextTask(task2);

        task3 = new Task(3, "Task3", 1);
        task3.setDurationInMinutes(3);
        task3.setPreviousTaskOrUser(task2);

        task4 = new Task(4, "Task4", 1);
        task4.setDurationInMinutes(4);
        task4.setPreviousTaskOrUser(task3);
        task3.setNextTask(task4);

        task5 = new Task(5, "Task5", 1);
        task5.setDurationInMinutes(5);
        task5.setPreviousTaskOrUser(task4);
        task4.setNextTask(task5);
    }

    @Test
    public void afterEntityAdded() {
        listener.afterEntityAdded(scoreDirector, task3);
        verifyTimes();
    }

    @Test
    public void afterVariableChanged() {
        listener.afterVariableChanged(scoreDirector, task3);
        verifyTimes();
    }

    private void verifyTimes() {
        assertEquals((long) (task1.getDurationInMinutes() + task2.getDurationInMinutes()), (long) task3.getStartTimeInMinutes());
        assertEquals((long) (task1.getDurationInMinutes() + task2.getDurationInMinutes() + task3.getDurationInMinutes()), (long) task3.getEndTimeInMinutes());
        assertEquals((long) (task1.getDurationInMinutes() + task2.getDurationInMinutes() + task3.getDurationInMinutes()), (long) task4.getStartTimeInMinutes());
        assertEquals((long) (task1.getDurationInMinutes() + task2.getDurationInMinutes() + task3.getDurationInMinutes() + task4.getDurationInMinutes()), (long) task4.getEndTimeInMinutes());
        assertEquals((long) (task1.getDurationInMinutes() + task2.getDurationInMinutes()) + task3.getDurationInMinutes() + task4.getDurationInMinutes(), (long) task5.getStartTimeInMinutes());
        assertEquals((long) (task1.getDurationInMinutes() + task2.getDurationInMinutes()) + task3.getDurationInMinutes() + task4.getDurationInMinutes() + task5.getDurationInMinutes(), (long) task5.getEndTimeInMinutes());

        Stream.of(task3, task4, task5).forEach(task -> {
            verify(scoreDirector).beforeVariableChanged(task, Task.START_TIME_IN_MINUTES);
            verify(scoreDirector).afterVariableChanged(task, Task.START_TIME_IN_MINUTES);
            verify(scoreDirector).beforeVariableChanged(task, Task.END_TIME_IN_MINUTES);
            verify(scoreDirector).afterVariableChanged(task, Task.END_TIME_IN_MINUTES);
        });
    }
}
