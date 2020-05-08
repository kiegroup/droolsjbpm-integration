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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.taskassigning.core.model.DefaultTaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.User;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddUserProblemFactChangeTest {

    private static final String USER_ID = "USER_ID";

    @Mock
    private ScoreDirector<TaskAssigningSolution<?>> scoreDirector;

    private TaskAssigningSolution<?> workingSolution;

    private AddUserProblemFactChange change;

    private User user;

    @Before
    public void setUp() {
        user = new User(1, USER_ID);
        workingSolution = new DefaultTaskAssigningSolution(1, new ArrayList<>(), new ArrayList<>());
        doReturn(workingSolution).when(scoreDirector).getWorkingSolution();
        change = new AddUserProblemFactChange(user);
    }

    @Test
    public void getUser() {
        assertThat(change.getUser()).isEqualTo(user);
    }

    @Test
    public void doChange() {
        change.doChange(scoreDirector);
        verify(scoreDirector).beforeProblemFactAdded(user);
        verify(scoreDirector).afterProblemFactAdded(user);
        verify(scoreDirector).triggerVariableListeners();
        assertThat(workingSolution.getUserList()).contains(user);
    }

    @Test
    public void doChangeUserAlreadyExists() {
        when(scoreDirector.lookUpWorkingObjectOrReturnNull(user)).thenReturn(user);
        assertThatThrownBy(() -> change.doChange(scoreDirector))
                .hasMessage(String.format("A user with the given identifier id: %s already exists", USER_ID));
    }
}
