/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.core.model.solver.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.services.taskassigning.core.model.ModelConstants;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.core.model.TaskOrUser;
import org.kie.server.services.taskassigning.core.model.User;
import org.optaplanner.core.impl.heuristic.selector.move.generic.ChangeMove;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockGroup;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockTask;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class TaskByGroupAndSkillsChangeMoveFilterTest {

    private static final String GROUP = "GROUP";
    private static final String SKILL = "SKILL";
    private static final String USER = "USER";

    private ScoreDirector<TaskAssigningSolution> scoreDirector;

    private TaskByGroupAndSkillsChangeMoveFilter filter;

    @Parameterized.Parameter
    public ChangeMove<TaskAssigningSolution> changeMove;

    @Parameterized.Parameter(1)
    public boolean moveAccepted;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();

        data.add(new Object[]{mockChangeMove(mockTask(Collections.emptyList(), Collections.emptySet()), ModelConstants.PLANNING_USER), true});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.emptyList(), Collections.emptySet()),
                        mockUser(USER, false, Collections.emptyList(), Collections.emptySet())), false});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.emptyList(), Collections.emptySet()),
                        mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), false});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.singletonList(mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), Collections.emptySet()),
                        mockUser(USER, false, Collections.emptyList(), Collections.emptySet())), false});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.singletonList(mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), Collections.emptySet()),
                        mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), true});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.singletonList(mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), Collections.singleton(SKILL)),
                        mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), false});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.singletonList(mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), Collections.singleton(SKILL)),
                        mockUser(USER, true, Collections.emptyList(), Collections.singleton(SKILL))), true});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.singletonList(mockGroup(GROUP)), Collections.emptySet()),
                        mockUser(USER, true, Collections.emptyList(), Collections.emptySet())), false});

        data.add(new Object[]{
                mockChangeMove(
                        mockTask(Collections.singletonList(mockGroup(GROUP)), Collections.emptySet()),
                        mockUser(USER, true, Collections.singletonList(mockGroup(GROUP)), Collections.emptySet())), true});

        return data;
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        scoreDirector = mock(ScoreDirector.class);
        filter = new TaskByGroupAndSkillsChangeMoveFilter();
    }

    @Test
    public void accept() {
        assertThat(filter.accept(scoreDirector, changeMove)).isEqualTo(moveAccepted);
    }

    @SuppressWarnings("unchecked")
    private static ChangeMove<TaskAssigningSolution> mockChangeMove(Task task, User user) {
        ChangeMove<TaskAssigningSolution> changeMove = mock(ChangeMove.class);
        TaskOrUser taskOrUser = mock(TaskOrUser.class);
        when(changeMove.getToPlanningValue()).thenReturn(taskOrUser);
        when(taskOrUser.getUser()).thenReturn(user);
        when(changeMove.getEntity()).thenReturn(task);
        return changeMove;
    }
}
