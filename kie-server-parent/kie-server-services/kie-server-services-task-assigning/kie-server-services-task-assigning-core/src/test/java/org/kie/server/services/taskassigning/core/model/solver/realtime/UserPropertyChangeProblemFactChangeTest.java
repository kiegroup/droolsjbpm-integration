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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.taskassigning.core.model.Group;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockTask;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockUser;
import static org.kie.server.services.taskassigning.core.model.solver.realtime.ProblemFactChangeUtilTest.assertTaskWasNotReleased;
import static org.kie.server.services.taskassigning.core.model.solver.realtime.ProblemFactChangeUtilTest.assertTaskWasReleased;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPropertyChangeProblemFactChangeTest {

    private static final String USER_ID = "USER_ID";
    private static final long TASK_ID1 = 1;
    private static final long TASK_ID2 = 2;
    private static final long TASK_ID3 = 3;
    private static final long TASK_ID4 = 4;

    @Mock
    private ScoreDirector<TaskAssigningSolution> scoreDirector;

    private TaskAssigningSolution workingSolution;

    private UserPropertyChangeProblemFactChange change;

    private User user;

    private Map<String, Object> newAttributes;

    private Set<Group> newGroups;

    private Map<String, Set<Object>> newLabelValues;

    private boolean newEnabled;

    @Before
    public void setUp() {
        newAttributes = new HashMap<>();
        newAttributes.put("attribute1", "value1");
        newGroups = new HashSet<>();
        newGroups.add(new Group(1, "test-group"));
        newLabelValues = new HashMap<>();
        newLabelValues.put("label1", new HashSet<>());
        newEnabled = false;
        user = new User(1, USER_ID);
        workingSolution = new TaskAssigningSolution(1, new ArrayList<>(), new ArrayList<>());
        when(scoreDirector.getWorkingSolution()).thenReturn(workingSolution);
        change = new UserPropertyChangeProblemFactChange(user, newEnabled, newAttributes, newLabelValues, newGroups);
    }

    @Test
    public void getUser() {
        assertThat(change.getUser()).isEqualTo(user);
    }

    @Test
    public void getAttributes() {
        assertThat(change.getAttributes()).isEqualTo(newAttributes);
    }

    @Test
    public void getLabelValues() {
        assertThat(change.getLabelValues()).isEqualTo(newLabelValues);
    }

    @Test
    public void getGroups() {
        assertThat(change.getGroups()).isEqualTo(newGroups);
    }

    @Test
    public void isEnabled() {
        assertThat(change.isEnabled()).isEqualTo(false);
    }

    @Test
    public void doChange() {
        List<Task> workingUserTasks = Arrays.asList(mockTask(TASK_ID1, true),
                                                    mockTask(TASK_ID2, true),
                                                    mockTask(TASK_ID3, false),
                                                    mockTask(TASK_ID4, false));

        User workingUser = mockUser(USER_ID, workingUserTasks);
        workingUser.setGroups(new HashSet<>());
        workingUser.setAttributes(new HashMap<>());
        workingUser.setAllLabelValues(new HashMap<>());
        workingUser.setEnabled(true);

        when(scoreDirector.lookUpWorkingObjectOrReturnNull(user)).thenReturn(workingUser);
        change.doChange(scoreDirector);
        verify(scoreDirector).beforeProblemPropertyChanged(workingUser);
        verify(scoreDirector).afterProblemPropertyChanged(workingUser);
        verify(scoreDirector).triggerVariableListeners();

        assertThat(workingUser.isEnabled()).isEqualTo(newEnabled);
        assertThat(workingUser.getGroups()).isEqualTo(newGroups);
        assertThat(workingUser.getAllLabelValues()).isEqualTo(newLabelValues);
        assertThat(workingUser.getAttributes()).isEqualTo(newAttributes);

        List<Task> pinnedTasks = Arrays.asList(workingUserTasks.get(0), workingUserTasks.get(1));
        for (Task pinnedTask : pinnedTasks) {
            assertThat(pinnedTask.isPinned()).isTrue();
            assertTaskWasNotReleased(pinnedTask, scoreDirector);
        }

        List<Task> releasedTasks = Arrays.asList(workingUserTasks.get(2), workingUserTasks.get(3));
        releasedTasks.forEach(releasedTask -> assertTaskWasReleased(releasedTask, scoreDirector));
    }

    @Test
    public void doChangeUserDontExists() {
        when(scoreDirector.lookUpWorkingObjectOrReturnNull(user)).thenReturn(null);
        change.doChange(scoreDirector);
        verify(scoreDirector, never()).beforeProblemPropertyChanged(any());
        verify(scoreDirector, never()).afterProblemPropertyChanged(any());
        verify(scoreDirector, never()).triggerVariableListeners();
    }
}
