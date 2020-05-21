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

package org.kie.server.services.taskassigning.core.model.solver.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.services.taskassigning.core.model.ModelConstants;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockGroup;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockTask;
import static org.kie.server.services.taskassigning.core.model.TestUtil.mockUser;

@RunWith(Parameterized.class)
public class TaskAssigningConditionsTest {

    private static final String GROUP1 = "GROUP1";
    private static final String SKILL1 = "SKILL1";
    private static final String USER1 = "USER1";

    @Parameterized.Parameter
    public Task task;

    @Parameterized.Parameter(1)
    public User user;

    @Parameterized.Parameter(2)
    public boolean meetsPotentialOwnerResult;

    @Parameterized.Parameter(3)
    public boolean meetRequiredSkillsResult;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{mockTask(Collections.emptyList(), Collections.emptySet()), null, false, false});

        data.add(new Object[]{mockTask(Collections.emptyList(), Collections.emptySet()), ModelConstants.PLANNING_USER, true, true});

        data.add(new Object[]{
                mockTask(Collections.emptyList(), Collections.emptySet()),
                mockUser(USER1, false, Collections.emptyList(), Collections.emptySet()), false, false});

        data.add(new Object[]{
                mockTask(Collections.emptyList(), Collections.emptySet()),
                mockUser(USER1, true, Collections.emptyList(), Collections.emptySet()), false, true});

        data.add(new Object[]{
                mockTask(Collections.singletonList(mockUser(USER1, true, Collections.emptyList(), Collections.emptySet())), Collections.emptySet()),
                mockUser(USER1, false, Collections.emptyList(), Collections.emptySet()), false, false});

        data.add(new Object[]{
                mockTask(Collections.singletonList(mockUser(USER1, true, Collections.emptyList(), Collections.emptySet())), Collections.emptySet()),
                mockUser(USER1, true, Collections.emptyList(), Collections.emptySet()), true, true});

        data.add(new Object[]{
                mockTask(Collections.singletonList(mockUser(USER1, true, Collections.emptyList(), Collections.emptySet())), Collections.singleton(SKILL1)),
                mockUser(USER1, true, Collections.emptyList(), Collections.emptySet()), true, false});

        data.add(new Object[]{
                mockTask(Collections.singletonList(mockUser(USER1, true, Collections.emptyList(), Collections.emptySet())), Collections.singleton(SKILL1)),
                mockUser(USER1, true, Collections.emptyList(), Collections.singleton(SKILL1)), true, true});

        data.add(new Object[]{
                mockTask(Collections.singletonList(mockGroup(GROUP1)), Collections.emptySet()),
                mockUser(USER1, true, Collections.emptyList(), Collections.emptySet()), false, true});

        data.add(new Object[]{
                mockTask(Collections.singletonList(mockGroup(GROUP1)), Collections.emptySet()),
                mockUser(USER1, true, Collections.singletonList(mockGroup(GROUP1)), Collections.emptySet()), true, true});

        return data;
    }

    @Test
    public void userMeetsPotentialOwnerOrPlanningUserCondition() {
        assertThat(TaskAssigningConditions.userMeetsPotentialOwnerOrPlanningUserCondition(task, user))
                .isEqualTo(meetsPotentialOwnerResult);
    }

    @Test
    public void userMeetsRequiredSkillsOrPlanningUserCondition() {
        assertThat(TaskAssigningConditions.userMeetsRequiredSkillsOrPlanningUserCondition(task, user))
                .isEqualTo(meetRequiredSkillsResult);
    }
}
