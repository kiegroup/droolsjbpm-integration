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

package org.kie.server.services.taskassigning.planning.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Test;
import org.kie.server.services.taskassigning.core.model.Task;
import org.kie.server.services.taskassigning.core.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.planning.TestUtil.assertContains;
import static org.kie.server.services.taskassigning.planning.TestUtil.initializeUser;
import static org.kie.server.services.taskassigning.planning.TestUtil.mockExternalGroup;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserUtilTest {

    private static final String USER_ID = "USER_ID";
    private static final String GROUP1_ID = "GROUP1_ID";
    private static final String GROUP2_ID = "GROUP2_ID";
    private static final String GROUP3_ID = "GROUP3_ID";

    @Test
    public void fromExternalUser() {
        org.kie.server.services.taskassigning.user.system.api.User externalUser =
                mock(org.kie.server.services.taskassigning.user.system.api.User.class);
        when(externalUser.getId()).thenReturn(USER_ID);

        org.kie.server.services.taskassigning.user.system.api.Group externalGroup1 = mockExternalGroup(GROUP1_ID);
        org.kie.server.services.taskassigning.user.system.api.Group externalGroup2 = mockExternalGroup(GROUP2_ID);
        org.kie.server.services.taskassigning.user.system.api.Group externalGroup3 = mockExternalGroup(GROUP3_ID);
        Set<org.kie.server.services.taskassigning.user.system.api.Group> externalGroups = new HashSet<>();
        externalGroups.add(externalGroup1);
        externalGroups.add(externalGroup2);
        externalGroups.add(externalGroup3);
        when(externalUser.getGroups()).thenReturn(externalGroups);

        User user = UserUtil.fromExternalUser(externalUser);
        assertEquals(user.getId(), USER_ID.hashCode(), 0);
        assertEquals(user.getEntityId(), USER_ID);

        assertEquals(user.getGroups().size(), externalGroups.size());
        assertContains(GROUP1_ID, user.getGroups());
        assertContains(GROUP2_ID, user.getGroups());
        assertContains(GROUP3_ID, user.getGroups());
    }

    @Test
    public void isUser() {
        assertTrue(UserUtil.isUser("User"));
        assertFalse(UserUtil.isUser("NotAUser"));
    }

    @Test
    public void extractTasks() {
        Task task1 = new Task(1, "Task1", 0);
        Task task2 = new Task(2, "Task2", 0);
        Task task3 = new Task(3, "Task3", 0);
        Task task4 = new Task(4, "Task4", 0);
        User user = initializeUser(new User(1, "User1"), Arrays.asList(task1, task2, task3, task4));
        Predicate<Task> predicate = (task -> task2.getId().equals(task.getId()) || task4.getId().equals(task.getId()));
        List<Task> tasks = UserUtil.extractTasks(user, predicate);
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task2));
        assertTrue(tasks.contains(task4));
    }
}
