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

package org.jbpm.task.assigning.model.solver;

import java.util.stream.Stream;

import org.jbpm.task.assigning.model.Task;
import org.jbpm.task.assigning.model.User;
import org.jbpm.task.assigning.model.solver.StartAndEndTimeUpdatingVariableListener;
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

        anchor = new User(1, null);
        task1 = new Task(1, null, 1);
        task1.setStartTime(anchor.getEndTime());
        task1.setDuration(1);
        task1.setEndTime(task1.getStartTime() + task1.getDuration());
        task1.setPreviousTaskOrUser(anchor);

        task2 = new Task(2, null, 1);
        task2.setDuration(2);
        task2.setPreviousTaskOrUser(task1);
        task2.setStartTime(task1.getEndTime());
        task2.setEndTime(task2.getStartTime() + task2.getDuration());
        task1.setNextTask(task2);

        task3 = new Task(3, null, 1);
        task3.setDuration(3);
        task3.setPreviousTaskOrUser(task2);

        task4 = new Task(4, null, 1);
        task4.setDuration(4);
        task4.setPreviousTaskOrUser(task3);
        task3.setNextTask(task4);

        task5 = new Task(5, null, 1);
        task5.setDuration(5);
        task5.setPreviousTaskOrUser(task4);
        task4.setNextTask(task5);
    }

    @Test
    public void afterEntityAddedTest() {
        listener.afterEntityAdded(scoreDirector, task3);
        verifyTimes();
    }

    @Test
    public void afterVariableChangedTest() {
        listener.afterVariableChanged(scoreDirector, task3);
        verifyTimes();
    }

    private void verifyTimes() {
        assertEquals((long) (task1.getDuration() + task2.getDuration()), (long) task3.getStartTime());
        assertEquals((long) (task1.getDuration() + task2.getDuration() + task3.getDuration()), (long) task3.getEndTime());
        assertEquals((long) (task1.getDuration() + task2.getDuration() + task3.getDuration()), (long) task4.getStartTime());
        assertEquals((long) (task1.getDuration() + task2.getDuration() + task3.getDuration() + task4.getDuration()), (long) task4.getEndTime());
        assertEquals((long) (task1.getDuration() + task2.getDuration()) + task3.getDuration() + task4.getDuration(), (long) task5.getStartTime());
        assertEquals((long) (task1.getDuration() + task2.getDuration()) + task3.getDuration() + task4.getDuration() + task5.getDuration(), (long) task5.getEndTime());

        Stream.of(task3, task4, task5).forEach(task -> {
            verify(scoreDirector).beforeVariableChanged(task, "startTime");
            verify(scoreDirector).afterVariableChanged(task, "startTime");
            verify(scoreDirector).beforeVariableChanged(task, "endTime");
            verify(scoreDirector).afterVariableChanged(task, "endTime");
        });
    }
}
